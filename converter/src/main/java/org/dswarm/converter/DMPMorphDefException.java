package org.dswarm.converter;

/**
 * A specialised exception class for indicating Metamorph definition failures
 */
public class DMPMorphDefException extends DMPConverterException {

	public DMPMorphDefException(final String exception) {
		super(exception);
	}

	public DMPMorphDefException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
