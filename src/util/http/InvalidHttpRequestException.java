package util.http;

public class InvalidHttpRequestException extends Exception {

	private static final long serialVersionUID = 1L;

	public InvalidHttpRequestException(String message) { super(message); }
	
	public InvalidHttpRequestException() { super(); }
	
}