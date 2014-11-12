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
package org.dswarm.persistence.service.schema.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.persistence.GuicedTest;
import org.dswarm.persistence.model.schema.ContentSchema;
import org.dswarm.persistence.model.schema.proxy.ProxyContentSchema;
import org.dswarm.persistence.service.schema.ContentSchemaService;
import org.dswarm.persistence.service.schema.test.utils.ContentSchemaServiceTestUtils;
import org.dswarm.persistence.service.test.IDBasicJPAServiceTest;

public class ContentSchemaServiceTest extends IDBasicJPAServiceTest<ProxyContentSchema, ContentSchema, ContentSchemaService> {

	private static final Logger LOG = LoggerFactory.getLogger(ContentSchemaServiceTest.class);

	private final ObjectMapper objectMapper = GuicedTest.injector.getInstance(ObjectMapper.class);

	private ContentSchemaServiceTestUtils contentSchemaServiceTestUtils;

	public ContentSchemaServiceTest() {

		super("content schema", ContentSchemaService.class);

	}

	@Override protected void initObjects() {
		super.initObjects();

		contentSchemaServiceTestUtils = new ContentSchemaServiceTestUtils();
	}

	@Test
	public void testSimpleSchema() throws Exception {

		final ContentSchema contentSchema = contentSchemaServiceTestUtils.createDefaultObject();

		final ContentSchema updatedContentSchema = contentSchemaServiceTestUtils.updateObject(contentSchema, contentSchema);

		String json = null;

		try {

			json = objectMapper.writeValueAsString(updatedContentSchema);
		} catch (final JsonProcessingException e) {

			e.printStackTrace();
		}

		ContentSchemaServiceTest.LOG.debug("content schema json: " + json);
	}
}
