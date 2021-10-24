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
		HttpResponse httpRes = new HttpResponse();
		httpRes.setCode(code);
		httpRes.setVersion(version);
		httpRes.setHeader("Server", "localhost");
		if (conn != null) httpRes.setHeader("Connection", conn);
		if (body != null) httpRes.setBody(body);
		return httpRes;
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
		HttpResponse httpRes;
		StringBuilder sb = new StringBuilder();
		boolean doubleCRLF = false; /* Flag per indicare l'uscita dal while dopo aver incontrato un doppio CRLF
		(se NON avviene certamente la richiesta HTTP NON è in un formato valido!) */
		try(
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
			String request = sb.toString();
			synchronized (System.out) {
				System.out.printf("Accepted new connection from '%s'%n", connection.getRemoteSocketAddress());
				System.out.println("RECEIVED REQUEST:\n" + request);
			}
			
			HttpRequest httpReq = new HttpRequest(request);
			if (!httpReq.getMethod().equals("GET")) { /* La richiesta non è di tipo "GET" */
				httpRes = makeResponse(Http.BAD_REQUEST, Http.HTTP10, "close", "Error 400: bad request".getBytes());
			} else {
				String filename = httpReq.getPath();
				File file = new File(this.rootDirectory + filename);
				if (!file.isFile()) { /* Non è stato richiesto un file regolare */
					httpRes = makeResponse(Http.FORBIDDEN, Http.HTTP10, "close", "Error 403: resource access denied".getBytes());
				} else {
					try (FileInputStream fstream = new FileInputStream(file)) {
						String contentLength = Long.toString(file.length());
						String contentType = URLConnection.guessContentTypeFromName(filename);
						int length = (int)file.length();
						byte[] fcontent = new byte[length];
						if (contentType == null) { /* Non è possibile determinare il MIME-type */
							httpRes = makeResponse(Http.INT_SERVER_ERROR, Http.HTTP10, "close", null);
						} else if (file.length() > Integer.MAX_VALUE) {
							/* Il file è troppo lungo per allocare un byte-array di questa dimensione */
							httpRes = makeResponse(Http.INT_SERVER_ERROR, Http.HTTP10, "close", null);
						} else if (fstream.read(fcontent) != length) { /* Lettura scorretta dallo stream di input */
								httpRes = makeResponse(Http.INT_SERVER_ERROR, Http.HTTP10,
										"close", "Error 500: internal server error".getBytes());
						} else { /* Tutto ok */
							httpRes = makeResponse(Http.OK, Http.HTTP10, "close", fcontent);
							httpRes.setHeader("Content-Type", contentType);
							httpRes.setHeader("Content-Length", contentLength);
						}
					} catch (FileNotFoundException fnfe) { /* File non esistente; generata dal costruttore di fstream */
						httpRes = makeResponse(Http.NOT_FOUND, Http.HTTP10, "close", "Error 404: resource not found".getBytes());
					} catch (IOException ioe) { /* Generata da fstream.read(fcontent) */
						httpRes = makeResponse(Http.INT_SERVER_ERROR, Http.HTTP10, "close", "Error 500: internal server error".getBytes());
					}
				}
			}
			
			byte[] response = httpRes.getByteResponse();
			this.out.write(response); //Can throw IOException => fatal error, since we CANNOT communicate with client!
		} catch (InvalidHttpRequestException ihre) { //Generata dal costruttore di httpReq o da readRequest()
			httpRes = makeResponse(Http.BAD_REQUEST, Http.HTTP10, "close", "Error 400: bad request".getBytes());
		} catch (Exception e) {
			System.out.printf("WORKERS POOL: Exception thrown when handling request%n");
			e.printStackTrace();
			System.exit(1);
		}
	}
}