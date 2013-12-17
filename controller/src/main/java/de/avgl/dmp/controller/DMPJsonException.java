package de.avgl.dmp.controller;

/**
 * User: knut
 * Date: 12/12/13
 * Time: 1:36 PM
 */
public class DMPJsonException extends DMPControllerException {

	/**
	 * Creates a new DMP exception with the given exception message
	 * and a cause.
	 *
	 * @param message the exception message
	 * @param cause   a previously thrown exception, causing this one
	 */
	public DMPJsonException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
