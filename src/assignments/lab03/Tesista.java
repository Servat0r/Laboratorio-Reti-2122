package assignments.lab03;

import java.util.concurrent.ThreadLocalRandom;

public final class Tesista extends Utente {
	private final Laboratorio lab;
	private int pcNum; //ID del pc che serve al tesista (in [MIN_PC, MAX_PC])
	private final int k; //Numero di accessi che l'utente deve compiere ( > 0 !)
	
	public Tesista(Laboratorio lab, int pcNum) {
		if (lab == null) throw new NullPointerException();
		if ( !Laboratorio.checkPCNum(pcNum) ) throw new IllegalArgumentException();
		this.lab = lab;
		this.pcNum = pcNum;
		this.k = ThreadLocalRandom.current().nextInt(Utente.MIN_K, Utente.MAX_K);
	}
	
	public int getK() { return k; }

	public boolean entra() {
		try {
			return this.lab.entraTesista(this.pcNum);
		} catch (InterruptedException e) { return false; }
	}
	
	public boolean esci() {
		return this.lab.esciTesista(this.pcNum);
	}
	
	public String str() { return "Tesista"; }
}