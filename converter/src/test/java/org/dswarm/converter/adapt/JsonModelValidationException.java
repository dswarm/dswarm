package org.dswarm.converter.adapt;

public class JsonModelValidationException extends Exception {

	private static final long serialVersionUID = -7802345789736329818L;

	public JsonModelValidationException() {
		super();
	}
	
	public JsonModelValidationException( Throwable throwable ) {
		super( throwable );
	}
	
	public JsonModelValidationException( String message, Throwable t ) {
		super( message, t );
	}
	
}
