package de.avgl.dmp.converter;

import de.avgl.dmp.init.DMPException;

/**
 * The exception class for DMP converter exceptions.<br>
 * 
 */

public class DMPConverterException extends DMPException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new DMP converter exception with the given exception message.
	 * 
	 * @param exception the exception message
	 */
	public DMPConverterException(final String exception) {

		super(exception);
	}

}