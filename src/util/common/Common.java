package util.common;

import java.util.*;

/**
 * Operazioni e metodi comuni.
 * @author Salvatore Correnti.
 */
public final class Common {
	
	private Common() {}
	
	public static void notNull(Object obj) { if (obj == null) throw new NullPointerException(); }
	
	public static Integer[] newIntegerArray(int length, int defValue) {
		if (length <= 0) throw new IllegalArgumentException(); 
		Integer[] result = new Integer[length];
		for (int i = 0; i < length; i++) result[i] = defValue;
		return result;
	}
	
	/**
	 * Crea una nuova HashMap da una coppia di array della stessa lunghezza associando per ogni i l'i-esimo elemento del primo array all'i-esimo
	 * elemento del secondo.
	 * @param <K> Tipo delle chiavi della HashMap.
	 * @param <V> Tipo dei valori della HashMap.
	 * @param keys Chiavi della HashMap.
	 * @param values Valori della HashMap.
	 * @return Una HashMap come già descritta in caso di successo, null altrimenti.
	 */
	public static <K,V> Map<K, V> newHashMapFromArrays(K[] keys, V[] values){
		if (keys.length != values.length) return null;
		int size = keys.length;
		Map<K,V> map = new HashMap<>();
		for (int i = 0; i < size; i++) map.put(keys[i], values[i]);
		return map;
	}
}