package de.avgl.dmp.controller.resources.test.utils;

import java.util.Iterator;
import java.util.Map.Entry;

import org.junit.Assert;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.avgl.dmp.persistence.model.schema.Schema;
import de.avgl.dmp.persistence.service.schema.SchemaService;

public class SchemasResourceTestUtils extends BasicResourceTestUtils<SchemaService, Schema, Long> {

	public SchemasResourceTestUtils() {

		super("schemas", Schema.class, SchemaService.class);
	}
	
	@Override
	public void compareObjects(final Schema expectedObject, final Schema actualObject) {
		
		super.compareObjects(expectedObject, actualObject);
		
		compareSchemas(expectedObject, actualObject);
	}

	
	private void compareSchemas(final Schema expectedSchema, final Schema actualSchema) {

		if (expectedSchema.getName() != null) {

			Assert.assertNotNull("the schema name shouldn't be null", actualSchema.getName());
			Assert.assertEquals("the schema names should be equal", expectedSchema.getName(), actualSchema.getName());
		}

		/*
		if (expectedSchema.getDescription() != null) {

			Assert.assertNotNull("the configuration description shouldn't be null", actualSchema.getDescription());
			Assert.assertEquals("the configuration descriptions should be equal", expectedSchema.getDescription(),
					actualSchema.getDescription());
		}*/
		
		/*
		
		// TODO: do a similar comparison for the attributepaths
		
		Assert.assertNotNull("parameters are null", actualConfiguration.getParameters());
		Assert.assertEquals("parameters are not equal", expectedConfiguration.getParameters(), actualConfiguration.getParameters());

		final ObjectNode parameters = expectedConfiguration.getParameters();

		final Iterator<Entry<String, JsonNode>> parameterEntriesIter = parameters.fields();

		final ObjectNode responseParameters = actualConfiguration.getParameters();

		Assert.assertNotNull("response parameters shoudln't be null", responseParameters);

		while (parameterEntriesIter.hasNext()) {

			final Entry<String, JsonNode> parameterEntry = parameterEntriesIter.next();

			final String parameterKey = parameterEntry.getKey();

			final JsonNode parameterValueNode = responseParameters.get(parameterKey);

			Assert.assertNotNull("parameter '" + parameterKey + "' is not part of the response configuration parameters", parameterValueNode);

			final String parameterValue = parameterEntry.getValue().asText();

			Assert.assertTrue("the parameter values of '" + parameterKey + "' are not equal. expected = '" + parameterValue + "'; was = '"
					+ parameterValueNode.asText() + "'", parameterValue.equals(parameterValueNode.asText()));
		}
		*/
	}
}

