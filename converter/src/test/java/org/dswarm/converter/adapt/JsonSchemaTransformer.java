package org.dswarm.converter.adapt;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonSchemaTransformer {

	public static final JsonSchemaTransformer INSTANCE = new JsonSchemaTransformer();
	
	private static final Logger log = LoggerFactory.getLogger( JsonSchemaTransformer.class );
	
	private final ObjectMapper mapper = new ObjectMapper();
	
	private static final String keyAttributePaths = "attribute_paths";
	private static final String keyAttributePath = "attribute_path";
	private static final String keySchema = "schema";
	private static final String keyInputDataModel = "input_data_model";
	private static final String keyOutputDataModel = "output_data_model";
	private static final String keyType = "type";
	private static final String keyName = "name";
	private static final String keyId = "id";
	
	private static final String valueSchemaAttributePathInstance = "SchemaAttributePathInstance";
	
	private int generatedId;
	
	/**
	 * 
	 * @param jsonContent The complete json content
	 * @return The rootNode
	 * @throws JsonModelTransformException
	 */
	public JsonNode transformFixAttributePathInstance( String jsonContent ) throws JsonModelTransformException, JsonModelAlreadyTransformedException {
		try {
			resetGeneratedId();
			
			JsonNode nodeRoot = objectifyJsonInput( jsonContent );	
			
			try {
				updateSchemaNode( nodeRoot.get( keyInputDataModel ) );
				updateSchemaNode( nodeRoot.get( keyOutputDataModel ) );
			} catch( JsonModelAlreadyTransformedException e ) {
				log.warn( e.getMessage() );
				throw e;
			} finally {
				logObjectJSON( nodeRoot );
				resetGeneratedId();				
			}
			
			return nodeRoot;
		} catch( IOException e ) {
			log.error( e.getMessage(), e );
			throw new JsonModelTransformException( e );
		}
	}
	
	
	private void resetGeneratedId() {
		generatedId = 0;
	}
	
	
	private void updateSchemaNode( JsonNode parent ) throws JsonModelAlreadyTransformedException {
		if( parent == null )
			return;
		
		ObjectNode nodeSchema = (ObjectNode)parent.get( keySchema );
		if( nodeSchema == null )
			return;
			
		//skip resource which are already transformed
		JsonNode valueForNodeType = nodeSchema.findValue( keyType );
		if( valueForNodeType != null ) 
			if( valueForNodeType.asText().equals(valueSchemaAttributePathInstance ) )
				throw new JsonModelAlreadyTransformedException( "Resource already transformed." );			
		
		ArrayNode oldAttributePaths = (ArrayNode)nodeSchema.get( keyAttributePaths );
		ArrayNode newAttributePaths = mapper.createArrayNode();
		for( JsonNode oldAttributePath : oldAttributePaths ) {
			int id = generateId();
			JsonNode attributePathInstance = createSchemaAttributePathInstanceNode( id, "sapi_" + id, oldAttributePath );
			newAttributePaths.add( attributePathInstance );
		}
		nodeSchema.replace( keyAttributePaths, newAttributePaths );
	}
	
	
	private int generateId() {
		return generatedId++;
	}
	
	
	private JsonNode createSchemaAttributePathInstanceNode( int id, String name, JsonNode attributePath ) {
		ObjectNode node = mapper.createObjectNode();
		node.put( keyType, valueSchemaAttributePathInstance );
		node.put( keyName, name );
		node.put( keyId, id );
		node.set( keyAttributePath, attributePath );
		return node;
	}
	
	
	private ObjectNode objectifyJsonInput( String jsonInput ) throws IOException {
			return mapper.readValue( jsonInput, ObjectNode.class );
	}

	
	private void logObjectJSON( final Object object ) {
		try {
			final String json = mapper.writeValueAsString( object );
			log.debug( json );
		} catch (final JsonProcessingException e) {
			log.error( "Unable to serialize " + object.getClass().getName() + " to JSON", e );
		}
	}
	
	
	public ObjectMapper getMapper() {
		return mapper;
	}
}