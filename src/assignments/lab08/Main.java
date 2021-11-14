package assignments.lab08;

import java.util.*;

public final class Main {

	private static final String DFL_FNAME = "bank_accounts.txt";
	
	private static final int MAX_TRANSFERS = 10000;
	private static final int MIN_TRANSFERS = 100;
	private static final String[] clients = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N"};

	private static final int MIN_YEAR = 2019;
	private static final int MAX_YEAR = 2021;
	private static final int MAX_MONTH = 11;
	
	private static final List<Integer> MONTH_30 = Arrays.asList(3, 5, 8, 10);
	private static final List<Integer> MONTH_31 = Arrays.asList(0, 2, 4, 6, 7, 9, 11);
	
	private static final int HOURS = 24;
	private static final int MINS_SECS = 60;
	
	private static final Random R_DATE = new Random(System.currentTimeMillis());
	
	private static Date nextDate() {
		int year = MIN_YEAR + R_DATE.nextInt(1 + MAX_YEAR - MIN_YEAR); //[2019-2021]
		int month = R_DATE.nextInt(MAX_MONTH);
		int day;
		if (MONTH_30.contains(month)) day = R_DATE.nextInt(30);
		else if (MONTH_31.contains(month)) day = R_DATE.nextInt(31);
		else if ((year % 400 == 0) || ( year % 100 != 0 && year % 4 == 0)) day = R_DATE.nextInt(29);
		else day = R_DATE.nextInt(28);
		Calendar c = Calendar.getInstance();
		c.set(year, month, day, R_DATE.nextInt(HOURS), R_DATE.nextInt(MINS_SECS), R_DATE.nextInt(MINS_SECS));
		return c.getTime();
	}
		
	public static void main(String[] args) {
		/* Creazione della banca e del file JSON con NIO */
		Random r = new Random();
		Random s = new Random();
		Bank bank = new Bank();
		String filename = (args.length > 0 ? args[0] : DFL_FNAME);
		Causale[] causals = Causale.values();
		for (String client : clients) {
			bank.addUser(client);
			int ntr = MIN_TRANSFERS + r.nextInt(MAX_TRANSFERS - MIN_TRANSFERS + 1);
			for (int i = 0; i < ntr; i++) {
				bank.addTransfer(client, Main.nextDate(),causals[s.nextInt(causals.length)]);
			}
		}
		try {
			bank.printToFile_NIO(filename);
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