package assignments.lab03;

import java.util.concurrent.ThreadLocalRandom;

public final class Professore extends Utente {
	private final Laboratorio lab;
	private final int k; //Numero di accessi che l'utente deve compiere ( > 0 !)
	
	public Professore(Laboratorio lab) {
		if (lab == null) throw new NullPointerException();
		this.lab = lab;
		this.k = ThreadLocalRandom.current().nextInt(Utente.MIN_K, Utente.MAX_K);
	}

	public boolean entra() {
		try {
			this.lab.entraProfessore();
			return true;
		} catch (InterruptedException e) { return false; }
	}
	
	public boolean esci() {
		return this.lab.esciProfessore();
	}
	
	public int getK() { return k; }
	
	public String str() { return "Professore"; }
}