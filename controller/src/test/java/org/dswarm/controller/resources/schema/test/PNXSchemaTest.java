/**
 * Copyright (C) 2013 – 2016 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.io.Resources;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import org.dswarm.controller.resources.schema.test.helper.PathHelper;
import org.dswarm.controller.resources.schema.test.helper.PathHelpers;
import org.dswarm.controller.resources.test.ResourceTest;
import org.dswarm.controller.test.GuicedTest;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.resource.Resource;
import org.dswarm.persistence.model.resource.ResourceType;
import org.dswarm.persistence.model.resource.utils.ConfigurationStatics;
import org.dswarm.persistence.model.schema.Attribute;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.model.schema.SchemaAttributePathInstance;
import org.dswarm.persistence.service.UUIDService;

@SuppressWarnings("MethodMayBeStatic")
public class PNXSchemaTest extends ResourceTest {

	private static final String CONFIGURATION_NAME        = "pnx";
	private static final String CONFIGURATION_DESCRIPTION = "pnx config";
	private static final String RESOURCE_NAME             = "pnx";
	private static final String RESOURCE_DESCRIPTION      = "pnx file";

	public PNXSchemaTest() {
		super(null);
	}

	@Test
	public void testPNXSchemaCreation() throws Exception {
		final Resource resource = uploadResource();
		final DataModel dataModel = createDataModel(resource);
		final Schema schema = dataModel.getSchema();
		final String dataModelUri = getResourceUri(dataModel);

		compareCreatedSchema(schema, dataModelUri);
	}

	private void compareCreatedSchema(final Schema schema, final String dataModelUri) {
		final PathHelper expected = makeExpected(dataModelUri);
		final PathHelper actual = makeActual(schema, dataModelUri);

		Assert.assertTrue(
				"Actual attribute paths contain superfluous paths",
				attributePathsEqual(actual.attributePaths(), expected.attributePaths()));

		Assert.assertTrue(
				"Actual attribute paths is missing some paths",
				attributePathsEqual(expected.attributePaths(), actual.attributePaths()));
	}

	private ObjectMapper getMapper() {
		return GuicedTest.injector.getInstance(ObjectMapper.class);
	}

	private Resource uploadResource() throws URISyntaxException, IOException {
		final File pnxFile = new File(Resources.getResource("test-pnx.xml").toURI());
		final FileDataBodyPart filePart = new FileDataBodyPart("file", pnxFile, MediaType.MULTIPART_FORM_DATA_TYPE);

		@SuppressWarnings("resource")
		final MultiPart multiPart = new FormDataMultiPart()
				.field("name", RESOURCE_NAME)
				.field("description", RESOURCE_DESCRIPTION)
				.bodyPart(filePart);

		final Response resourceResponse = target("resources").request().buildPost(Entity.entity(multiPart, MediaType.MULTIPART_FORM_DATA_TYPE))
				.invoke();
		final Resource resource = getMapper().readValue(resourceResponse.readEntity(String.class), Resource.class);

		MatcherAssert.assertThat(resourceResponse.getStatus(), Matchers.equalTo(201));
		MatcherAssert.assertThat(resource.getName(), Matchers.equalTo(RESOURCE_NAME));
		MatcherAssert.assertThat(resource.getDescription(), Matchers.equalTo(RESOURCE_DESCRIPTION));
		MatcherAssert.assertThat(resource.getType(), Matchers.equalTo(ResourceType.FILE));

		return resource;
	}

	private Configuration createConfiguration() {

		final String configurationUuid = UUIDService.getUUID(Configuration.class.getSimpleName());

		final Configuration configuration = new Configuration(configurationUuid);
		configuration.setName(CONFIGURATION_NAME);
		configuration.setDescription(CONFIGURATION_DESCRIPTION);
		configuration.addParameter(ConfigurationStatics.STORAGE_TYPE, new TextNode("xml"));
		configuration.addParameter(ConfigurationStatics.RECORD_TAG, new TextNode("record"));
		configuration.addParameter(ConfigurationStatics.XML_NAMESPACE, new TextNode("http://foo-bar.de/"));
		return configuration;
	}

	private DataModel createDataModel(final Resource resource) throws IOException {
		final Configuration configuration = createConfiguration();
		final DataModel model = createDataModel(resource, configuration);
		return obtainDataModelByUpload(resource, model);
	}

	private DataModel createDataModel(final Resource resource, final Configuration configuration) {

		final String dataModelUuid = UUIDService.getUUID(DataModel.class.getSimpleName());

		final DataModel dataModel = new DataModel(dataModelUuid);
		dataModel.setDataResource(resource);
		dataModel.setConfiguration(configuration);
		return dataModel;
	}

	private DataModel obtainDataModelByUpload(final Resource resource, final DataModel model) throws IOException {
		final String dataModelAsString = getMapper().writeValueAsString(model);
		final Response dataModelResponse = target("datamodels").request().buildPost(Entity.json(dataModelAsString)).invoke();
		final DataModel dataModel = getMapper().readValue(dataModelResponse.readEntity(String.class), DataModel.class);

		MatcherAssert.assertThat(dataModelResponse.getStatus(), Matchers.equalTo(201));
		MatcherAssert.assertThat(dataModel.getDataResource(), Matchers.equalTo(resource));
		MatcherAssert.assertThat(dataModel.getConfiguration().getName(), Matchers.equalTo(CONFIGURATION_NAME));
		MatcherAssert.assertThat(dataModel.getConfiguration().getDescription(), Matchers.equalTo(CONFIGURATION_DESCRIPTION));
		return dataModel;
	}

	private String getResourceUri(final DataModel dataModel) {
		return String.format("http://data.slub-dresden.de/resources/%s/schema", dataModel.getUuid());
	}

	private PathHelper makeExpected(final String uri) {
		final PathHelper pathHelper = PathHelpers.newExpected(uri);

		final String[] attributeNames = { "sort", "lsr06", "booster2", "frbr", "ilsapiid", "dedup",
				"search", "lds22", "lds28", "toplevel", "delcategory", "f6", "lds21", "lad04",
				"lad05", "lfc01", "f1", "availlibrary", "sourcesystem", "lds32", "format", "cop",
				"pub", "publisher", "ranking", "availpnx", "btitle", "addata", "searchscope",
				"prefilter", "creationdate", "scope", "control", "lds12", "linktoholdings", "lds17",
				"addsrcrecordid", "links", "lds18", "relation", "delivery", "rsrctype", "lds39",
				"lsr01", "display", "lsr04", "lsr05", "lsr20", "lsr21", "lds31", "addtitle",
				"lfc02", "recordid", "t", "f10", "ispartof", "facets", "c4", "availinstitution",
				"c1", "genre", "title", "sourcerecordid", "sourceformat", "sourceid", "language",
				"institution", "value", "type" };
		for (final String attributeName : attributeNames) {
			pathHelper.make(attributeName);
		}

		PathHelpers.force(pathHelper,
				new String[] { "type" },
				new String[] { "display", "type" },
				new String[] { "display", "type", "type" },
				new String[] { "display", "type", "value" });

		pathHelper.path("sort", "creationdate");
		pathHelper.path("sort", "title");
		pathHelper.path("links", "linktoholdings");
		pathHelper.path("frbr", "t");
		pathHelper.path("dedup", "f10");
		pathHelper.path("dedup", "c4");
		pathHelper.path("dedup", "c1");
		pathHelper.path("dedup", "f6");
		pathHelper.path("dedup", "f1");
		pathHelper.path("dedup", "t");
		pathHelper.path("search", "rsrctype");
		pathHelper.path("search", "scope");
		pathHelper.path("search", "addsrcrecordid");
		pathHelper.path("search", "title");
		pathHelper.path("search", "lsr06");
		pathHelper.path("search", "lsr01");
		pathHelper.path("search", "lsr04");
		pathHelper.path("search", "searchscope");
		pathHelper.path("search", "lsr20");
		pathHelper.path("search", "lsr21");
		pathHelper.path("search", "creationdate");
		pathHelper.path("search", "sourceid");
		pathHelper.path("search", "addtitle");
		pathHelper.path("search", "recordid");
		pathHelper.path("search", "lsr05");
		pathHelper.path("display", "availlibrary");
		pathHelper.path("display", "lds12");
		pathHelper.path("display", "ispartof");
		pathHelper.path("display", "publisher");
		pathHelper.path("display", "lds17");
		pathHelper.path("display", "format");
		pathHelper.path("display", "availinstitution");
		pathHelper.path("display", "availpnx");
		pathHelper.path("display", "lds39");
		pathHelper.path("display", "title");
		pathHelper.path("display", "lds28");
		pathHelper.path("display", "relation");
		pathHelper.path("display", "lds18");
		pathHelper.path("display", "lds31");
		pathHelper.path("display", "creationdate");
		pathHelper.path("display", "lds32");
		pathHelper.path("display", "language");
		pathHelper.path("display", "lds21");
		pathHelper.path("display", "lds22");
		pathHelper.path("facets", "language");
		pathHelper.path("facets", "toplevel");
		pathHelper.path("facets", "prefilter");
		pathHelper.path("facets", "creationdate");
		pathHelper.path("facets", "lfc02");
		pathHelper.path("facets", "rsrctype");
		pathHelper.path("facets", "lfc01");
		pathHelper.path("ranking", "booster2");
		pathHelper.path("delivery", "delcategory");
		pathHelper.path("delivery", "institution");
		pathHelper.path("addata", "genre");
		pathHelper.path("addata", "format");
		pathHelper.path("addata", "cop");
		pathHelper.path("addata", "btitle");
		pathHelper.path("addata", "pub");
		pathHelper.path("addata", "addtitle");
		pathHelper.path("addata", "lad04");
		pathHelper.path("addata", "lad05");
		pathHelper.path("control", "sourceformat");
		pathHelper.path("control", "sourcesystem");
		pathHelper.path("control", "sourcerecordid");
		pathHelper.path("control", "addsrcrecordid");
		pathHelper.path("control", "ilsapiid");
		pathHelper.path("control", "sourceid");
		pathHelper.path("control", "recordid");

		return pathHelper;
	}

	private PathHelper makeActual(final Schema schema, final String dataModelUri) {
		final PathHelper actual = PathHelpers.newActual(dataModelUri);

		for (final SchemaAttributePathInstance attributePath : schema.getUniqueAttributePaths()) {
			final List<Attribute> attributes = attributePath.getAttributePath().getAttributePath();

			final String[] attributeNames = new String[attributes.size()];
			int idx = 0;

			for (final Attribute attribute : attributes) {
				actual.make(attribute.getName());
				attributeNames[idx++] = attribute.getName();
			}
			actual.path(attributeNames);
		}
		return actual;
	}

	private boolean attributePathsEqual(final Iterable<AttributePath> actual, final Iterable<AttributePath> expected) {
		for (final AttributePath attributePath : actual) {
			if (!findAttributePath(expected, attributePath.getAttributePath())) {
				return false;
			}
		}
		return true;
	}

	private boolean findAttributePath(final Iterable<AttributePath> haystack, final Iterable<Attribute> needle) {
		for (final AttributePath path : haystack) {
			if (isEqualByUri(path.getAttributePath(), needle)) {
				return true;
			}
		}
		return false;
	}

	private boolean isEqualByUri(final Iterable<Attribute> actualAttributes, final Iterable<Attribute> expectedAttributes) {
		final Iterator<Attribute> actuals = actualAttributes.iterator();
		for (final Attribute expectedAttribute : expectedAttributes) {
			if (!actuals.hasNext() || !expectedAttribute.getUri().equals(actuals.next().getUri())) {
				return false;
			}
		}

		return !actuals.hasNext();
	}
}
