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
package org.dswarm.controller.resources.job.test.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.io.Resources;
import org.apache.commons.io.FileUtils;
import org.dswarm.controller.resources.resource.test.utils.ResourcesResourceTestUtils;
import org.dswarm.persistence.model.resource.Resource;
import org.dswarm.persistence.model.resource.ResourceType;
import org.dswarm.persistence.service.UUIDService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

/**
 * Created by tgaengler on 04.03.16.
 */
public class PrepareResource {

	private static final Logger LOG = LoggerFactory.getLogger(PrepareResource.class);

	private Resource res1;
	private Resource resource;
	private final String resourceFileName;

	private final ObjectMapper objectMapper;
	private final ResourcesResourceTestUtils resourcesResourceTestUtils;

	public PrepareResource(final String resourceFileName, final ObjectMapper objectMapper, final ResourcesResourceTestUtils resourcesResourceTestUtils) {
		this.resourceFileName = resourceFileName;
		this.objectMapper = objectMapper;
		this.resourcesResourceTestUtils = resourcesResourceTestUtils;
	}

	public Resource getRes1() {
		return res1;
	}

	public Resource getResource() {
		return resource;
	}

	public PrepareResource invoke() throws Exception {
		final String resource1Uuid = UUIDService.getUUID(Resource.class.getSimpleName());

		res1 = new Resource(resource1Uuid);
		res1.setName(resourceFileName);
		res1.setDescription("this is a description");
		res1.setType(ResourceType.FILE);

		final URL fileURL = Resources.getResource(resourceFileName);
		final File resourceFile = FileUtils.toFile(fileURL);

		final ObjectNode attributes1 = new ObjectNode(objectMapper.getNodeFactory());
		attributes1.put("path", resourceFile.getAbsolutePath());

		String fileType = null;
		try {
			fileType = Files.probeContentType(resourceFile.toPath());
		} catch (final IOException e1) {

			LOG.debug("couldn't determine file type from file '{}'", resourceFile.getAbsolutePath());
		}

		if (fileType != null) {

			attributes1.put("filetype", fileType);
		}

		// hint: size is not important to know since its value is skipped in the comparison of actual and expected resource
		attributes1.put("filesize", -1);

		res1.setAttributes(attributes1);

		// upload data resource
		resource = resourcesResourceTestUtils.uploadResource(resourceFile, res1);
		return this;
	}
}
