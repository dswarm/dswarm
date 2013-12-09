package de.avgl.dmp.controller.resources.test;

import java.io.IOException;
import java.util.Set;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;

import de.avgl.dmp.controller.resources.test.utils.ResourceTestUtils;
import de.avgl.dmp.persistence.model.schema.Schema;
import de.avgl.dmp.persistence.service.schema.SchemaService;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

public class SchemasResourceTest extends ResourceTest {

	private static final org.apache.log4j.Logger	LOG						= org.apache.log4j.Logger.getLogger(SchemasResourceTest.class);

	private String									schemaJSONString	= null;
	private Schema									expectedSchema	= null;
	private Set<Schema>								expectedSchemas	= null;

	private final SchemaService						schemaService = injector.getInstance(SchemaService.class);

	private final ObjectMapper						objectMapper = injector.getInstance(ObjectMapper.class);


	public SchemasResourceTest() {
		super("schemas");
	}

	@Before
	public void prepare() throws IOException {
		schemaJSONString = DMPPersistenceUtil.getResourceAsString("schema.json");
		expectedSchema = DMPPersistenceUtil.getJSONObjectMapper().readValue(schemaJSONString, Schema.class);
	}

	@Test
	public void testPOSTSchemas() throws Exception {

		final Schema actualSchema = createSchemaInternal();

		cleanUpDB(actualSchema);
	}

	/*
	@Test
		public void testGETSchemas() throws Exception {

			final Schema actualSchema = createSchemaInternal();

			LOG.debug("try to retrieve schemas");

			final Response response = target().request().accept(MediaType.APPLICATION_JSON_TYPE).get(Response.class);

			Assert.assertEquals("200 OK was expected", 200, response.getStatus());

			final String responseSchemas = response.readEntity(String.class);

			expectedSchemas = Sets.newHashSet();
			expectedSchemas.add(actualSchema);

			ResourceTestUtils.evaluateSchemas(responseSchemas, expectedSchemas);

			cleanUpDB(actualSchema);
		}
	*/


	@Test
	public void testGETSchema() throws Exception {

		final Schema actualSchema = createSchemaInternal();

		LOG.debug("try to retrieve schema");

		final Response response = target(String.valueOf(actualSchema.getId())).request()
				.accept(MediaType.APPLICATION_JSON_TYPE).get(Response.class);

		Assert.assertEquals("200 OK was expected", 200, response.getStatus());

		final String responseSchemaJSON = response.readEntity(String.class);

		Assert.assertNotNull("response schema JSON shouldn't be null", responseSchemaJSON);

		final Schema responseSchema = objectMapper
				.readValue(responseSchemaJSON, Schema.class);

		Assert.assertNotNull("response schema shouldn't be null", responseSchema);

//		ResourceTestUtils.compareSchemas(actualSchema, responseSchema);

		cleanUpDB(responseSchema);
	}


	private Schema createSchemaInternal() throws Exception {

		final Response response = target().request(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.json(schemaJSONString));

		Assert.assertEquals("201 Created was expected", 201, response.getStatus());

		final String responseString = response.readEntity(String.class);

		Assert.assertNotNull("the response JSON shouldn't be null", responseString);

		final Schema actualSchema = objectMapper.readValue(responseString, Schema.class);

//		ResourceTestUtils.compareSchemas(expectedSchema, actualSchema);

		return actualSchema;
	}


	private void cleanUpDB(final Schema schema) {

		final Long schemaId = schema.getId();

		schemaService.deleteObject(schemaId);

		final Schema deletedSchema = schemaService.getObject(schemaId);

		Assert.assertNull("the deleted schema should be null", deletedSchema);
	}

}
