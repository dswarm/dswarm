package de.avgl.dmp.controller.resources.test;

import java.io.IOException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.avgl.dmp.persistence.model.job.Mapping;
import de.avgl.dmp.persistence.service.job.MappingService;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

public class MappingsResourceTest extends ResourceTest {
	
	private final MappingService				mappingService = injector.getInstance(MappingService.class);

	private final ObjectMapper						objectMapper = injector.getInstance(ObjectMapper.class);
	
	public MappingsResourceTest() {
		super("mappings");
	}

	private static final org.apache.log4j.Logger	LOG						= org.apache.log4j.Logger.getLogger(MappingsResourceTest.class);
	private String mappingJSONStringFile	= null;

	@Before
	public void prepare() throws IOException {
		mappingJSONStringFile = DMPPersistenceUtil.getResourceAsString("mapping.json");
	}
	
	@Test
	public void testCreateMapping() throws Exception {

		objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
		Mapping mapping = objectMapper.readValue(mappingJSONStringFile, Mapping.class);
		LOG.debug("[JUnit] after transforming JSON mapping in mapping object: " + ToStringBuilder.reflectionToString(mapping));
		Assert.assertNotNull(mapping);
		
		final Response response = target().request(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE).get(Response.class);
		Assert.assertEquals("200 was expected", 200, response.getStatus());
		
		String mappingJSONString = objectMapper.writeValueAsString(mapping);
		Assert.assertNotNull(mappingJSONString);
		
		String responseString = response.readEntity(String.class);
		Assert.assertNotNull(responseString);
		LOG.debug("[JUnit] mapping to response: " + responseString);
				
	}

}
