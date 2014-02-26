package de.avgl.dmp.converter.morph;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.avgl.dmp.converter.GuicedTest;
import de.avgl.dmp.persistence.mapping.JsonToPojoMapper;
import de.avgl.dmp.persistence.model.job.Job;
import de.avgl.dmp.persistence.model.job.Mapping;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

public class MorphScriptBuilderTest extends GuicedTest {
	
	// TODO:

//	@Test
//	public void testRequestToMorph() throws Exception {
//
//		final String request = DMPPersistenceUtil.getResourceAsString("complex-request.json");
//		final String expected = DMPPersistenceUtil.getResourceAsString("complex-metamorph.xml");
//
//		final Job job = injector.getInstance(JsonToPojoMapper.class).toJob(request);
//		final String actual = new MorphScriptBuilder().apply(job.getTransformations()).toString();
//
//		assertEquals(expected, actual);
//	}
	
	
	@Test
	public void testComplexMappingToMorph() throws Exception {
		
		final ObjectMapper objectMapper = injector.getInstance(ObjectMapper.class);
		
		final String mappingJSONString = DMPPersistenceUtil.getResourceAsString("complete.complex.mapping.as.in.intranet.json");
		//final String mappingJSONString = DMPPersistenceUtil.getResourceAsString("complex.mapping.as.in.intranet.json"); // does not work, since @IDRef was used to reference input/output components -> object mapper seems to fail reconstructing the full objects here
		
		
		Mapping mapping = objectMapper.readValue(mappingJSONString,Mapping.class);
		
		final String morphScriptString = new MorphScriptBuilder().apply(mapping).toString();
		
		System.out.println(morphScriptString);
		
	}
	
}
