package org.dswarm.converter.adapt;

public class JsonModelTransformException extends Exception {

	private static final long serialVersionUID = -2598044985016356394L;

	public JsonModelTransformException() {
		super();
	}

	
	public JsonModelTransformException( String message ) {
		super( message );
	}
	
	
	public JsonModelTransformException( Throwable throwable ) {
		super( throwable );
	}
	
}
