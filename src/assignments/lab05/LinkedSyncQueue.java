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
	 * - con CLOSED si intende che non è possibile inserire nuovi elementi;
	 * - con SHUTDOWN si intende che non è possibile né inserire nuovi elementi né consumare quelli già presenti.
	 * Al momento della creazione, lo stato della coda è OPEN, ed è possibile chiamare il metodo close()
	 * per impostarlo su CLOSED o shutdown() per impostarlo su SHUTDOWN: in questo modo è possibile ad esempio
	 * notificare tutti gli altri thread che operano sulla coda che non possono essere più aggiunti o rimossi
	 * altri elementi e che quindi dovranno terminare.
	 * In questo modo si gestisce anche la terminazione per interruzione senza dover inviare sulla coda
	 * un messaggio di terminazione esplicito a tutti i thread attivi e che questi devono saper interpretare.
	 */
	private static final int OPEN = 0;
	private static final int CLOSED = 1;
	private static final int SHUTDOWN = 2;
	
	private LinkedList<T> list; /* LinkedList per mantenere gli elementi in coda. */
	private final int capacity; /* Capacità della coda: se viene impostata a 0, la coda è unbounded. */
	public int state; /* Stato della coda (OPEN/CLOSED/SHUTDOWN). */
	
	public LinkedSyncQueue(int capacity) {
		if (capacity < 0) throw new IllegalArgumentException();
		this.list = new LinkedList<T>();
		this.capacity = capacity;
		this.state = OPEN;
	}
	
	/* Di default la coda è costruita unbounded. */
	public LinkedSyncQueue() { this(0); }
	
	/**
	 * Controlla se è o sara possibile estrarre un elemento dalla coda.
	 * @return true se non si possono più estrarre altri elementi dalla coda (stato == SHUTDOWN oppure stato == CLOSED
	 * e coda vuota), false altrimenti.
	 */
	private boolean noMoreItems() {
		if (this.state == SHUTDOWN) return true;
		else if ((this.state == CLOSED) && (this.list.size() <= 0)) return true;
		else return false;
	}
	
	/**
	 * @param item L'elemento da aggiungere in coda.
	 * @return true se l'operaione ha successo, false se la coda è CLOSED o SHUTDOWN o se il metodo add() della
	 * LinkedList fallisce. NOTA: Se la coda è CLOSED o SHUTDOWN quando viene acquisita la lock del monitor per
	 * la prima volta, il metodo termina immediatamente.
	 * @throws InterruptedException Se la wait() viene interrotta.
	 */
	public synchronized boolean put(T item) throws InterruptedException {
		if (item == null) throw new NullPointerException();
		boolean b = true;
		if (this.state >= CLOSED) b = false;
		if (b) {
			while ((this.capacity > 0) && (this.list.size() >= this.capacity)) this.wait();
			if (this.state >= CLOSED) b = false;
		}
		if (b) b = this.list.add(item);
		this.notifyAll();
		return b;
	}
	
	/**
	 * @return Il primo elemento della coda se l'operazione ha successo, null se la coda è SHUTDOWN oppure
	 * se è CLOSED e vuota.
	 * @throws InterruptedException Se la wait() viene interrotta.
	 */
	public synchronized T get() throws InterruptedException {
		T item;
		if (this.noMoreItems()) item = null;
		else {
			while ((this.state == OPEN) && (this.list.size() <= 0)) this.wait();
			if (this.noMoreItems()) item = null;
			else item = this.list.remove(0); //state == OPEN && (lista non vuota)
		}
		this.notifyAll();
		return item;
	}
	
	/**
	 * @return true se la coda è vuota, false altrimenti.
	 */
	public synchronized boolean isEmpty() {
		boolean b = this.list.isEmpty();
		this.notifyAll();
		return b;
	}
	
	public synchronized void close() {
		if (this.state == OPEN) this.state = CLOSED;
		this.notifyAll();
	}
	
	public synchronized void shutdown() {
		if (this.state != SHUTDOWN) this.state = SHUTDOWN;
		this.notifyAll();
	}

	public synchronized int getCapacity() {
		this.notifyAll();
		return this.capacity;
	}

	public synchronized int getState() {
		this.notifyAll();
		return this.state;
	}
	
}