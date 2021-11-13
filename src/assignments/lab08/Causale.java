package assignments.lab08;

public enum Causale {
	BONIFICO,
	ACCREDITO,
	BOLLETTINO,
	F24,
	PAGOBANCOMAT;
	
	public String toString() {
		if (this == BONIFICO) return "BONIFICO";
		else if (this == ACCREDITO) return "ACCREDITO";
		else if (this == BOLLETTINO) return "BOLLETTINO";
		else if (this == F24) return "F24";
		else if (this == PAGOBANCOMAT) return "PAGOBANCOMAT";
		else return null;
	}
}