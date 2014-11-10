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

import org.dswarm.persistence.GuicedTest;
import org.dswarm.persistence.model.schema.Attribute;
import org.dswarm.persistence.model.schema.proxy.ProxyAttribute;
import org.dswarm.persistence.service.schema.AttributeService;
import org.dswarm.persistence.service.schema.test.utils.AttributeServiceTestUtils;
import org.dswarm.persistence.service.test.AdvancedJPAServiceTest;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AttributeServiceTest extends AdvancedJPAServiceTest<ProxyAttribute, Attribute, AttributeService> {

	private static final Logger LOG = LoggerFactory.getLogger(AttributeServiceTest.class);

	private final ObjectMapper objectMapper = GuicedTest.injector.getInstance(ObjectMapper.class);


	private AttributeServiceTestUtils astUtils;
	
	public AttributeServiceTest() {
		super("attribute", AttributeService.class);
	}

	
	@Override
	protected void initObjects() {
		super.initObjects();
		astUtils = new AttributeServiceTestUtils();
	}
	

	@Test
	public void testSimpleAttribute() throws Exception {

		final Attribute attribute = astUtils.createAttribute("http://purl.org/dc/terms/title", "title");
		
		updateObjectTransactional( attribute );

		final Attribute updatedAttribute = getObject( attribute );

		Assert.assertNotNull("the attribute name of the updated resource shouldn't be null", updatedAttribute.getName());
		Assert.assertEquals("the attribute's name are not equal", attribute.getName(), updatedAttribute.getName());

		String json = null;

		try {

			json = objectMapper.writeValueAsString(updatedAttribute);
		} catch( final JsonProcessingException e ) {

			e.printStackTrace();
		}

		AttributeServiceTest.LOG.debug("attribute json: " + json);
	}


	@Test
	public void testUniquenessOfAttributes() throws Exception {

		final Attribute attribute1 = createAttribute();
		final Attribute attribute2 = createAttribute();

		Assert.assertNotNull("attribute1 shouldn't be null", attribute1);
		Assert.assertNotNull("attribute2 shouldn't be null", attribute2);
		Assert.assertNotNull("attribute1 id shouldn't be null", attribute1.getId());
		Assert.assertNotNull("attribute2 id shouldn't be null", attribute2.getId());
		Assert.assertEquals("the attributes should be equal", attribute1, attribute2);
		Assert.assertNotNull("attribute1 uri shouldn't be null", attribute1.getUri());
		Assert.assertNotNull("attribute2 uri shouldn't be null", attribute2.getUri());
		Assert.assertNotNull("attribute1 uri shouldn't be empty", attribute1.getUri().trim().isEmpty());
		Assert.assertNotNull("attribute2 uri shouldn't be empty", attribute2.getUri().trim().isEmpty());
		Assert.assertEquals("the attribute uris should be equal", attribute1.getUri(), attribute2.getUri());
		Assert.assertNotNull("attribute1 uri shouldn't be null", attribute1.getName());
		Assert.assertNotNull("attribute2 uri shouldn't be null", attribute2.getName());
		Assert.assertNotNull("attribute1 uri shouldn't be empty", attribute1.getName().trim().isEmpty());
		Assert.assertNotNull("attribute2 uri shouldn't be empty", attribute2.getName().trim().isEmpty());
		Assert.assertEquals("the attribute uris should be equal", attribute1.getName(), attribute2.getName());
	}


	private Attribute createAttribute() throws Exception {

		final Attribute attribute = astUtils.createAttribute("http://purl.org/dc/terms/title", "title");

		updateObjectTransactional(attribute);

		final Attribute updatedAttribute = getObject(attribute);

		return updatedAttribute;
	}
}
