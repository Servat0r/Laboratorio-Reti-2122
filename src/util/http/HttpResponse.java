package util.http;

import java.io.*;
import java.util.*;

/**
 * Classe che rappresenta un messaggio di risposta in protocollo HTTP.
 * WARNING: Come in {@link HttpRequest}, la seguente implementazione è INCOMPLETA ed è funzionante SOLO ai fini
 * dell'assignment, e contiene le stesse assunzioni semplificative sugli headers, mentre a differenza di HttpRequest
 * consente ai messaggi di risposta di avere un corpo.
 * @author Salvatore Correnti
 */
public final class HttpResponse implements AutoCloseable {
	
	public static final int DFL_BUF_LENGTH = 1024;
	
	private String version; /* Versione HTTP del messaggio. */
	private int code; /* Codice di risposta. */
	private String message; /* Messaggio associato al codice. */
	private Map<String, String> headers; /* HashMap di coppie <nome header, valore header>. */
	private InputStream body; /* Corpo del messaggio, espresso in raw bytes. */
	
	public HttpResponse(int code, String version, byte[] body) {
		this.setVersion(version);
		this.setCode(code);
		this.headers = new HashMap<String, String>();
		this.body = new ByteArrayInputStream((body != null ? body : new byte[0]));
	}
	
	public HttpResponse(int code, String version, InputStream body) {
		this.setVersion(version);
		this.setCode(code);
		this.headers = new HashMap<String, String>();
		this.body = body;
	}
	
	public final String getVersion() { return new String(version); }
	
	public final void setVersion(String version) {
		if (!Http.checkVersion(version)) throw new IllegalArgumentException();
		this.version = version;
	}
	
	public final int getCode() { return code; }
	
	/**
	 * Setta il valore di code a quello fornito, se valido, e assegna a message il valore del messaggio
	 * di risposta corrispondente.
	 * @param code Codice di risposta.
	 */
	public final void setCode(int code) {
		if (!Http.checkCode(code)) throw new IllegalArgumentException();
		this.code = code;
		this.message = Http.getCodeMessage(code);
	}
	
	public final String getMessage() { return new String(message);	}
	
	public final Map<String, String> getHeaders() {
		Map<String, String> hcopy = new HashMap<>();
		for (String s : this.headers.keySet()) hcopy.put(new String(s), new String(this.headers.get(s)));
		return hcopy;
	}
	
	public final void setHeader(String header, String value) {
		if (header == null || value == null) throw new NullPointerException();
		if (!Http.checkHeaderName(header, this.headers)) throw new IllegalArgumentException();
		this.headers.put(header, value);
	}
	
	public final String getHeader(String header) {
		if (!this.headers.containsKey(header)) return null;
		return new String(this.headers.get(header));
	}
	
	public InputStream getBody() { return this.body; }
		
	public void send(OutputStream out) throws IOException {
		if (out == null) throw new NullPointerException();
		StringBuilder sb = new StringBuilder();
		byte[] content = new byte[DFL_BUF_LENGTH];
		/* Linea di stato */
		sb.append(this.version + Http.STATE_LINE_SEPARATOR + this.code
				+ Http.STATE_LINE_SEPARATOR + this.message
				+ Http.LINE_SEPARATOR);
		/* Linee di intestazione */
		for (String h : this.headers.keySet()) {
			sb.append(h + Http.HEADER_NAME_VALUE_SEPARATOR
					+ this.headers.get(h)
					+ Http.LINE_SEPARATOR);
		}
		sb.append(Http.LINE_SEPARATOR);
		out.write(sb.toString().getBytes());
		if (this.body != null) {
			int c = 0;
			while (true) {
				c = this.body.read(content, 0, DFL_BUF_LENGTH);
				if (c == -1) break;
				out.write(content, 0, c);
			}
		}
	}
	
	public void close() throws Exception {
		if (this.body != null){
			this.body.close();
			this.body = null;
		}
	}
}