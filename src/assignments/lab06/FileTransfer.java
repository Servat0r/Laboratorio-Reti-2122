package assignments.lab06;

import java.io.*;
import java.net.*;
import java.util.Scanner;

import util.http.*;

public final class FileTransfer implements Runnable {
	
	private final Socket connection; /* Socket di connessione col client. */
	private InputStream in; /* InputStream da cui leggere la richiesta. */
	private OutputStream out; /* OutputStream su cui scrivere al risposta. */
	private final String rootDirectory; /* Root directory in cui cercare i file da inviare ai client. */

	/**
	 * Utility per settare opportunamente i campi fondamentali (code, version, body, header "Connection")
	 * del messaggio di risposta.
	 * @param code Codice di risposta.
	 * @param version Versione HTTP.
	 * @param conn Se != null, valore a cui settare l'header "Connection".
	 * @param body Se != null, valore da assegnare al corpo del messaggio.
	 * @return Un oggetto HttpResponse con i campi opportunamente assegnati.
	 */
	private static HttpResponse makeResponse(int code, String version, String conn, byte[] body) {
		HttpResponse httpRes = new HttpResponse(code, version, body);
		httpRes.setHeader("Server", "localhost");
		if (conn != null) httpRes.setHeader("Connection", conn);
		return httpRes;
	}
		
	private void send(HttpResponse response) {
		try { response.send(this.out); }
		catch (Exception e) {
			System.err.println("WORKERS POOL: Error when sending response");
			System.exit(1);
		}
	}
	
	private void close(HttpResponse response) {
		try { response.close(); }
		catch (Exception e) {
			System.out.println("WORKERS POOL: Exception thrown when closing output stream");
			e.printStackTrace();
			System.exit(1);			
		}
	}
	
	public FileTransfer(Socket connection, String rootDirectory) throws IOException {
		if (connection == null || rootDirectory == null) throw new NullPointerException();
		this.connection = connection;
		this.in = connection.getInputStream();
		this.out = connection.getOutputStream();
		this.rootDirectory = rootDirectory;
	}
	
	/**
	 * Legge la richiesta inviata dal client, recupera il file corrispondente (se presente) e invia il messaggio
	 * di risposta al client.
	 */
	public void run() {
		HttpResponse httpRes = null;
		StringBuilder sb = new StringBuilder();
		boolean doubleCRLF = false; /* Flag per indicare l'uscita dal while dopo aver incontrato un doppio CRLF
		(se NON avviene certamente la richiesta HTTP NON è in un formato valido!) */
		try (
			this.connection;
			Scanner sc = new Scanner(this.in).useDelimiter(Http.LINE_SEPARATOR);
		){
			/* Recupero della richiesta dallo stream di input */
			while (sc.hasNext()) {
				String next = sc.next();
				if (next.length() == 0) {
					sb.append(Http.LINE_SEPARATOR);
					doubleCRLF = true;
					break;
				} else sb.append(next + Http.LINE_SEPARATOR);
			}
			if (!doubleCRLF) throw new InvalidHttpRequestException();
			//while (this.in.available() > 0) { sb.append((char)this.in.read()); }
			String request = sb.toString();
			System.out.printf("Accepted new connection from '%s'%nRECEIVED REQUEST:%n%s",
				connection.getRemoteSocketAddress(), request);
			HttpRequest httpReq = new HttpRequest(request);
			if (!httpReq.getMethod().equals("GET")) { /* La richiesta non è di tipo "GET" */
				httpRes = makeResponse(Http.BAD_REQUEST, Http.HTTP11, Http.CONN_CLOSE, "Error 400: bad request".getBytes());
				this.send(httpRes);
			} else {
				String filename = httpReq.getPath();
				File file = new File(this.rootDirectory + filename);
				if (file.isDirectory()) { /* Non è stato richiesto un file regolare */
					httpRes = makeResponse(Http.FORBIDDEN, Http.HTTP11, Http.CONN_CLOSE, "Error 403: resource access denied".getBytes());
					this.send(httpRes);
				} else {
					try {
						String contentLength = Long.toString(file.length());
						String contentType = URLConnection.guessContentTypeFromName(filename);
						if (contentType == null) { /* Non è possibile determinare il MIME-type */
							httpRes = makeResponse(Http.INT_SERVER_ERROR, Http.HTTP11, Http.CONN_CLOSE, null);
						} else { /* Tutto ok */
							httpRes = new HttpResponse(Http.OK, Http.HTTP11, new FileInputStream(file));
							httpRes.setHeader("Server", "localhost");
							httpRes.setHeader("Connection", Http.CONN_CLOSE);
							httpRes.setHeader("Content-Type", contentType);
							httpRes.setHeader("Content-Length", contentLength);
						}
						this.send(httpRes);
					} catch (FileNotFoundException fnfe) { /* File non esistente; generata dal costruttore di fstream */
						httpRes = makeResponse(Http.NOT_FOUND, Http.HTTP11, Http.CONN_CLOSE, "Error 404: resource not found".getBytes());
						this.send(httpRes);
					}
				}
			}
			this.close(httpRes);
		} catch (InvalidHttpRequestException ihre) { /* Generata dai metodi di HttpRequest */
			httpRes = makeResponse(Http.BAD_REQUEST, Http.HTTP11, Http.CONN_CLOSE, "Error 400: bad request".getBytes());
			this.send(httpRes);
			this.close(httpRes);
		} catch (Exception e) { /* Altre eccezioni */
			System.out.println("WORKERS POOL: Exception thrown when handling request");
			e.printStackTrace();
			System.exit(1);
		}
	}
}