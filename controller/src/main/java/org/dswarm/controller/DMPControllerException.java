package org.dswarm.controller;

import org.dswarm.init.DMPException;

/**
 * The exception class for DMP controller exceptions.<br>
 */

public class DMPControllerException extends DMPException {

	/**
	 *
	 */
	private static final long	serialVersionUID	= 1L;

	/**
	 * Creates a new DMP controller exception with the given exception message.
	 * 
	 * @param exception the exception message
	 */
	public DMPControllerException(final String exception) {

		super(exception);
	}

	/**
	 * Creates a new DMP exception with the given exception message and a cause.
	 * 
	 * @param message the exception message
	 * @param cause a previously thrown exception, causing this one
	 */
	public DMPControllerException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
