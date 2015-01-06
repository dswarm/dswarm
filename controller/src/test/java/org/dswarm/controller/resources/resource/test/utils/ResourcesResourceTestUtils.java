/**
 * Copyright (C) 2013, 2014 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dswarm.controller.resources.resource.test.utils;

import java.io.File;
import java.net.URL;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.common.io.Resources;
import org.apache.commons.io.FileUtils;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.junit.Assert;

import org.dswarm.controller.resources.test.utils.ExtendedBasicDMPResourceTestUtils;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.Resource;
import org.dswarm.persistence.model.resource.proxy.ProxyResource;
import org.dswarm.persistence.service.resource.ResourceService;
import org.dswarm.persistence.service.resource.test.utils.ResourceServiceTestUtils;

public class ResourcesResourceTestUtils
		extends ExtendedBasicDMPResourceTestUtils<ResourceServiceTestUtils, ResourceService, ProxyResource, Resource> {

	private final ConfigurationsResourceTestUtils configurationsResourceTestUtils;

	public ResourcesResourceTestUtils() {

		super("resources", Resource.class, ResourceService.class, ResourceServiceTestUtils.class);

		configurationsResourceTestUtils = new ConfigurationsResourceTestUtils();
	}

	@Override
	public Resource createObject(final String objectJSONString, final Resource expectedObject) throws Exception {

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

		Assert.assertEquals("201 CREATED was expected", 201, response.getStatus());

		final String responseResourceString = response.readEntity(String.class);

		Assert.assertNotNull("resource shouldn't be null", responseResourceString);

		Resource responseResource = objectMapper.readValue(responseResourceString, Resource.class);

		if (resourceFromJSON.getConfigurations() != null && !resourceFromJSON.getConfigurations().isEmpty()) {

			// add configuration

			final Configuration configurationFromJSON = resourceFromJSON.getConfigurations().iterator().next();
			final String configurationJSON = objectMapper.writeValueAsString(configurationFromJSON);

			final Response response2 = target(String.valueOf(responseResource.getUuid()), "/configurations").request(MediaType.APPLICATION_JSON_TYPE)
					.accept(MediaType.APPLICATION_JSON_TYPE).post(Entity.json(configurationJSON));

			Assert.assertEquals("201 CREATED was expected", 201, response2.getStatus());

			final String responseConfigurationJSON = response2.readEntity(String.class);

			Assert.assertNotNull("response configuration JSON shouldn't be null", responseConfigurationJSON);

			final Configuration responseConfiguration = objectMapper.readValue(responseConfigurationJSON, Configuration.class);

			Assert.assertNotNull("response configuration shouldn't be null", responseConfiguration);

			configurationsResourceTestUtils.compareObjects(configurationFromJSON, responseConfiguration);

			// retrieve resource (with configuration)

			final Response response3 = target(String.valueOf(responseResource.getUuid())).request().accept(MediaType.APPLICATION_JSON_TYPE)
					.get(Response.class);

			Assert.assertEquals("200 OK was expected", 200, response3.getStatus());

			final String responseResource2String = response3.readEntity(String.class);

			Assert.assertNotNull("response resource JSON string shouldn't be null", responseResource2String);

			responseResource = objectMapper.readValue(responseResource2String, Resource.class);

		}

		persistenceServiceTestUtils.compareObjects(expectedObject, responseResource);

		return responseResource;
	}

	public Resource uploadResource(final File resourceFile, final Resource expectedResource) throws Exception {

		final FormDataMultiPart form = new FormDataMultiPart();

		// SR FIXME: why do we set this to hard coded values here? This leads to test failures if expectedResource does not
		// contain the same name values, i.e. when generating test data, one has to know that values in expectedResource must be
		// set to these hard coded values.
		form.field("name", resourceFile.getName());
		form.field("filename", resourceFile.getName());
		form.field("description", "this is a description");
		form.bodyPart(new FileDataBodyPart("file", resourceFile, MediaType.MULTIPART_FORM_DATA_TYPE));

		final Response response = target().request(MediaType.MULTIPART_FORM_DATA_TYPE).accept(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.entity(form, MediaType.MULTIPART_FORM_DATA));

		Assert.assertEquals("201 CREATED was expected", 201, response.getStatus());

		final String responseResourceString = response.readEntity(String.class);

		Assert.assertNotNull("resource shouldn't be null", responseResourceString);

		final Resource responseResource = objectMapper.readValue(responseResourceString, Resource.class);

		persistenceServiceTestUtils.compareObjects(expectedResource, responseResource);

		return responseResource;
	}

	public Configuration addResourceConfiguration(final Resource resource, final String configurationJSON) throws Exception {

		final Response response = target(String.valueOf(resource.getUuid()), "/configurations").request(MediaType.APPLICATION_JSON_TYPE)
				.accept(MediaType.APPLICATION_JSON_TYPE).post(Entity.json(configurationJSON));

		final String responseConfigurationJSON = response.readEntity(String.class);

		Assert.assertEquals("201 CREATED was expected", 201, response.getStatus());
		Assert.assertNotNull("response configuration JSON shouldn't be null", responseConfigurationJSON);

		final Configuration responseConfiguration = objectMapper.readValue(responseConfigurationJSON, Configuration.class);

		Assert.assertNotNull("response configuration shouldn't be null", responseConfiguration);

		final Configuration configuration = objectMapper.readValue(configurationJSON, Configuration.class);

		configurationsResourceTestUtils.compareObjects(configuration, responseConfiguration);

		resource.addConfiguration(responseConfiguration);

		return responseConfiguration;
	}

}
