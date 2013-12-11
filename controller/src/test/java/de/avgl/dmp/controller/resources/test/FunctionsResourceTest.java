package de.avgl.dmp.controller.resources.test;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.avgl.dmp.persistence.model.job.Function;
import de.avgl.dmp.persistence.services.FunctionService;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

public class FunctionsResourceTest extends ResourceTest {
	
	private final FunctionService				functionService = injector.getInstance(FunctionService.class);

	private final ObjectMapper						objectMapper = injector.getInstance(ObjectMapper.class);
	
	public FunctionsResourceTest() {
		super("functions");
	}

	private static final org.apache.log4j.Logger	LOG						= org.apache.log4j.Logger.getLogger(FunctionsResourceTest.class);
	private String functionJSONList	= null;

	@Before
	public void prepare() throws IOException {
		functionJSONList = DMPPersistenceUtil.getResourceAsString("functions.json");
	}
	
	@Test
	public void testGetAllFunctions() throws Exception {

		List<Function> functionList = functionService.getObjects();
		if (functionList.isEmpty()) {
			functionPreload();
		}
		
		final Response response = target().request().accept(MediaType.APPLICATION_JSON_TYPE).get(Response.class);
		Assert.assertEquals("200 was expected", 200, response.getStatus());
		
		String functionJSONString = objectMapper.writeValueAsString(functionList);
		Assert.assertNotNull(functionJSONString);
		
		String responseString = response.readEntity(String.class);
		Assert.assertNotNull(responseString);
		LOG.debug("[JUnit] list of functions: " + responseString);
				
	}
	
	private void functionPreload() throws Exception, JsonMappingException, IOException {

		objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
		List<Function> functionList = objectMapper.readValue(functionJSONList, new TypeReference<List<Function>>(){});
		
		LOG.debug("[JUnit] Preload - number of simple metafacture functions: " + functionList.size());
		
		for (Function item: functionList) {
			Function simpleFunction = functionService.createObject();
			simpleFunction.setName(item.getName());
			simpleFunction.setDescription(item.getDescription());
			simpleFunction.setFunctionDescription(item.getFunctionDescription());
			simpleFunction = functionService.updateObjectTransactional(simpleFunction);
		}
	}

}
