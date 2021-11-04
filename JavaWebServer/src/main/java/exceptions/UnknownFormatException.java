package main.java.exceptions;

public class UnknownFormatException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public UnknownFormatException() {
		super("Could not find relative path in the received request");
	}

}
