package assignments.lab08;

import java.util.Date;

import util.common.Common;

public final class Transfer {
	private final Date date;
	private final Causale causale;
	
	public Transfer(Date date, Causale causale) {
		Common.notNull(date); Common.notNull(causale);
		this.date = date;
		this.causale = causale;
	}

	public final Date getDate() {
		return date;
	}

	public final Causale getCausale() {
		return causale;
	}
	
	public String toString() {
		return "Transfer[" + this.date.toString() + " : " + this.causale.toString() + "]";
	}
}