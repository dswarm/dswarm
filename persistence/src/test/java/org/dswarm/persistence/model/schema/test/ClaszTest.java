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
package org.dswarm.persistence.model.schema.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.persistence.GuicedTest;
import org.dswarm.persistence.model.schema.Clasz;
import org.dswarm.persistence.service.UUIDService;

public class ClaszTest extends GuicedTest {

	private static final Logger	LOG				= LoggerFactory.getLogger(ClaszTest.class);

	private final ObjectMapper	objectMapper	= GuicedTest.injector.getInstance(ObjectMapper.class);

	@Test
	public void simpleClaszTest() {

		final String biboDocumentId = "http://purl.org/ontology/bibo/Document";
		final String biboDocumentName = "document";

		final String uuid = UUIDService.getUUID(Clasz.class.getSimpleName());

		final Clasz biboDocument = new Clasz(uuid, biboDocumentId, biboDocumentName);

		Assert.assertNotNull("the clasz id shouldn't be null", biboDocument.getUri());
		Assert.assertEquals("the clasz ids are not equal", biboDocumentId, biboDocument.getUri());
		Assert.assertNotNull("the clasz name shouldn't be null", biboDocument.getName());
		Assert.assertEquals("the clasz names are not equal", biboDocumentName, biboDocument.getName());

		String json = null;

		try {

			json = objectMapper.writeValueAsString(biboDocument);
		} catch (final JsonProcessingException e) {

			e.printStackTrace();
		}

		ClaszTest.LOG.debug("clasz json: " + json);
	}

}
