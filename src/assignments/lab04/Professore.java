package assignments.lab04;

import java.util.concurrent.ThreadLocalRandom;

public final class Professore extends Utente {
	private final Laboratorio lab;
	private final int k; //Numero di accessi che l'utente deve compiere ( > 0 !)
	private final int ID; //Campo statico per assegnare un ID a ogni professore
	
	public Professore(Laboratorio lab) {
		if (lab == null) throw new NullPointerException();
		this.ID = Utente.nextId();
		this.lab = lab;
		this.k = ThreadLocalRandom.current().nextInt(Utente.MIN_K, Utente.MAX_K);
	}
	
	public boolean entra() {
		try { return this.lab.entraProfessore(); }
		catch (InterruptedException e) { return false; }
	}
	
	public boolean esci() { return this.lab.esciProfessore(); }
	
	public int getK() { return k; }
	
	public String str() { return "Professore"; }

	public int getID() { return this.ID; }
}