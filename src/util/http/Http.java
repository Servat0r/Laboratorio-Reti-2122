package util.http;

import java.util.Map;

/**
 * Insieme di campi e metodi statici comuni per scambiare messaggi HTTP-compliant.
 * @author Salvatore Correnti
 */
public class Http {
	
	private Http() { }

	/* Costanti per codici di risposta comuni. */
	public static final int OK = 200;
	public static final int BAD_REQUEST = 400;
	public static final int FORBIDDEN = 403;
	public static final int NOT_FOUND = 404;
	public static final int INT_SERVER_ERROR = 500;
	
	/* Costanti per versioni comuni di HTTP. */
	public static final String HTTP09 = "HTTP/0.9";
	public static final String HTTP10 = "HTTP/1.0";
	public static final String HTTP11 = "HTTP/1.1";
	
	/* Metodi accettati. */
	public static final String[] METHODS = {
		"GET",
		"POST",
		"OPTIONS",
		"DELETE",
		"HEAD",
		"PUT",
		"TRACE",
	};
	
	/* Versioni di HTTP accettate. */
	public static final String[] VERSIONS = {
		HTTP09,
		HTTP10,
		HTTP11,
	};
	
	/* Codici di risposta definiti. */
	public static final int[] CODES = {
		OK,
		BAD_REQUEST,
		FORBIDDEN,
		NOT_FOUND,
		INT_SERVER_ERROR,
	};
	
	/* Messaggi di risposta definiti (per ogni i, l'i-esimo messaggio corrisponde all'i-esimo codice in CODES). */
	public static final String[] MESSAGES = {
		"OK",
		"Bad Request",
		"Forbidden",
		"Not Found",
		"Internal Server Error",
	};
	
	/* Separatore fra linee di un messaggio HTTP (CR+LF). */
	public static final String LINE_SEPARATOR = "\r\n";
	
	/* Separatore fra nome e valore di uno header (: ). */
	public static final String HEADER_NAME_VALUE_SEPARATOR = ": ";
	
	/* Separatore fra campi nella linea di stato ( ). */
	public static final String STATE_LINE_SEPARATOR = " ";
	
	
	/**
	 * Controlla che il metodo fornito sia valido, ovvero che sia presente in METHODS.
	 * @param method Stringa che rappresenta il metodo da controllare.
	 * @return true se il metodo è valido, false altrimenti.
	 */
	public static boolean checkMethod(String method) {
		for (String m : Http.METHODS) { if (m.equals(method)) return true; }
		return false;
	}
	
	/**
	 * Controlla che la versione fornita sia valida, ovvero che sia in VERSIONS.
	 * @param version Stringa che rappresenta la versione da controllare.
	 * @return true se la versione è valida, false altrimenti.
	 */
	public static boolean checkVersion(String version) {
		if (version == null) return false;
		for (String v : Http.VERSIONS) { if (version.equals(v)) return true; }
		return false;
	}
	
	/**
	 * Controlla che il codice fornito sia valido, ovvero che sia in CODES.
	 * @param code Intero che rappresenta il codice da controllare.
	 * @return true se il codice è valido, false altrimenti.
	 */
	public static boolean checkCode(int code) {
		for (int c : Http.CODES) { if (c == code) return true; }
		return false;
	}

	/**
	 * Controlla che l'header fornito sia valido e non già presente nella richiesta per evitare duplicati.
	 * @param header Stringa che rappresenta l'header da controllare.
	 * @param headers HashMap di coppie <headerName, headerValue> rappresentante gli headers già parsati. 
	 * @return true se l'header è corretto, false altrimenti.
	 */
	public static boolean checkHeaderName(String header, Map<String, String> headers) {
		if (header == null || headers == null) return false;
		if (headers.containsKey(header)) return false; //header duplicato nella richiesta!
		else return true;
	}

	/**
	 * Restituisce il messaggio di risposta corrispondente al codice fornito.
	 * @param code Codice di risposta.
	 * @return Il messaggio di risposta corrispondente a code se questi è un codice valido, null altrimenti.
	 */
	public static String getCodeMessage(int code) {
		if (!Http.checkCode(code)) return null;
		int index = 0;
		while (index < Http.CODES.length) {
			if (CODES[index] == code) break;
			else index++;
		}
		return new String(MESSAGES[index]);
	}
}