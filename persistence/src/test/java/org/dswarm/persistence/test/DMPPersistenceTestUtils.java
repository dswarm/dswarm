/**
 * Copyright (C) 2013 – 2015 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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
package org.dswarm.persistence.test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.persistence.adapt.JsonModelExportException;

/**
 *
 * @author sbarthel
 * @author tgaengler
 */
public class DMPPersistenceTestUtils {

	private static final Logger log = LoggerFactory.getLogger(DMPPersistenceTestUtils.class);

	private static final ObjectMapper mapper = new ObjectMapper();

	public static String readResource(final URI uri) {

		try {

			return Resources.toString(uri.toURL(), Charsets.UTF_8);
		} catch (final IOException e) {

			DMPPersistenceTestUtils.log.error(e.getMessage(), e);
			return "{}";
		}
	}

	public static void writeToFile(final JsonNode node, final URI uri) throws JsonModelExportException {

		try {
			FileUtils.write(new File(uri), mapper.configure(SerializationFeature.INDENT_OUTPUT, true).writeValueAsString(node));
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
	public static URI getResourceURI(final String resourceName, final String rootPath) {

		final URI repositoryUri = getTestResourcesBasePath(rootPath);
		final String filePath = repositoryUri.getRawPath() + File.separator + resourceName;
		return new File(filePath).toURI();
	}

	public static URI getTestResourcesBasePath(final String rootPath) {

		final String resourceRepository = rootPath + File.separator + "src" + File.separator + "test" + File.separator + "resources" + File.separator;
		return new File(resourceRepository).toURI();
	}

	public static List<URI> collectResources(final String root) {

		final List<URI> resources = new ArrayList<>();

		final File folder = new File(DMPPersistenceTestUtils.getTestResourcesBasePath(root));
		final IOFileFilter fileFilter = new RegexFileFilter(".*task\\.((.*?)(?<!result)\\.){0,}json"); // find all *task*.json but
		// without result in it
		final Iterator<File> it = FileUtils.iterateFiles(folder, fileFilter, null);

		while (it.hasNext()) {
			resources.add(it.next().toURI());
		}

		return resources;
	}
}
