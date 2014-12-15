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

import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.persistence.model.schema.Attribute;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.proxy.ProxyAttributePath;
import org.dswarm.persistence.service.schema.AttributePathService;
import org.dswarm.persistence.service.schema.test.utils.AttributePathServiceTestUtils;
import org.dswarm.persistence.service.test.BasicJPAServiceTest;

public class AttributePathServiceTest extends BasicJPAServiceTest<ProxyAttributePath, AttributePath, AttributePathService> {

	private static final Logger LOG = LoggerFactory.getLogger(AttributePathServiceTest.class);

	private AttributePathServiceTestUtils apstUtils;

	public AttributePathServiceTest() {
		super("attribute path", AttributePathService.class);
	}

	@Override
	protected void initObjects() {
		super.initObjects();
		apstUtils = new AttributePathServiceTestUtils();
	}

	@Test
	public void testSimpleAttributePath2() throws Exception {
		AttributePathServiceTest.LOG.debug("start simple attribute path test 2");

		apstUtils.createAndPersistDefaultObject();

		AttributePathServiceTest.LOG.debug("end simple attribute path test 2");
	}

	@Test
	public void testUniquenessOfAttributePath() throws Exception {
		AttributePathServiceTest.LOG.debug("start uniqueness of attribute path test");

		final AttributePath attributePath1 = apstUtils.getNonCachedDctermsTitleDctermsHaspartDctermsTitleAP();
		final AttributePath attributePath2 = apstUtils.getNonCachedDctermsTitleDctermsHaspartDctermsTitleAP();

		Assert.assertEquals("ids of attribute paths should be equal", attributePath1.getId(), attributePath2.getId());

		final String jsonPath = attributePath1.getAttributePathAsJSONObjectString();

		final List<AttributePath> attributePathList = jpaService.getAttributePathsWithPath(jsonPath);
		AttributePathServiceTest.LOG.debug("Number of AttributePath instances with the identical path" + jsonPath + ":"
				+ attributePathList.size());
		Assert.assertTrue("There is more than one AttributePath instance with an identical path!", attributePathList.size() == 1);

		final Set<Attribute> attributeSet1 = attributePath1.getAttributes();
		Assert.assertEquals("the attribute path's attributes size is not 2", 2, attributeSet1.size());
		Assert.assertFalse("the attribute path's attributes set shouldn't be empty", attributeSet1.isEmpty());

		AttributePathServiceTest.LOG.debug("start uniquness of attribute path test");
	}

	@Test
	@Override
	public void testSimpleObject() throws Exception {
		AttributePathServiceTest.LOG.debug("start simple attribute path test");

		final AttributePath attributePath = apstUtils.createAndPersistDefaultObject();
		final AttributePath updatedAttributePath = apstUtils.updateAndCompareObject(attributePath, attributePath);

		logObjectJSON(updatedAttributePath);

		AttributePathServiceTest.LOG.debug("end simple attribute path test");
	}
}
