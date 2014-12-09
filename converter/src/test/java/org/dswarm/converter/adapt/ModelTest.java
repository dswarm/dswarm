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
package org.dswarm.converter.adapt;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.io.Resources;
import com.google.inject.Key;
import com.google.inject.name.Names;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.persistence.GuicedTest;
import org.dswarm.persistence.model.job.Project;
import org.dswarm.persistence.model.job.Task;
import org.dswarm.persistence.model.schema.Schema;

public class ModelTest extends GuicedTest {

	private static final Logger log = LoggerFactory.getLogger(ModelTest.class);

	protected final ObjectMapper objectMapper = GuicedTest.injector.getInstance(ObjectMapper.class);

	private static final String sep = File.separator;

	protected void writeBackToSource(final JsonNode node, final URI uri) throws JsonModelExportException {
		try {
			FileUtils.write(new File(uri), objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true).writeValueAsString(node));
		} catch (final IOException e) {
			throw new JsonModelExportException(e);
		}
	}

	/**
	 * Finds a concrete resource
	 *
	 * @param resourceName The filename of the resource
	 * @return The uri to the resource
	 */
	protected URI findResource(final String resourceName) {
		final URI repositoryUri = findRepository();
		final String filePath = repositoryUri.getRawPath() + ModelTest.sep + resourceName;
		return new File(filePath).toURI();
	}

	protected URI findRepository() {
		final String root = GuicedTest.injector.getInstance(Key.get(String.class, Names.named("dswarm.paths.root")));
		final String resourceRepository = root + ModelTest.sep + "src" + ModelTest.sep + "test" + ModelTest.sep + "resources" + ModelTest.sep;
		return new File(resourceRepository).toURI();
	}

	protected String readResource(final URI uri) {
		try {
			return Resources.toString(uri.toURL(), Charsets.UTF_8);
		} catch (final IOException e) {
			ModelTest.log.error(e.getMessage(), e);
			return "{}";
		}
	}

	protected void rewriteTaskJSON(final String resourceName) throws JsonModelTransformException, JsonModelExportException {

		final URI resourceURI = findResource(resourceName);
		rewriteTaskJSON(resourceURI, false);
	}

	protected void rewriteTaskJSON(final URI uri, final boolean checkTask) throws JsonModelTransformException, JsonModelExportException {
		final String content = readResource(uri);

		try {
			final JsonNode rootNode = JsonSchemaTransformer.INSTANCE.transformFixAttributePathInstance(content, false);

			if (checkTask) {
				checkTransformation(rootNode, uri);
			}
			writeBackToSource(rootNode, uri);
			Assert.assertTrue(true);
		} catch (JsonModelAlreadyTransformedException | JsonModelValidationException e) {
			// nothing to do on this resource just continue to the next one
			ModelTest.log.debug("adapted schema of task '" + uri + "' already");
		}
	}

	protected void rewriteSchemaJSON(final String resourceName) throws Exception {

		final URI resourceURI = findResource(resourceName);
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
			ModelTest.log.debug("adapted schema '" + resourceURI + "' already");
		}
	}

	protected void rewriteProjectJSON(final String resourceName) throws Exception {

		final URI resourceURI = findResource(resourceName);
		final String content = readResource(resourceURI);

		try {

			final JsonNode rootNode = JsonSchemaTransformer.INSTANCE.transformFixAttributePathInstance(content, true);
			Assert.assertNotNull(rootNode);
			checkProject(rootNode, resourceURI);
			writeBackToSource(rootNode, resourceURI);
			Assert.assertTrue(true);
		} catch (JsonModelAlreadyTransformedException | JsonModelValidationException e) {
			// nothing to do on this resource just continue to the next one
			ModelTest.log.debug("adapted project '" + resourceURI + "' already");
		}
	}

	protected void checkSchema(final JsonNode node, final URI uri) throws JsonModelValidationException {
		try {
			final String jsonString = objectMapper.writeValueAsString(node);
			objectMapper.readValue(jsonString, Schema.class);
		} catch (final IOException e) {
			ModelTest.log.warn("The file '" + uri + "' did not pass validation.", e);
			throw new JsonModelValidationException("Invalid JSON content in resource: " + uri.toString(), e);
		}
	}

	protected void checkProject(final JsonNode node, final URI uri) throws JsonModelValidationException {
		try {
			
			// System.out.println(objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true).writeValueAsString(node));
			
			final String jsonString = objectMapper.writeValueAsString(node);
			objectMapper.readValue(jsonString, Project.class);
		} catch (final IOException e) {
			ModelTest.log.warn("The file '" + uri + "' did not pass validation.", e);
			throw new JsonModelValidationException("Invalid JSON content in resource: " + uri.toString(), e);
		}
	}

	protected void checkTransformation(final JsonNode node, final URI uri) throws JsonModelValidationException {
		try {
			final String jsonString = objectMapper.writeValueAsString(node);
			objectMapper.readValue(jsonString, Task.class);
		} catch (final IOException e) {
			ModelTest.log.warn("The file '" + uri + "' did not pass validation.", e);
			throw new JsonModelValidationException("Invalid JSON content in resource: " + uri.toString(), e);
		}
	}
}
