package de.avgl.dmp.persistence;

/**
 * The exception class for DMP persistence exceptions.<br>
 * 
 */

public class DMPPersistenceException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new DMP persistence exception with the given exception message.
	 * 
	 * @param exception the exception message
	 */
	public DMPPersistenceException(final String exception) {

		super(exception);
	}

}

