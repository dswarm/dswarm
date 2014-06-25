package org.dswarm.persistence;

import org.dswarm.init.DMPException;

/**
 * The exception class for DMP persistence exceptions.<br>
 */

public class DMPPersistenceException extends DMPException {

	/**
	 *
	 */
	private static final long	serialVersionUID	= 1L;

	/**
	 * Creates a new DMP persistence exception with the given exception message.
	 *
	 * @param exception the exception message
	 */
	public DMPPersistenceException(final String exception) {

		super(exception);
	}

}
