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
package org.dswarm.controller.resources.schema.test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.junit.Assert;
import org.junit.Test;

import org.dswarm.controller.resources.test.ResourceTest;
import org.dswarm.controller.test.GuicedTest;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.resource.Resource;
import org.dswarm.persistence.model.resource.ResourceType;
import org.dswarm.persistence.model.resource.utils.ConfigurationStatics;
import org.dswarm.persistence.model.schema.Attribute;
import org.dswarm.persistence.model.schema.SchemaAttributePathInstance;
import org.dswarm.persistence.service.UUIDService;

@SuppressWarnings("MethodMayBeStatic")
public class SchemaOrderTest extends ResourceTest {

	private Resource      resource;
	private DataModel     dataModel;
	private Configuration configuration;

	public SchemaOrderTest() {
		super(null);
	}

	@Test
	public void testDD492() throws Exception {
		givenUploadedResource();

		whenConfiguringTheSchema();

		assertThatAllAttributePathsAreInOrder();
	}

	private void givenUploadedResource() throws URISyntaxException, IOException {
		final String name = "DD-492";
		final String description = "DD-492 Test";
		final String resourceFileName = "dd-492_test.csv";
		final File content = new File(Resources.getResource(resourceFileName).toURI());

		resource = uploadAndTestResource(name, description, content);
	}

	private void whenConfiguringTheSchema() throws IOException {
		dataModel = uploadAndTestDataModel();
	}

	private void assertThatAllAttributePathsAreInOrder() {
		final List<String> expectedUris = getExpecteds();
		final List<String> actualUris = getActuals();

		final Iterator<String> actualIterator = actualUris.iterator();
		for (int i = 0; i < expectedUris.size(); i++) {
			final String expectedUri = expectedUris.get(i);
			Assert.assertTrue(
					String.format("The schema is missing the attribute path %s", expectedUri),
					actualIterator.hasNext());
			final String actualUri = actualIterator.next();
			Assert.assertEquals(
					String.format("The %d. attribute path aren't equal. Expected [%s] but got [%s].", i + 1, expectedUri, actualUri),
					expectedUri, actualUri);
		}
		Assert.assertFalse("The schema has a superfluous attribute path", actualIterator.hasNext());
	}

	private List<String> getExpecteds() {
		final Resource thisResource = this.resource;
		final List<String> names = Lists.newArrayList("id", "val1", "val2", "val3", "val4", "val5", "val6", "type");
		return Lists.transform(names, new Function<String, String>() {

			@Override
			public String apply(final String input) {
				if (input.equals("type")) {
					return "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
				}
				return String.format("http://data.slub-dresden.de/resources/%s/schema#%s", thisResource.getUuid(), input);
			}
		});
	}

	private List<String> getActuals() {
		final List<String> actuals = Lists.newArrayList();
		final Collection<SchemaAttributePathInstance> attributePaths = dataModel.getSchema().getAttributePaths();
		for (final SchemaAttributePathInstance attributePath : attributePaths) {
			final Iterable<Attribute> path = attributePath.getAttributePath().getAttributePath();
			final Iterable<String> pathUris = Iterables.transform(path, new Function<Attribute, String>() {

				@Override
				public String apply(final Attribute input) {
					return input.getUri();
				}
			});
			final String actualUri = Joiner.on('\u001E').join(pathUris);
			actuals.add(actualUri);
		}
		return actuals;
	}

	private Resource uploadAndTestResource(final String name, final String description, final File content) throws IOException {
		final Entity<MultiPart> entity = givenResourceEntity(name, description, content);

		final Resource resource = whenResourceIsUploaded(entity);

		return assertResourceHasCorrectProperties(name, description, resource);
	}

	private DataModel uploadAndTestDataModel() throws IOException {
		final Entity<String> entity = givenDataModelEntity();

		final DataModel model = whenDataModelIsUploaded(entity);

		return assertDataModelHasCorrectProperties(model);
	}

	@SuppressWarnings("resource")
	private Entity<MultiPart> givenResourceEntity(final String name, final String description, final File content) {
		final FileDataBodyPart filePart =
				new FileDataBodyPart("file", content, MediaType.MULTIPART_FORM_DATA_TYPE);
		final MultiPart multiPart = new FormDataMultiPart()
				.field("name", name).field("description", description).bodyPart(filePart);
		return Entity.entity(multiPart, MediaType.MULTIPART_FORM_DATA_TYPE);
	}

	private Entity<String> givenDataModelEntity() throws JsonProcessingException {
		final DataModel dm = createNewDataModel();
		return Entity.json(getObjectMapper().writeValueAsString(dm));
	}

	private Resource whenResourceIsUploaded(final Entity<?> entity) throws IOException {
		final String response = uploadResource("resources", entity);
		return readResponse(response, Resource.class);
	}

	private DataModel whenDataModelIsUploaded(final Entity<?> entity) throws IOException {
		final String response = uploadResource("datamodels", entity);
		return readResponse(response, DataModel.class);
	}

	private Resource assertResourceHasCorrectProperties(final String name, final String description, final Resource resource) {
		Assert.assertEquals(resource.getName(), name);
		Assert.assertEquals(resource.getDescription(), description);
		Assert.assertEquals(resource.getType(), ResourceType.FILE);

		return resource;
	}

	private DataModel assertDataModelHasCorrectProperties(final DataModel model) {
		Assert.assertEquals(model.getDataResource(), resource);
		Assert.assertEquals(model.getConfiguration().getName(), configuration.getName());
		Assert.assertEquals(model.getConfiguration().getDescription(), configuration.getDescription());

		return model;
	}

	private String uploadResource(final String location, final Entity<?> entity) {
		final Response response = target(location).request().buildPost(entity).invoke();
		Assert.assertEquals(response.getStatus(), 201);

		return response.readEntity(String.class);
	}

	private <T> T readResponse(final String response, final Class<T> klass) throws IOException {
		return getObjectMapper().readValue(response, klass);
	}

	private DataModel createNewDataModel() {
		createConfiguration();
		return createDataModel();
	}

	private void createConfiguration() {
		final String name = "DD-492";
		final String description = "DD-492 config";

		final String configurationUuid = UUIDService.getUUID(Configuration.class.getSimpleName());

		configuration = new Configuration(configurationUuid);
		configuration.setName(name);
		configuration.setDescription(description);
		configuration.addParameter(ConfigurationStatics.STORAGE_TYPE, new TextNode("csv"));
		configuration.addParameter(ConfigurationStatics.COLUMN_DELIMITER, new TextNode(","));
	}

	private DataModel createDataModel() {

		final String dataModelUuid = UUIDService.getUUID(DataModel.class.getSimpleName());

		final DataModel dataModel = new DataModel(dataModelUuid);
		dataModel.setDataResource(resource);
		dataModel.setConfiguration(configuration);
		return dataModel;
	}

	private ObjectMapper getObjectMapper() {
		return GuicedTest.injector.getInstance(ObjectMapper.class);
	}
}
