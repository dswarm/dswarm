/**
 * Copyright (C) 2013 – 2017 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.persistence.model.schema.Attribute;
import org.dswarm.persistence.model.schema.proxy.ProxyAttribute;
import org.dswarm.persistence.service.schema.AttributeService;
import org.dswarm.persistence.service.schema.test.utils.AttributeServiceTestUtils;
import org.dswarm.persistence.service.test.AdvancedJPAServiceTest;

public class AttributeServiceTest extends AdvancedJPAServiceTest<ProxyAttribute, Attribute, AttributeService> {

	private static final Logger LOG = LoggerFactory.getLogger(AttributeServiceTest.class);

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
	@Override
	public void testSimpleObject() throws Exception {

		final Attribute attribute = astUtils.getDctermsTitle();
		final Attribute updatedAttribute = astUtils.updateAndCompareObject(attribute, attribute);

		logObjectJSON(updatedAttribute);
	}

	@Test
	public void testUniquenessOfAttributes() throws Exception {

		final Attribute attribute1 = createAndUpdateAttribute();
		final Attribute attribute2 = createAndUpdateAttribute();

		Assert.assertEquals("the attribute uris should be equal", attribute1.getName(), attribute2.getName());
	}

	private Attribute createAndUpdateAttribute() throws Exception {

		final Attribute attribute = astUtils.createObject(AttributeServiceTestUtils.DCTERMS_TITLE, "title");

		return astUtils.updateAndCompareObject(attribute, attribute);
	}
}
