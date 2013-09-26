package de.avgl.dmp.init;

/**
 * The exception class for DMP exceptions.<br>
 * 
 */

public class DMPException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new DMP exception with the given exception message.
	 * 
	 * @param exception the exception message
	 */
	public DMPException(final String exception) {

		super(exception);
	}

}
