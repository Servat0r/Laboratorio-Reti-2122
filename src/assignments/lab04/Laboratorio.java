package assignments.lab04;

import java.util.Arrays;

public final class Laboratorio {
	
	/*
	 * 1. La sincronizzazione è affidata al monitor dell'oggetto Laboratorio corrente.
	 * 2. Si assume che la precedenza Professori - Tesisti - Studenti sia intesa in modo che ad ogni
	 * istante t si considerano solo gli utenti non sospesi su una sleep() dopo essere stati nel
	 * laboratorio e fra questi un professore avrà sempre la precedenza per l'ingresso al laboratorio rispetto
	 * a tesisti e studenti, e un tesista avrà sempre la precedenza per l'accesso al computer di interesse
	 * rispetto agli studenti.
	 * 3. La gestione della precedenza Professori - Tesisti - Studenti è affidata ai metodi privati
	 * profShouldWait(), thesistShouldWait(), studentShouldWait() e il risveglio dei thread in
	 * attesa avviene sempre con una notifyAll() se e solo se c'è effettivamente almeno un thread
	 * in attesa (controllato da profWaitNum, studentWaitNum, thesistWaitNum).
	 * 4. Si assume che uno studente accetti il computer correntemente libero il cui id sia minimo.
	 */
	
	public static final int MIN_PC = 1; //Minimo id di un computer 
	public static final int MAX_PC = 20; //Massimo id di un computer

	private boolean profInside; //Il laboratorio è occupato da un professore?
	private boolean busy[]; //Quali computer sono occupati?
	
	private int profWaitNum; //Quanti professori sono in attesa (per il laboratorio)?
	private int thesistWaitNum[]; //Quanti tesisti sono in attesa (per computer)?
	private int studentWaitNum; //Quanti studenti sono in attesa (per qualsiasi computer)?
	
	public Laboratorio() {
		this.profInside = false;
		this.busy = new boolean[getNumComputers()];
		for (int i = 0; i < getNumComputers(); i++) this.busy[i] = false;
		this.profWaitNum = 0;
		this.thesistWaitNum = new int[getNumComputers()];
		for (int i = 0; i < getNumComputers(); i++) this.thesistWaitNum[i] = 0;		
		this.studentWaitNum = 0;
	}
	
	/**
	 * @return true sse il professore deve aspettare (i.e., se c'è qualcun altro nel laboratorio).
	 * Richiede di aver già acquisito la lock sul laboratorio corrente.
	 */
	private boolean profShouldWait() {
		if (this.profInside) return true;
		for (int i = 0; i < getNumComputers(); i++) {
			if (this.busy[i]) return true;
		}
		return false; //Nessun computer è attualmente in uso
	}
	
	/**
	 * @param pcId Id del computer di interesse del tesista (in [0,MAX_PC-MIN_PC]).
	 * @return true sse il tesista deve aspettare (i.e., c'è un professore dentro o in attesa o il computer
	 * di interesse è occupato).
	 * Richiede di aver già acquisito la lock sul laboratorio corrente.
	 */
	private boolean thesistShouldWait(int pcId) {
		if (this.profInside) return true; //Un professore occupa il laboratorio
		if (this.profWaitNum > 0) return true; //C'è almeno un professore in attesa
		return this.busy[pcId]; //Il computer che serve è occupato
	}
	
	/**
	 * @return true sse lo studente deve aspettare (i.e., c'è un professore dentro o in attesa oppure per ogni
	 * computer o quest'ultimo è occupato o almeno un tesista è in attesa per esso).
	 * Richiede di aver già acquisito la lock sul laboratorio corrente.
	 */
	private boolean studentShouldWait() {
		if (this.profInside) return true;
		if (this.profWaitNum > 0) return true;
		for (int i = 0; i < getNumComputers(); i++) {
			if (!this.busy[i] && (this.thesistWaitNum[i] == 0)) return false;
		}
		return true;
	}
	
	/**
	 * Chiama la notifyAll() se e solo se c'è almeno un utente in attesa.
	 * Richiede di aver già acquisito la lock sul laboratorio corrente.
	 */
	private void broadcast() {
		int thWaitNum = Arrays.stream(this.thesistWaitNum).sum(); //Somma degli elementi di thesistWaitNum
		if (this.profWaitNum + this.studentWaitNum + thWaitNum > 0) this.notifyAll();
	}
	
	/**
	 * @return Quanti computer ci sono nel laboratorio.
	 */
	public static int getNumComputers() {
		return Laboratorio.MAX_PC - Laboratorio.MIN_PC + 1;
	}
	
	/**
	 * Controlla che pcNum sia un identificatore valido di un pc.
	 * @param pcNum L'identificatore da controllare.
	 */
	public static boolean checkPCNum(int pcNum) {
		if ((pcNum < MIN_PC) || (pcNum > MAX_PC)) return false;
		return true;
	}
	
	/**
	 * @return true sse l'operazione termina con successo.
	 * @throws InterruptedException Se il thread sospeso su labLock viene interrotto (rilascia comunque la lock).
	 */
	public synchronized boolean entraProfessore() throws InterruptedException {		
		while (this.profShouldWait()) {
			this.profWaitNum++;
			this.wait();
			this.profWaitNum--;
		}
		this.profInside = true;
		for (int i = 0; i < getNumComputers(); i++) this.busy[i] = true;
		return true;
	}
	
	/**
	 * @return true se l'operazione termina con successo, false se non risulta nessun professore nel laboratorio.
	 */
	public synchronized boolean esciProfessore() {
		if (!this.profInside) return false;
		this.profInside = false;
		for (int i = 0; i < getNumComputers(); i++) this.busy[i] = false; //Libera tutti i computer
		this.broadcast();
		return true;
	}
	
	/**
	 * @param pcNum Id del computer (in [MIN_PC, MAX_PC]) a cui è interessato il tesista.
	 * @return true in caso di successo, false se pcNum non identifica alcun computer nel laboratorio.
	 * @throws InterruptedException Se il thread sospeso su labLock viene interrotto (rilascia comunque la lock).
	 */
	public synchronized boolean entraTesista(int pcNum) throws InterruptedException {
		if (!checkPCNum(pcNum)) return false;
		int pcId = pcNum - MIN_PC;
		while (this.thesistShouldWait(pcId)) {
			this.thesistWaitNum[pcId]++;
			this.wait();
			this.thesistWaitNum[pcId]--;
		}
		this.busy[pcId] = true;
		return true;
	}
	
	/**
	 * @param pcNum Id del computer (in [MIN_PC, MAX_PC]) a cui è interessato il tesista.
	 * @return true se l'operazione termina con successo, false se il computer #pcNum non è occupato o pcNum
	 * non identifica un computer nel laboratorio.
	 */
	public synchronized boolean esciTesista(int pcNum) {
		if (!checkPCNum(pcNum)) return false;
		int pcId = pcNum - MIN_PC;
		if (!this.busy[pcId]) return false; //Il computer corrispondente NON è stato occupato
		this.busy[pcId] = false;
		this.broadcast();
		return true;
	}
	
	/**
	 * @return L'id del computer (in [MIN_PC, MAX_PC]) assegnato allo studente in caso di successo,
	 * MIN_PC - 1 altrimenti.
	 * @throws InterruptedException
	 */
	public synchronized int entraStudente() throws InterruptedException {
			while (this.studentShouldWait()) {
				this.studentWaitNum++;
				this.wait();
				this.studentWaitNum--;
			}
			for (int i = 0; i < MAX_PC; i++) {
				if (!this.busy[i] && (this.thesistWaitNum[i] == 0)) {
					this.busy[i] = true;
					return MIN_PC + i; 
				}
			}
			return MIN_PC - 1;
	}
	
	/**
	 * 
	 * @param pcNum L'id del computer (in [MIN_PC, MAX_PC]) occupato dallo studente.
	 * @return true se l'operazione termina con successo, false se pcNum non identifica alcun computer del
	 * laboratorio o il pc corrispondente non è occupato.
	 */
	public synchronized boolean esciStudente(int pcNum) {
		if (!checkPCNum(pcNum)) return false;
		int pcId = pcNum - MIN_PC;
		if (!this.busy[pcId]) return false;
		this.busy[pcId] = false;
		this.broadcast();
		return true;
	}
}