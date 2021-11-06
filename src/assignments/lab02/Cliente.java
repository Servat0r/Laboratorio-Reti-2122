package assignments.lab02;

import java.util.concurrent.ThreadLocalRandom;

import util.threads.ThreadUtils;

public final class Cliente implements Runnable {
	
	private static final int MAX_TIME = 1000; /* Massimo tempo di permanenza di un cliente a uno sportello */
	private final int id; /* Identificatore del cliente */
	
	public Cliente(int id) {
		this.id = id;
	}
	
	public final int getId() {
		return id;
	}

	public void run() {
		System.out.printf("Cliente [%d] comincia%n", this.id);
		ThreadUtils.Sleep(ThreadLocalRandom.current().nextInt(MAX_TIME));
		System.out.printf("Cliente [%d] finisce%n", this.id);
	}
}