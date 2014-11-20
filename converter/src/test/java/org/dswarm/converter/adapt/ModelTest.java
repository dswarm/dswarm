package org.dswarm.converter.adapt;

import org.apache.commons.io.FileUtils;
import org.dswarm.persistence.GuicedTest;
import org.dswarm.persistence.model.job.Task;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ModelTest extends GuicedTest {

	private static final Logger log = LoggerFactory.getLogger( ModelTest.class );
	
	private final ObjectMapper objectMapper = GuicedTest.injector.getInstance(ObjectMapper.class);
	
	private static final String resourceToManipulate = "dd-528.mabxml.task.json";
	
	@Test
	public void shouldTransformResource() {
		try {
			JsonNode node = JsonSchemaTransformer.INSTANCE.transformFixAttributePathInstance( resourceToManipulate );
			
			//check if ok
			String jsonString = objectMapper.writeValueAsString( node );
			objectMapper.readValue( jsonString, Task.class );
			
			//write back into file
//			FileUtils.
			
		} catch( Exception e ) {
			log.error( e.getMessage(), e );
		}
		
	}
	
}
