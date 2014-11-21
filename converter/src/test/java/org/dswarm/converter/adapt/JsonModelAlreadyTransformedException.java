package org.dswarm.converter.adapt;

public class JsonModelAlreadyTransformedException extends RuntimeException {

	private static final long serialVersionUID = -2598044985016356394L;

	public JsonModelAlreadyTransformedException() {
		super();
	}

	
	public JsonModelAlreadyTransformedException( String message ) {
		super( message );
	}
	
	
	public JsonModelAlreadyTransformedException( Throwable throwable ) {
		super( throwable );
	}
	
}
