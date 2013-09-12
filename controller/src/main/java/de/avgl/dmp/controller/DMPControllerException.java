package de.avgl.dmp.controller;

import de.avgl.dmp.init.DMPException;

/**
 * The exception class for DMP controller exceptions.<br>
 * 
 */

public class DMPControllerException extends DMPException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new DMP controller exception with the given exception message.
	 * 
	 * @param exception the exception message
	 */
	public DMPControllerException(final String exception) {

		super(exception);
	}

}
