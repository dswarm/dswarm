package de.avgl.dmp.converter;

/**
 * A specialised exception class for indicating Metamorph definition failures
 */
public class DMPMorphDefException extends DMPConverterException {
	public DMPMorphDefException(String exception) {
		super(exception);
	}

	public DMPMorphDefException(String message, Throwable cause) {
		super(message, cause);
	}
}
