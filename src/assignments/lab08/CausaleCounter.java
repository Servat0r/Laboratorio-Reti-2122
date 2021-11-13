package assignments.lab08;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import com.google.gson.*;
import com.google.gson.stream.*;

import util.common.Common;
import util.threads.ThreadPoolUtils;

public final class CausaleCounter {
	
	private CausaleCounter() {}
	
	private static final int COUNT_TERM_DELAY = 10000;
		
	public static Map<Causale, Integer> count(String filename) throws Exception {
		Common.notNull(filename);
		Map<Causale, Integer> counter = new HashMap<>();
		for (Causale c : Causale.values()) counter.put(c, 0);
		ExecutorService pool = new ThreadPoolExecutor(
			4, //ThreadPoolUtils.getProcNum(),
			4, //ThreadPoolUtils.getProcNum(),
			0,
			TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<Runnable>(),
			new ThreadPoolExecutor.AbortPolicy()
		);
		/* Lettura del file */
		Gson gson = new Gson();
		FileInputStream fin = new FileInputStream(filename);
		JsonReader reader = new JsonReader(new InputStreamReader(fin));
		reader.beginArray();
		while (reader.hasNext()) {
			BankAccount account = gson.fromJson(reader, BankAccount.class);
			try {
				pool.execute(new AccountCausaleCounter(counter, account));
			} catch (RejectedExecutionException ree) {
				System.out.println("Rejected Execution");
				break;
			}
		}
		reader.endArray();
		ThreadPoolUtils.shutdown(pool, COUNT_TERM_DELAY);
		reader.close();
		fin.close();
		return counter;
	}
}