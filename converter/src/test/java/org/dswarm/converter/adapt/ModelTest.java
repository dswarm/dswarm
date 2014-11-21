package org.dswarm.converter.adapt;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.inject.Key;
import com.google.inject.name.Names;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.persistence.GuicedTest;
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

	protected void checkSchema(final JsonNode node, final URI uri) throws JsonModelValidationException {
		try {
			final String jsonString = objectMapper.writeValueAsString(node);
			objectMapper.readValue(jsonString, Schema.class);
		} catch (final IOException e) {
			ModelTest.log.warn("The file '" + uri + "' did not pass validation.", e);
			throw new JsonModelValidationException("Invalid JSON content in resource: " + uri.toString(), e);
		}
	}
}
