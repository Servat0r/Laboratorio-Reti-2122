package assignments.lab03;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public final class Main {

	/*
	 * Assumiamo che l'input sia della forma:
	 * <nome_programma> S T P, dove S = #studenti, T = #tesisti, P = #professori e S,T,P >= 0.
	 */
	public static void main(String[] args) {
		int S, T, P;
		Laboratorio lab;
		if (args.length != 3) {
			System.err.println("Uso: <nome_programma> S T P, dove S = #studenti, T = #tesisti, P = #professori");
			System.exit(1);
		}
		S = Integer.parseInt(args[0]);
		T = Integer.parseInt(args[1]);
		P = Integer.parseInt(args[2]);
		if ( (S < 0) || (T < 0) || (P < 0) ) {
			System.err.println("S, T, P devono essere maggiori o uguali a zero!");
			System.exit(1);
		}
		lab = new Laboratorio();
		List<Thread> utenti = new ArrayList<>();
		
		//Aggiungiamo prima tutti gli studenti ...
		for (int i = 0; i < S; i++) { utenti.add(new Thread(new Studente(lab))); }
		
		//... poi tutti i tesisti ...
		for (int i = 0; i < T; i++) {
			utenti.add(new Thread(
						new Tesista(
						lab,
						ThreadLocalRandom.current().nextInt(Laboratorio.MIN_PC, Laboratorio.MAX_PC)
						)
					)
			);
		}
		
		//... e infine tutti i professori
		for (int i = 0; i < P; i++) { utenti.add(new Thread(new Professore(lab))); }
		
		for (Thread t : utenti) { t.start(); }
	}
}