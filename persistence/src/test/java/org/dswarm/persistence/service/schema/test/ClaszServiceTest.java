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

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.dswarm.persistence.GuicedTest;
import org.dswarm.persistence.model.schema.Clasz;
import org.dswarm.persistence.model.schema.proxy.ProxyClasz;
import org.dswarm.persistence.service.schema.ClaszService;
import org.dswarm.persistence.service.schema.test.utils.ClaszServiceTestUtils;
import org.dswarm.persistence.service.test.AdvancedJPAServiceTest;

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
		final Clasz clasz = cstUtils.getDefaultObject();
		final Clasz updatedClass = cstUtils.updateObject(clasz, clasz);

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

		Assert.assertEquals("the attribute uris should be equal", clasz1.getName(), clasz2.getName());
	}


	private Clasz createAndUpdateClass() throws Exception {
		final Clasz clasz = cstUtils.createObject(ClaszServiceTestUtils.BIBO_DOCUMENT, "document");
		return cstUtils.updateObject(clasz, clasz);
	}
}
