package assignments.lab05;

import java.util.LinkedList;

/**
 * Coda FIFO sincronizzata implementata mediante LinkedList e monitor.
 * @author Salvatore Correnti
 */

public final class LinkedSyncQueue<T> {
	
	/*
	 * Descrittori dello "stato" della coda, dove:
	 * - con OPEN si intende che la coda permette l'inserimento di nuovi elementi;
	 * - con CLOSED si intende che non � possibile inserire nuovi elementi.
	 * Al momento della creazione, lo stato della coda � OPEN, ed � possibile chiamare il metodo close()
	 * per impostarlo su CLOSED: in questo modo � possibile ad esempio notificare tutti gli altri thread
	 * che operano sulla coda che non verranno pi� aggiunti altri elementi e che quindi dovranno terminare,
	 * nel caso dei consumatori quando la coda si sar� svuotata e nel caso del produttore immediatamente
	 * perch� non potr� inserire altri elementi.
	 * In questo modo si gestisce anche la terminazione per interruzione senza dover inviare sulla coda
	 * un messaggio di terminazione esplicito a tutti i thread attivi e che questi devono saper interpretare.
	 */
	private static final boolean OPEN = true;
	private static final boolean CLOSED = false;
	
	private LinkedList<T> list; //LinkedList per mantenere gli elementi in coda.
	private final int capacity; //Capacit� della coda: se viene impostata a 0, la coda � unbounded.
	public boolean state; //Stato della coda (OPEN/CLOSED).
	
	public LinkedSyncQueue(int capacity) {
		if (capacity < 0) throw new IllegalArgumentException();
		this.list = new LinkedList<T>();
		this.capacity = capacity;
		this.state = OPEN;
	}
	
	public LinkedSyncQueue() { this(0); }
	
	/**
	 * @param item L'elemento da aggiungere in coda.
	 * @return true se l'operaione ha successo, false se la coda � chiusa. NOTA: Se la coda � chiusa
	 * quando viene acquisita la lock implicita per la prima volta, il metodo termina immediatamente.
	 * @throws InterruptedException Se la wait() viene interrotta.
	 */
	public synchronized boolean put(T item) throws InterruptedException {
		if (item == null) throw new NullPointerException();
		if (this.state == CLOSED) return false;
		while ((this.capacity > 0) && (this.list.size() >= this.capacity)) this.wait();
		if (this.state == CLOSED) return false;
		this.list.add(item);
		this.notifyAll();
		return true;
	}
	
	/**
	 * @return Il primo elemento della coda se l'operazione ha successo, null se la coda � chiusa e vuota.
	 * NOTA: Se la coda � chiusa ma NON vuota, viene comunque estratto un elemento, per cui � sufficiente
	 * solo il primo controllo all'inizio per restituire null.
	 * @throws InterruptedException Se la wait() viene interrotta.
	 */
	public synchronized T get() throws InterruptedException {
		if ((this.state == CLOSED) && (this.list.size() <= 0)) return null;
		while (this.list.size() <= 0) this.wait();
		T item = this.list.remove(0);
		this.notifyAll();
		return item;
	}
	
	public synchronized boolean isEmpty() throws InterruptedException {
		boolean b = this.list.isEmpty();
		this.notifyAll();
		return b;
	}
	
	public synchronized void close() {
		if (this.state == OPEN) this.state = CLOSED;
		this.notifyAll();
	}
}