package util.common;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;

/**
 * Wrapper della classe ByteBuffer che mantiene un controllo sullo stato del buffer,
 * permettendo di leggere e scrivere con SocketChannels e array di bytes.
 * @author Salvatore Correnti
 */
public final class MessageBuffer {

	/**
	 * Enumerazione che rappresenta lo stato del ByteBuffer e i dati significativi che contiene.
	 * INIT = buffer inizializzato, nessun dato significativo contenuto (position = 0, limit = capacity);
	 * READ = appena compiuta un'operazione di lettura dati dal buffer (tranne getAllData()), dati significativi in [0, limit)
	 * (position >= 0, normalizzabile con compact() a 0, limit <= capacity);
	 * WRITTEN = appena compiuta un'operazione di scrittura dati nel buffer, dati significativi in [0, position)
	 * (position >= 0, limit = capacity).
	 * @author Salvatore Correnti
	 */
	private static enum State {
		INIT, /* Buffer inizializzato, nessuna operazione ancora compiuta */
		READ, /* Compiuta un'operazione di lettura dati dal buffer: position = 0 (compact), limit = ultimo byte rilevante */
		WRITTEN, /* Compiuta un'operazione di scrittura dati nel buffer: position punta all'ultimo byte scritto, limit alla fine */
	}
		
	private ByteBuffer buffer;
	private State state;
	
	public MessageBuffer(int bufCap) {
		if (bufCap <= 0) throw new IllegalArgumentException();
		this.buffer = ByteBuffer.allocate(bufCap);
		this.state = State.INIT;
	}
	
	/**
	 * Se � appena stata compiuta una scrittura nel buffer, trasla i dati contenuti in [position, limit) in [0, limit-position),
	 * dopodich�, a differenza di {@link ByteBuffer#compact()}, setta limit = limit-position e position = 0.
	 * @return true se l'operazione � completata con successo, false se lo stato � diverso da State.READ o l'operazione
	 * non � completata con successo.
	 */
	public boolean compact() {
		if (this.state == State.INIT) return false;
		else if (this.state == State.WRITTEN) return false;
		else if (this.state == State.READ) {
			this.buffer.compact();
			this.buffer.flip();
			return true;
		} else throw new IllegalStateException();
	}
	
	/**
	 * Legge dati da un SocketChannel trasferendoli nel buffer interno e pone lo stato a State.WRITTEN .
	 * @param sc SocketChannel da cui leggere i dati.
	 * @return Il numero di bytes letti, -1 se nel canale si raggiunge la fine dello stream.
	 * @throws NotYetConnectedException Se lanciata da sc.read().
	 * @throws IOException Se lanciata da sc.read().
	 */
	public int readFromChannel(SocketChannel sc) throws NotYetConnectedException, IOException {
		Common.notNull(sc);
		int result = 0;
		if (this.state == State.READ) {
			this.buffer.position(this.buffer.limit());
			this.buffer.limit(this.buffer.capacity());
		}
		this.state = State.WRITTEN;
		result = sc.read(this.buffer);
		return result;
	}
	
	/**
	 * Scrive i dati in [position, limit) sul SocketChannel sc, pone lo stato a State.READ ed
	 * eventualmente compatta i dati rimanenti.
	 * @param sc SocketChannel su cui scrivere i dati.
	 * @param compact Se true al termine della scrittura viene eseguita una this.compact(),
	 * altrimenti non viene eseguito nulla.
	 * @return Il numero di bytes scritti se lo stato � diverso da State.INIT, -1 altrimenti.
	 * @throws IOException Se lanciata da sc.write() (e.g. in caso di chiusura del canale).
	 */
	public int writeToChannel(SocketChannel sc, boolean compact) throws IOException {
		Common.notNull(sc);
		int result = 0;
		if (this.state == State.INIT) result = -1; /* Non c'� niente da scrivere! */
		else { 
			if (this.state == State.WRITTEN) this.buffer.flip(); //position = 0, limit = old_position
			this.state = State.READ; /* Indipendentemente dal risultato della write! */
			result = sc.write(this.buffer);
			if (compact) this.compact();
		}
		return result;
	}
	
	public int writeToChannel(SocketChannel sc) throws IOException { return this.writeToChannel(sc, true); }
	
	/**
	 * Copia i dati contenuti nel buffer in un array di bytes, SENZA modificare lo stato del buffer n� il suo contenuto.
	 * @return Un array di bytes contenente i dati del buffer ([0, position) se lo stato � WRITTEN, [position, limit) se
	 * lo stato � READ), null altrimenti.
	 */
	public byte[] getAllData() {
		if (this.state == State.INIT) return null;
		else {
			byte[] result;
			int position = this.buffer.position();
			int limit = this.buffer.limit();
			if (this.state == State.WRITTEN) {
				result = new byte[position];
				this.buffer.flip();
			} else if (this.state == State.READ) {
				result = new byte[limit - position];
			} else throw new IllegalStateException();
			this.buffer.get(result);
			this.buffer.position(position);
			this.buffer.limit(limit);
			return result;
		}
	}
	
	/**
	 * Legge al pi� length bytes da array partendo da offset e pone lo stato a WRITTEN.
	 * @param array Array di bytes da cui leggere i dati.
	 * @param offset Prima posizione in array da cui leggere i dati.
	 * @param length Massimo numero di bytes leggibili dall'array (nei limiti
	 * della capacit� del buffer e della lunghezza dell'array).
	 * @return Il numero di bytes copiati nel buffer.
	 */
	public int readFromArray(byte[] array, int offset, int length) {
		Common.notNull(array);
		int maxCopy = Math.min(length, array.length - offset);
		if (maxCopy <= 0) throw new IllegalArgumentException();
		int copied = 0;
		if (this.state == State.READ) {
			this.buffer.position(this.buffer.limit());
			this.buffer.limit(this.buffer.capacity());
		}
		try {
			while (copied < maxCopy) { this.buffer.put(array[copied + offset]); copied++; }
		} catch (BufferOverflowException boe) {}
		this.state = State.WRITTEN;
		return copied;
	}
	
	/**
	 * Scrive i dati contenuti nel buffer ([0, position) se lo stato � WRITTEN,
	 * [position, limit) se lo stato � READ) nell'array passato dalla posizione
	 * offset per al pi� length bytes, ed eventualmente compatta i dati rimanenti
	 * alla fine oppure rimette position al valore iniziale.
	 * @param array Array di bytes in cui scrivere i dati.
	 * @param offset Prima posizione in array su cui scrivere.
	 * @param length Massimo numero di bytes scrivibili in array (nei limiti della
	 * lunghezza di array e del buffer).
	 * @param compact Se true, al termine della scrittura compatta il buffer eliminando
	 * i dati scritti in array, altrimenti ripristina position al valore iniziale.
	 * @return Il numero di bytes scritti se lo stato � diverso da INIT, -1 altrimenti.
	 */
	public int writeToArray(byte[] array, int offset, int length, boolean compact) {
		Common.notNull(array);
		int maxCopy = Math.min(length, array.length - offset);
		if (maxCopy <= 0) throw new IllegalArgumentException();
		if (this.state == State.INIT) return -1;
		else if (this.state == State.WRITTEN) this.buffer.flip();
		this.state = State.READ;
		int position = this.buffer.position();
		int copied = 0;
		try {
			while (copied < maxCopy) { array[copied + offset] = this.buffer.get(); copied++; }
		} catch (BufferUnderflowException bue) {}
		if (compact) this.compact();
		//TODO Eliminare il ramo else? Oppure usare dei flag?
		else {
			this.buffer.rewind();
			this.buffer.position(position);
		}
		return copied;
	}

	public int writeToArray(byte[] array, int offset, int length) { return this.writeToArray(array, offset, length, true); } 
	
	/**
	 * Resetta il buffer allo stato INIT.
	 */
	public void clear() {
		this.buffer.clear();
		this.state = State.INIT;
	}
	
	/**
	 * @return Il numero di bytes leggibili nel buffer se presenti.
	 */
	public int remaining() {
		if (this.state == State.INIT) return 0;
		else if (this.state == State.READ) return this.buffer.remaining();
		else if (this.state == State.WRITTEN) return this.buffer.position();
		else throw new IllegalStateException();
	}
	
	/**
	 * @return true sse this.remaining() > 0.
	 */
	public boolean hasRemaining() { return (this.remaining() > 0); }
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("MessageBuffer[state = ");
		sb.append(this.state.toString());
		sb.append("; position = " + this.buffer.position());
		sb.append("; limit = " + this.buffer.limit());
		sb.append("; capacity = " + this.buffer.capacity() + "]");
		return sb.toString();
	}
}