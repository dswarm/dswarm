package de.avgl.dmp.controller.resources.test.utils;

import java.io.File;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.junit.Assert;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;

import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.Resource;
import de.avgl.dmp.persistence.service.resource.ResourceService;

public class ResourcesResourceTestUtils extends ExtendedBasicDMPResourceTestUtils<ResourceService, Resource> {

	private final ConfigurationsResourceTestUtils	configurationsResourceTestUtils;

	public ResourcesResourceTestUtils() {

		super("resources", Resource.class, ResourceService.class);

		configurationsResourceTestUtils = new ConfigurationsResourceTestUtils();
	}

	@Override
	public void compareObjects(final Resource expectedObject, final Resource actualObject) {

		super.compareObjects(expectedObject, actualObject);

		compareResources(expectedObject, actualObject);
	}

	@Override
	public Resource createObject(String objectJSONString, Resource expectedObject) throws Exception {

		Assert.assertNotNull("resource JSON string shouldn't be null", objectJSONString);

		final Resource resourceFromJSON = objectMapper.readValue(objectJSONString, Resource.class);

		Assert.assertNotNull("resource from JSON shouldn't be null", resourceFromJSON);
		Assert.assertNotNull("name of resource from JSON shouldn't be null", resourceFromJSON.getName());

		final URL fileURL = Resources.getResource(resourceFromJSON.getName());
		final File resourceFile = FileUtils.toFile(fileURL);

		final FormDataMultiPart form = new FormDataMultiPart();
		form.field("name", resourceFile.getName());
		form.field("filename", resourceFile.getName());
		form.field("description", resourceFromJSON.getDescription());
		form.bodyPart(new FileDataBodyPart("file", resourceFile, MediaType.MULTIPART_FORM_DATA_TYPE));

		final Response response = target().request(MediaType.MULTIPART_FORM_DATA_TYPE).accept(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.entity(form, MediaType.MULTIPART_FORM_DATA));

		Assert.assertEquals("200 OK was expected", 201, response.getStatus());

		String responseResourceString = response.readEntity(String.class);

		Assert.assertNotNull("resource shouldn't be null", responseResourceString);

		Resource responseResource = objectMapper.readValue(responseResourceString, Resource.class);

		if (resourceFromJSON.getConfigurations() != null && !resourceFromJSON.getConfigurations().isEmpty()) {

			// add configuration

			final Configuration configurationFromJSON = resourceFromJSON.getConfigurations().iterator().next();
			final String configurationJSON = objectMapper.writeValueAsString(configurationFromJSON);

			final Response response2 = target(String.valueOf(responseResource.getId()), "/configurations").request(MediaType.APPLICATION_JSON_TYPE)
					.accept(MediaType.APPLICATION_JSON_TYPE).post(Entity.json(configurationJSON));

			Assert.assertEquals("201 Created was expected", 201, response2.getStatus());

			final String responseConfigurationJSON = response2.readEntity(String.class);

			Assert.assertNotNull("response configuration JSON shouldn't be null", responseConfigurationJSON);

			final Configuration responseConfiguration = objectMapper.readValue(responseConfigurationJSON, Configuration.class);

			Assert.assertNotNull("response configuration shouldn't be null", responseConfiguration);

			configurationsResourceTestUtils.compareObjects(configurationFromJSON, responseConfiguration);

			// retrieve resource (with configuration)

			final Response response3 = target(String.valueOf(responseResource.getId())).request().accept(MediaType.APPLICATION_JSON_TYPE)
					.get(Response.class);

			Assert.assertEquals("200 OK was expected", 200, response3.getStatus());

			final String responseResource2String = response3.readEntity(String.class);

			Assert.assertNotNull("response resource JSON string shouldn't be null", responseResource2String);

			Resource responseResource2 = objectMapper.readValue(responseResource2String, Resource.class);

			responseResource = responseResource2;

		}

		compareObjects(expectedObject, responseResource);

		return responseResource;
	}

	private void compareResources(final Resource expectedResource, final Resource actualResource) {

		if (expectedResource.getAttributes() != null) {

			Assert.assertNotNull("attributes are null", actualResource.getAttributes());

			final ObjectNode attributes = expectedResource.getAttributes();

			final Iterator<Entry<String, JsonNode>> attributeEntriesIter = attributes.fields();

			final ObjectNode responseAttributes = actualResource.getAttributes();

			Assert.assertNotNull("response attributes shouldn't be null", responseAttributes);

			while (attributeEntriesIter.hasNext()) {

				final Entry<String, JsonNode> attributeEntry = attributeEntriesIter.next();

				final String attributeKey = attributeEntry.getKey();

				if (attributeKey.equals("path")) {

					// skip path attribute

					continue;
				}

				final JsonNode attributeValueNode = responseAttributes.get(attributeKey);

				Assert.assertNotNull("attribute '" + attributeKey + "' is not part of the response resource attributes", attributeValueNode);

				final String attributeValue = attributeEntry.getValue().asText();

				Assert.assertTrue("the attribute values of '" + attributeKey + "' are not equal. expected = '" + attributeValue + "'; was = '"
						+ attributeValueNode.asText() + "'", attributeValue.equals(attributeValueNode.asText()));
			}
		}

		if (expectedResource.getConfigurations() != null && !expectedResource.getConfigurations().isEmpty()) {

			final Set<Configuration> actualConfigurations = actualResource.getConfigurations();

			Assert.assertNotNull("configurations of actual resource '" + actualResource.getId() + "' shouldn't be null", actualConfigurations);
			Assert.assertFalse("configurations of actual resource '" + actualResource.getId() + "' shouldn't be empty",
					actualConfigurations.isEmpty());

			final Map<Long, Configuration> actualConfigurationsMap = Maps.newHashMap();

			for (final Configuration actualConfiguration : actualConfigurations) {

				actualConfigurationsMap.put(actualConfiguration.getId(), actualConfiguration);
			}

			configurationsResourceTestUtils.compareObjects(expectedResource.getConfigurations(), actualConfigurationsMap);
		}
	}
}
