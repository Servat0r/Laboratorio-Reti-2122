package assignments.lab03;

import java.util.concurrent.ThreadLocalRandom;

public final class Studente extends Utente {
	
	private final Laboratorio lab;
	private final int k; //Numero di accessi che l'utente deve compiere ( > 0 !)
	/*
	 * Id (in [MIN_PC, MAX_PC]) del computer correntemente occupato dall'utente (o MIN_PC - 1
	 * se non ne occupa nessuno)
	 */
	private int currentPCNum; 
	
	public Studente(Laboratorio lab) {
		if (lab == null) throw new NullPointerException();
		this.lab = lab;
		this.k = ThreadLocalRandom.current().nextInt(Utente.MIN_K, Utente.MAX_K);
		this.currentPCNum = Laboratorio.MIN_PC - 1;
	}
	
	public int getK() { return k; }
	
	public boolean entra() {
		try {
			this.currentPCNum = this.lab.entraStudente();
			if (!Laboratorio.checkPCNum(this.currentPCNum)) return false; //entraStudente() è fallita
			return true;
		} catch (InterruptedException e) {
			return false;
		}
	}
	
	public boolean esci() {
		boolean b = this.lab.esciStudente(this.currentPCNum);
		if (b) this.currentPCNum = Laboratorio.MIN_PC - 1;
		return b;
	}	

	public String str() { return "Studente"; }
}