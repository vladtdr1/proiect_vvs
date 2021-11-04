package main.java.exceptions;

public class FileAccessPermissionException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public FileAccessPermissionException() {
		super("Could not read contents of file.");
	}

}
