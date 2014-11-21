package org.dswarm.converter.adapt;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.persistence.model.job.Task;

/**
 * @author sbarthel
 * @author tgaengler
 */
public class ConverterModelTest extends ModelTest {

	private static final Logger log = LoggerFactory.getLogger(ConverterModelTest.class);

	@Test
	public void shouldTransformResource() {
		try {
			for (final URI uri : collectResources()) {
				final String content = readResource(uri);

				try {
					final JsonNode rootNode = JsonSchemaTransformer.INSTANCE.transformFixAttributePathInstance(content);
					checkTransformation(rootNode, uri);
					writeBackToSource(rootNode, uri);
					Assert.assertTrue(true);
				} catch (JsonModelAlreadyTransformedException | JsonModelValidationException e) {
					// nothing to do on this resource just continue to the next one
				}

			}
		} catch (JsonModelTransformException | JsonModelExportException e) {
			ConverterModelTest.log.error(e.getMessage(), e);
			Assert.fail(e.getMessage());
		}
	}

	private void checkTransformation(final JsonNode node, final URI uri) throws JsonModelValidationException {
		try {
			final String jsonString = objectMapper.writeValueAsString(node);
			objectMapper.readValue(jsonString, Task.class);
		} catch (final IOException e) {
			ConverterModelTest.log.warn("The file '" + uri + "' did not pass validation.", e);
			throw new JsonModelValidationException("Invalid JSON content in resource: " + uri.toString(), e);
		}
	}

	private List<URI> collectResources() {
		final List<URI> resources = new ArrayList<>();

		final File folder = new File(findRepository());
		final IOFileFilter fileFilter = new RegexFileFilter(".*task\\.((.*?)(?<!result)\\.){0,}json"); // find all *task*.json but
		// without result in it
		final Iterator<File> it = FileUtils.iterateFiles(folder, fileFilter, null);

		while (it.hasNext()) {
			resources.add(it.next().toURI());
		}

		return resources;
	}
}
