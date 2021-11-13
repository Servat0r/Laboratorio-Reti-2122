package assignments.lab08;

import java.util.*;

public final class Main {

	private static String filename = "bank_accounts.txt";
	
	//TODO Modificare in seguito per randomizzare
	private static int MAX_TRANSFERS = 10;
	private static String[] clients = {"A", "B", "C", "D", "E", "F"};

		
	@SuppressWarnings("deprecation")
	private static Date[] dates = {new Date(119, 1, 2), new Date(120, 3, 4), new Date(121, 5, 6)};
	
	private static Causale[] causales = {Causale.BOLLETTINO, Causale.ACCREDITO, Causale.BONIFICO, Causale.F24, Causale.PAGOBANCOMAT};
	
	public static void main(String[] args) {
		/* Creazione della banca e del file JSON con NIO */
		Random r = new Random();
		Random s = new Random();
		Bank bank = new Bank();
		for (String client : clients) {
			bank.addUser(client);
			int ntr = r.nextInt(MAX_TRANSFERS);
			for (int i = 0; i < ntr; i++) {
				bank.addTransfer(client, dates[s.nextInt(dates.length)], causales[s.nextInt(causales.length)]);
			}
		}
		try {
			bank.printToFile(filename);
			Map<Causale, Integer> counter = CausaleCounter.count(filename);
			System.out.println("RISULTATO:");
			for (Causale c : counter.keySet()) {
				System.out.printf("\t%s : %d occorrenze%n", c.toString(), counter.get(c));
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}