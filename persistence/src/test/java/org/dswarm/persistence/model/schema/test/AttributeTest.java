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
import org.dswarm.persistence.model.schema.Attribute;
import org.dswarm.persistence.service.UUIDService;

public class AttributeTest extends GuicedTest {

	private static final Logger	LOG				= LoggerFactory.getLogger(AttributeTest.class);

	private final ObjectMapper	objectMapper	= GuicedTest.injector.getInstance(ObjectMapper.class);

	@Test
	public void simpleAttributeTest() {

		final String attributeUri = "http://purl.org/dc/terms/title";
		final String attributeName = "title";

		final String uuid = UUIDService.getUUID(Attribute.class.getSimpleName());

		final Attribute dctermsTitle = new Attribute(uuid, attributeUri);
		dctermsTitle.setName(attributeName);

		Assert.assertNotNull("the attribute uri shouldn't be null", dctermsTitle.getUri());
		Assert.assertEquals("the attribute uris are not equal", attributeUri, dctermsTitle.getUri());
		Assert.assertNotNull("the attribute name shouldn't be null", dctermsTitle.getName());
		Assert.assertEquals("the attribute names are not equal", attributeName, dctermsTitle.getName());

		String json = null;

		try {

			json = objectMapper.writeValueAsString(dctermsTitle);
		} catch (final JsonProcessingException e) {

			e.printStackTrace();
		}

		AttributeTest.LOG.debug("attribute json: " + json);
	}

}
