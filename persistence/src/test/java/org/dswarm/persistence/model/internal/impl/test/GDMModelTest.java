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
package org.dswarm.persistence.model.internal.impl.test;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import org.junit.Assert;
import org.junit.Test;

import org.dswarm.graph.json.Model;
import org.dswarm.graph.json.util.Util;
import org.dswarm.persistence.model.internal.gdm.GDMModel;
import org.dswarm.persistence.util.DMPPersistenceUtil;

/**
 * @author tgaengler
 */
public class GDMModelTest {

	@Test
	public void testToJSON() throws IOException {

		testToJSONInternal("test-mabxml.gson", "http://data.slub-dresden.de/records/e9e1fa5a-3350-43ec-bb21-6ccfa90a4497", "test-mabxml.json");
	}

	@Test
	public void testToJSON2() throws IOException {

		testToJSONInternal("test-complex-xml.gson", "http://data.slub-dresden.de/records/7fc13720-2859-477d-9127-5ec65f82220e",
				"test-complex-xml.json");
	}

	@Test
	public void testToJSON3() throws IOException {

		testToJSONInternal("test-pnx.gson", "http://data.slub-dresden.de/datamodels/1/records/a95c0401-5acf-471d-aefb-14cdf0f5deb2", "test-pnx.json");
	}

	@Test
	public void testGetSchema() throws IOException {

		testGetSchemaInternal("test-mabxml.gson", "http://data.slub-dresden.de/records/e9e1fa5a-3350-43ec-bb21-6ccfa90a4497", "mabxml_schema.json");
	}

	private void testToJSONInternal(final String fileName, final String resourceURI, final String expectedFileName) throws IOException {

		// prepare
		final ObjectMapper mapper = Util.getJSONObjectMapper();

		final URL fileURL = Resources.getResource(fileName);

		Assert.assertNotNull(fileURL);

		final String fileContent = Resources.toString(fileURL, StandardCharsets.UTF_8);

		Assert.assertNotNull(fileContent);

		final Model model = mapper.readValue(fileContent, Model.class);

		Assert.assertNotNull(model);

		final GDMModel gdmModel = new GDMModel(model, resourceURI);
		final JsonNode jsonNode = gdmModel.toGDMCompactJSON();

		final String jsonString = DMPPersistenceUtil.getJSONObjectMapper().writeValueAsString(jsonNode);

		Assert.assertNotNull("the JSON string shouldn't be null", jsonString);

		final String expectedJsonString = DMPPersistenceUtil.getResourceAsString(expectedFileName);

		Assert.assertNotNull("the JSON string shouldn't be null", expectedJsonString);

		Assert.assertEquals("the lengths of transformed JSON for FE should be equal", expectedJsonString.length(), jsonString.length());

		Assert.assertEquals("the content of transformed JSON for FE should be equal", expectedJsonString, jsonString);
	}

	private void testGetSchemaInternal(final String fileName, final String resourceURI, final String expectedFileName) throws IOException {

		// prepare
		final ObjectMapper mapper = Util.getJSONObjectMapper();

		final URL fileURL = Resources.getResource(fileName);

		Assert.assertNotNull(fileURL);

		final String fileContent = Resources.toString(fileURL, StandardCharsets.UTF_8);

		Assert.assertNotNull(fileContent);

		final Model model = mapper.readValue(fileContent, Model.class);

		Assert.assertNotNull(model);

		final GDMModel gdmModel = new GDMModel(model, resourceURI);
		final JsonNode jsonNode = gdmModel.getSchema();

		final String jsonString = DMPPersistenceUtil.getJSONObjectMapper().writeValueAsString(jsonNode);

		Assert.assertNotNull("the schema JSON string shouldn't be null", jsonString);

		final String expectedJsonString = DMPPersistenceUtil.getResourceAsString(expectedFileName).trim();

		Assert.assertNotNull("the schema JSON string shouldn't be null", expectedJsonString);

		Assert.assertEquals("the lengths of the schema JSON should be equal", expectedJsonString.length(), jsonString.length());

		Assert.assertEquals("the content of the schema JSON should be equal", expectedJsonString, jsonString);
	}
}
