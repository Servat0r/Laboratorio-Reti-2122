package util.common;

public final class Common {

	private Common() {}
	
	public static void notNull(Object obj) { if (obj == null) throw new NullPointerException(); }
}