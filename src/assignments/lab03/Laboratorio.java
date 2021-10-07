package assignments.lab03;


import java.util.concurrent.locks.*;
import java.util.*;

public final class Laboratorio {
	
	public static final int MIN_PC = 1; //Minimo id di un computer 
	public static final int MAX_PC = 20; //Massimo id di un computer

	private boolean profInside; //Il laboratorio è occupato da un professore?
	private boolean busy[]; //Quali computer sono occupati?
	
	private Lock labLock; //Accesso al laboratorio

	private Condition profWait; //Professori in attesa (per il laboratorio)
	private int profWaitNum; //Quanti professori sono in attesa (per il laboratorio)?
	
	/*
	 * Lista di Condition Variables corrispondenti alle code di tesisti in attesa per ogni computer
	 * (thesistWait.get(i) è la coda dei tesisti in attesa del pc #(MIN_PC+i))
	 */
	private List<Condition> thesistWait;
	private int[] thesistWaitNum; //Quanti tesisti sono in attesa (per computer)?
	
	private Condition studentWait; //Studenti in attesa (per qualsiasi computer)
	private int studentWaitNum; //Quanti studenti sono in attesa (per qualsiasi computer)?

	/**
	 * @return true sse il professore deve aspettare (i.e., se c'è qualcun altro nel laboratorio).
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
	 */
	private boolean thesistShouldWait(int pcId) {
		if (this.profInside) return true; //Un professore occupa il laboratorio
		if (this.profWaitNum > 0) return true; //C'è almeno un professore in attesa
		return this.busy[pcId]; //Il computer che serve è occupato
	}
	
	/**
	 * @return true sse lo studente deve aspettare (i.e., c'è un professore dentro o in attesa oppure per ogni
	 * computer o quest'ultimo è occupato o almeno un tesista è in attesa per esso).
	 */
	private boolean studentShouldWait() {
		if (this.profInside) return true;
		if (this.profWaitNum > 0) return true;
		for (int i = 0; i < getNumComputers(); i++) {
			if (!this.busy[i] && (this.thesistWaitNum[i] == 0)) return false;
		}
		return true;
	}
	
	public Laboratorio() {
		
		this.profInside = false;
		
		this.busy = new boolean[getNumComputers()];
		for (int i = 0; i < getNumComputers(); i++) this.busy[i] = false;
		
		this.labLock = new ReentrantLock();
		
		this.profWait = labLock.newCondition();
		this.profWaitNum = 0;
		
		this.thesistWait = new ArrayList<Condition>();
		this.thesistWaitNum = new int[getNumComputers()];
		
		for (int i = 0; i < getNumComputers(); i++) {
			this.thesistWait.add(this.labLock.newCondition());
			this.thesistWaitNum[i] = 0;
		}
		
		this.studentWait = this.labLock.newCondition();
		this.studentWaitNum = 0;
		
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
	 * @throws InterruptedException Se il thread sospeso su labLock viene interrotto (rilascia comunque la lock).
	 */
	public void entraProfessore() throws InterruptedException {
		this.labLock.lock();
		try {
			this.profWaitNum++;
			while (this.profShouldWait()) { this.profWait.await(); }
			this.profWaitNum--;
			this.profInside = true;
			for (int i = 0; i < getNumComputers(); i++) this.busy[i] = true;
		} finally { this.labLock.unlock(); }
	}
	
	/**
	 * @return true se l'operazione termina con successo, false se non risulta nessun professore nel laboratorio.
	 */
	public boolean esciProfessore() {
		this.labLock.lock();
		try {
			if (!this.profInside) return false;
			this.profInside = false;
			for (int i = 0; i < getNumComputers(); i++) this.busy[i] = false; //Libera tutti i computer
			if (this.profWaitNum > 0) this.profWait.signal();
			else {
				for (int j = 0; j < getNumComputers(); j++) {
					if (this.thesistWaitNum[j] > 0) this.thesistWait.get(j).signal();
					else this.studentWait.signal();
				}
			}
			return true;
		} finally { this.labLock.unlock(); }
	}
	
	/**
	 * @param pcNum Id del computer (in [MIN_PC, MAX_PC]) a cui è interessato il tesista.
	 * @return true in caso di successo, false se pcNum non identifica alcun computer nel laboratorio.
	 * @throws InterruptedException Se il thread sospeso su labLock viene interrotto (rilascia comunque la lock).
	 */
	public boolean entraTesista(int pcNum) throws InterruptedException {
		if (!checkPCNum(pcNum)) return false;
		int pcId = pcNum - MIN_PC;
		this.labLock.lock();
		try {
			this.thesistWaitNum[pcId]++;
			while (this.thesistShouldWait(pcId)) { this.thesistWait.get(pcId).await(); }
			this.thesistWaitNum[pcId]--;
			this.busy[pcId] = true;
			return true;
		} finally { this.labLock.unlock(); }
	}
	
	/**
	 * @param pcNum Id del computer (in [MIN_PC, MAX_PC]) a cui è interessato il tesista.
	 * @return true se l'operazione termina con successo, false se il computer #pcNum non è occupato o pcNum
	 * non identifica un computer nel laboratorio.
	 */
	public boolean esciTesista(int pcNum) {
		if (!checkPCNum(pcNum)) return false;
		int pcId = pcNum - MIN_PC;
		this.labLock.lock();
		try {
			if (!this.busy[pcId]) return false; //Il computer corrispondente NON è stato occupato
			this.busy[pcId] = false;
			if (this.profWaitNum > 0) this.profWait.signal();
			else if (this.thesistWaitNum[pcId] > 0) this.thesistWait.get(pcId).signal();
			else if (this.studentWaitNum > 0) this.studentWait.signal();
			return true;
		} finally { this.labLock.unlock(); }		
	}
	
	/**
	 * @return L'id del computer (in [MIN_PC, MAX_PC]) assegnato allo studente in caso di successo,
	 * MIN_PC - 1 altrimenti.
	 * @throws InterruptedException
	 */
	public int entraStudente() throws InterruptedException {
		this.labLock.lock();
		try {
			this.studentWaitNum++;
			while (this.studentShouldWait()) { this.studentWait.await(); }
			this.studentWaitNum--;
			for (int i = 0; i < MAX_PC; i++) {
				if (!this.busy[i] && (this.thesistWaitNum[i] == 0)) {
					this.busy[i] = true;
					return MIN_PC + i; 
				}
			}
			return MIN_PC - 1;
		} finally { this.labLock.unlock(); }
	}
	
	/**
	 * 
	 * @param pcNum L'id del computer (in [MIN_PC, MAX_PC]) occupato dallo studente.
	 * @return true se l'operazione termina con successo, false se pcNum non identifica alcun computer del
	 * laboratorio o il pc corrispondente non è occupato.
	 */
	public boolean esciStudente(int pcNum) {
		if (!checkPCNum(pcNum)) return false;
		int pcId = pcNum - MIN_PC;
		this.labLock.lock();
		try {
			if (!this.busy[pcId]) return false;
			this.busy[pcId] = false;
			if (this.profWaitNum > 0) this.profWait.signal();
			else if (this.thesistWaitNum[pcId] > 0) this.thesistWait.get(pcId).signal();
			else if (this.studentWaitNum > 0) this.studentWait.signal();
			return true;
		} finally { this.labLock.unlock(); }		
	}
}