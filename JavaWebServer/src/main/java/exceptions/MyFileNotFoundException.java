package main.java.exceptions;

public class MyFileNotFoundException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public MyFileNotFoundException() {
		super("Could not find the specific file in the path.");
	}

}
