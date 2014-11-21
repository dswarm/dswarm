package org.dswarm.controller.adapt;

import java.net.URI;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Optional;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.converter.adapt.JsonModelAlreadyTransformedException;
import org.dswarm.converter.adapt.JsonModelValidationException;
import org.dswarm.converter.adapt.JsonSchemaTransformer;
import org.dswarm.converter.adapt.ModelTest;

/**
 * @author tgaengler
 */
public class ControllerModelTest extends ModelTest {

	private static final Logger log = LoggerFactory.getLogger(ControllerModelTest.class);

	//@Test
	public void rewriteSchemaJSON() throws Exception {

		final URI resourceURI = findResource("schema.json");
		final String content = readResource(resourceURI);

		try {

			final ObjectNode schemaJSON = objectMapper.readValue(content, ObjectNode.class);

			final Optional<JsonNode> optionalRootNode = JsonSchemaTransformer.INSTANCE.updateSchemaNode(schemaJSON);
			Assert.assertTrue(optionalRootNode.isPresent());
			checkSchema(optionalRootNode.get(), resourceURI);
			writeBackToSource(optionalRootNode.get(), resourceURI);
			Assert.assertTrue(true);
		} catch (JsonModelAlreadyTransformedException | JsonModelValidationException e) {
			// nothing to do on this resource just continue to the next one
			ControllerModelTest.log.debug("adapted schema '" + resourceURI + "' already");
		}
	}
}
