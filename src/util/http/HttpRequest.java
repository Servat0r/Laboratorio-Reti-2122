package util.http;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Rappresentazione di una richiesta in protocollo HTTP.
 * WARNING: La seguente implementazione è INCOMPLETA ed è funzionante SOLO ai fini dell'assignment. In particolare,
 * per semplificare la realizzazione, sono state fatte le seguenti assunzioni:
 * 	1. le richieste HTTP NON prevedono un message-body, in modo da poter identificare la fine di una richiesta con il
 * 	doppio "\r\n" alla fine della linea di stato + headers lines;
 * 	2. NON si effettua NESSUN controllo su quali header siano mandati, i.e. qualunque stringa della forma:
 * 	S1 + ": " + S2 + "\r\n" con ":", " ", "\r\n" non appartenenti a S1 e "\r\n" non appartenente a S2 è considerata
 * 	una header-line corretta. Questa semplificazione è necessaria per non complicare ulteriormente la classe.
 * @author Salvatore Correnti
 */
public final class HttpRequest {
	
	private String method; /* Metodo della richiesta */
	private String version; /* Versione HTTP (0.9 / 1.0 / 1.1) */
	private String path; /* Path della richiesta (NON comincia con '/')*/
	private Map<String, String> headers; /* Coppie <header_name, header_value> */
	
	/**
	 * Costruisce un oggetto HttpRequest partendo da una stringa HTTP-compliant.
	 * @param request Stringa che contiene la richiesta da parsare.
	 * @throws InvalidHttpRequestException Se la stringa non rappresenta una richiesta HTTP corretta.
	 */
	public HttpRequest(String request) throws InvalidHttpRequestException {
		if (request == null) throw new NullPointerException();
		boolean stateParsed = false;
		String currentLine;
		String token;
		this.headers = new HashMap<String, String>();
		try ( Scanner scanner = new Scanner(request).useDelimiter(Http.LINE_SEPARATOR); ){
			while (scanner.hasNext()) {
				currentLine = scanner.next();
				if (currentLine.length() == 0) break; //Doppio CRLF => fine della richiesta!
				if (!stateParsed) {
					/* Parsing della linea di stato */
					try ( Scanner stateLineScanner = new Scanner(currentLine).useDelimiter(Http.STATE_LINE_SEPARATOR); ){
						token = stateLineScanner.next();
						if (Http.checkMethod(token)) this.method = new String(token);
						else throw new InvalidHttpRequestException("Invalid method");
						token = stateLineScanner.next();
						this.path = new String(token);
						token = stateLineScanner.next();
						if (Http.checkVersion(token)) this.version = new String(token);
						else throw new InvalidHttpRequestException("Invalid version");
					}
					stateParsed = true;
				} else {
					/* Parsing di una header-line */
					try ( Scanner headerLineScanner = new Scanner(currentLine).useDelimiter(Http.HEADER_NAME_VALUE_SEPARATOR); ){
						token = headerLineScanner.next();
						if (Http.checkHeaderName(token, this.headers)) {
							this.headers.put(new String(token), new String(headerLineScanner.nextLine())); //Inserisce la coppia <token, resto della riga>
						} else throw new InvalidHttpRequestException("Header line repeated or invalid");
					}
				}
			}
		} catch (NoSuchElementException nsee) { throw new InvalidHttpRequestException("Request ended prematurely"); }
	}
	
	/**
	 * Restituisce il valore associato allo header passato se presente, null altrimenti.
	 * @param header Header name.
	 * @return La stringa corrispondente al valore dell'header richiesto se tale valore è definito, null altrimenti.
	 * @throws IllegalArgumentException se header non è un header http supportato.
	 */
	public String getHeaderValue(String header) {
		if (header == null) throw new NullPointerException();
		if (!this.headers.containsKey(header)) return null;
		return new String(this.headers.get(header));
	}
	
	/**
	 * Modifica il valore associato a uno header già presente nella richiesta.
	 * @param header Header name.
	 * @param value Nuovo valore.
	 * @return true se lo header e il suo valore erano già presenti nella hashmap, false altrimenti.
	 */
	public boolean setHeaderValue(String header, String value) {
		if (header == null) throw new NullPointerException();
		if (!this.headers.containsKey(header)) return false;
		this.headers.put(header, value);
		return true;
	}
	
	public final String getMethod() { return new String(method); }

	public final String getVersion() { return new String(version); }

	public final String getPath() { return new String(path); }

	/**
	 * @return Una copia della hashmap che contiene le coppie <nome header, valore header>.
	 */
	public final Map<String, String> getHeaders() {
		Map<String, String> hcopy = new HashMap<>();
		for (String s : hcopy.keySet()) hcopy.put(new String(s), new String(this.headers.get(s)));
		return hcopy;
	}
	
	/**
	 * @return Una rappresentazione della richiesta HTTP.
	 * NOTA: La stringa restituita potrebbe NON corrispondere con quella parsata per costruire
	 * l'oggetto HttpRequest corrente, in quanto dipendente dall'iteratore this.headers.keySet().
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.method + Http.STATE_LINE_SEPARATOR + this.path + Http.STATE_LINE_SEPARATOR
				+ this.version + Http.LINE_SEPARATOR);
		for (String s : this.headers.keySet()) {
			sb.append(new String(s) + new String(this.headers.get(s)) + Http.LINE_SEPARATOR);
		}
		sb.append(Http.LINE_SEPARATOR);
		return sb.toString();
	}
}