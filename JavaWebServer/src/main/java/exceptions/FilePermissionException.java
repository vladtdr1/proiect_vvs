package main.java.exceptions;

public class FilePermissionException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public FilePermissionException() {
		super("Could not find the specific file in the path.");
	}

}
