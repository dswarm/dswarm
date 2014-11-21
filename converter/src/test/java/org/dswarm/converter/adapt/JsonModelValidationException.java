package org.dswarm.converter.adapt;

public class JsonModelValidationException extends Exception {

	private static final long serialVersionUID = -7802345789736329818L;

	public JsonModelValidationException() {
		super();
	}

	public JsonModelValidationException(final Throwable throwable) {
		super(throwable);
	}

	public JsonModelValidationException(final String message, final Throwable t) {
		super(message, t);
	}

}
