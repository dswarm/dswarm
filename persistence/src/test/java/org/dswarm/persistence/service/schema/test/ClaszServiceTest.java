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

import static org.dswarm.persistence.service.schema.test.utils.ClaszServiceTestUtils.CLASS__DOCUMENT;

import org.dswarm.persistence.GuicedTest;
import org.dswarm.persistence.model.schema.Clasz;
import org.dswarm.persistence.model.schema.proxy.ProxyClasz;
import org.dswarm.persistence.service.schema.ClaszService;
import org.dswarm.persistence.service.schema.test.utils.ClaszServiceTestUtils;
import org.dswarm.persistence.service.test.AdvancedJPAServiceTest;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClaszServiceTest extends AdvancedJPAServiceTest<ProxyClasz, Clasz, ClaszService> {

	private static final Logger LOG = LoggerFactory.getLogger(ClaszServiceTest.class);

	private final ObjectMapper objectMapper = GuicedTest.injector.getInstance(ObjectMapper.class);

	private ClaszServiceTestUtils cstUtils;

	public ClaszServiceTest() {
		super("class", ClaszService.class);
	}
	
	
	@Override
	protected void initObjects() {
		super.initObjects();
		cstUtils = new ClaszServiceTestUtils();
	}


	@Test
	public void testSimpleAttribute() throws Exception {
		final Clasz clasz = cstUtils.createClass( CLASS__DOCUMENT );
		final Clasz updatedClass = updateObjectTransactional(clasz).getObject();

		cstUtils.compareObjects( clasz, updatedClass);

		Assert.assertNotNull("the attribute name of the updated resource shouldn't be null", updatedClass.getName());
//		Assert.assertEquals("the attribute's name are not equal", clasz.getName(), updatedClass.getName());

		String json = null;
		try {

			json = objectMapper.writeValueAsString(updatedClass);
		} catch( final JsonProcessingException e ) {
			LOG.error( e.getMessage(), e );
		}
		ClaszServiceTest.LOG.debug("class json: " + json);
	}


	@Test
	public void testUniquenessOfClasses() throws Exception {

		final Clasz clasz1 = createAndUpdateClass();
		final Clasz clasz2 = createAndUpdateClass();

		Assert.assertNotNull("attribute1 shouldn't be null", clasz1);
		Assert.assertNotNull("attribute2 shouldn't be null", clasz2);
		Assert.assertNotNull("attribute1 id shouldn't be null", clasz1.getId());
		Assert.assertNotNull("attribute2 id shouldn't be null", clasz2.getId());
		Assert.assertEquals("the attributes should be equal", clasz1, clasz2);
		Assert.assertNotNull("attribute1 uri shouldn't be null", clasz1.getUri());
		Assert.assertNotNull("attribute2 uri shouldn't be null", clasz2.getUri());
		Assert.assertNotNull("attribute1 uri shouldn't be empty", clasz1.getUri().trim().isEmpty());
		Assert.assertNotNull("attribute2 uri shouldn't be empty", clasz2.getUri().trim().isEmpty());
		Assert.assertEquals("the attribute uris should be equal", clasz1.getUri(), clasz2.getUri());
		Assert.assertNotNull("attribute1 uri shouldn't be null", clasz1.getName());
		Assert.assertNotNull("attribute2 uri shouldn't be null", clasz2.getName());
		Assert.assertNotNull("attribute1 uri shouldn't be empty", clasz1.getName().trim().isEmpty());
		Assert.assertNotNull("attribute2 uri shouldn't be empty", clasz2.getName().trim().isEmpty());
		Assert.assertEquals("the attribute uris should be equal", clasz1.getName(), clasz2.getName());
	}


	private Clasz createAndUpdateClass() throws Exception {
		final Clasz clasz = cstUtils.createClass( CLASS__DOCUMENT );
		return updateObjectTransactional(clasz).getObject();
	}
}
