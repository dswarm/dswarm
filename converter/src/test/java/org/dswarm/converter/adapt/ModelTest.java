package org.dswarm.converter.adapt;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.dswarm.persistence.GuicedTest;
import org.dswarm.persistence.model.job.Task;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.inject.Key;
import com.google.inject.name.Names;

public class ModelTest extends GuicedTest {

	private static final Logger log = LoggerFactory.getLogger( ModelTest.class );
	
	private final ObjectMapper objectMapper = GuicedTest.injector.getInstance(ObjectMapper.class);
	
	private static final String sep = File.separator;
	
	@Test
	public void shouldTransformResource() {
		try {
			for( URI uri : collectResources() ) {
				String content = readResource( uri );
				
				try {
					JsonNode rootNode = JsonSchemaTransformer.INSTANCE.transformFixAttributePathInstance( content );
					checkTransformation( rootNode, uri );
					writeBackToSource( rootNode, uri );
					Assert.assertTrue( true );
				} catch( JsonModelAlreadyTransformedException | JsonModelValidationException e ) {
					//nothing to do on this resource just continue to the next one
					continue;
				}
				
			}
		} catch( JsonModelTransformException | JsonModelExportException e ) {
			log.error( e.getMessage(), e );
			Assert.fail( e.getMessage() );
		}
	}
	
	
	private void checkTransformation( JsonNode node, URI uri ) throws JsonModelValidationException {
		try {
			String jsonString = objectMapper.writeValueAsString( node );
			objectMapper.readValue( jsonString, Task.class );
		} catch( IOException e ) {
			log.warn( "The file '" + uri +  "' did not pass validation.", e );
			throw new JsonModelValidationException( "Invalid JSON content in resource: " + uri.toString(), e );
		}
	}
	
	
	private void writeBackToSource( JsonNode node, URI uri ) throws JsonModelExportException {
		try {
			FileUtils.write( new File( uri ), objectMapper.writeValueAsString( node ) );
		} catch( IOException e ) {
			throw new JsonModelExportException( e );
		}
	}
	
	
	private List<URI> collectResources() {
		List<URI> resources = new ArrayList<>();
		
		File folder = new File( findRepository() );
		IOFileFilter fileFilter = new RegexFileFilter( ".*task\\.((.*?)(?<!result)\\.){0,}json" );	//find all *task*.json but without result in it
		Iterator<File> it = FileUtils.iterateFiles( folder, fileFilter, null );
		
		while( it.hasNext() ) 
			resources.add( it.next().toURI() );
		
		return resources;
	}
	
	
	
	/**
	 * Finds a concrete resource
	 * @param resourceName The filename of the resource 
	 * @return The uri to the resource
	 */
	private URI findResource( String resourceName ) {
		URI repositoryUri = findRepository();
		String filePath = repositoryUri.getRawPath() + resourceName;
		return new File( filePath ).toURI();
	}
	

	private URI findRepository() {
		String root = GuicedTest.injector.getInstance( Key.get( String.class, Names.named("dswarm.paths.root") ) );
		String resourceRepository = root + sep + "src" + sep + "test" + sep + "resources" + sep;
		return new File( resourceRepository ).toURI();
	}
	
	
	private String readResource( URI uri ) {
		try {
			return Resources.toString( uri.toURL(), Charsets.UTF_8 );
		} catch( IOException e ) {
			log.error( e.getMessage(), e );
			return "{}";
		}
	}
	
}
