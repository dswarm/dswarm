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
package org.dswarm.persistence.service.job.test;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.persistence.model.job.Mapping;
import org.dswarm.persistence.model.job.proxy.ProxyMapping;
import org.dswarm.persistence.service.job.MappingService;
import org.dswarm.persistence.service.job.test.utils.MappingServiceTestUtils;
import org.dswarm.persistence.service.test.IDBasicJPAServiceTest;

public class MappingServiceTest extends IDBasicJPAServiceTest<ProxyMapping, Mapping, MappingService> {

	private static final Logger LOG = LoggerFactory.getLogger(MappingServiceTest.class);

	private MappingServiceTestUtils mappingServiceTestUtils;

	public MappingServiceTest() {

		super("mapping", MappingService.class);
	}

	@Override
	protected void initObjects() {

		super.initObjects();

		mappingServiceTestUtils = new MappingServiceTestUtils();
	}

	@Test
	public void testSimpleObject() throws Exception {

		MappingServiceTest.LOG.debug("start simple mapping test");

		final Mapping mapping = mappingServiceTestUtils.createDefaultObject();

		final Mapping updatedMapping = mappingServiceTestUtils.updateAndCompareObject(mapping, mapping);

		logObjectJSON(updatedMapping);

		MappingServiceTest.LOG.debug("end simple mappping test");
	}

	@Test
	public void complexMappingTest() throws Exception {

		MappingServiceTest.LOG.debug("start complex mapping test");

		final Mapping mapping = mappingServiceTestUtils.createDefaultCompleteObject();

		final Mapping updatedMapping = mappingServiceTestUtils.updateAndCompareObject(mapping, mapping);

		logObjectJSON(updatedMapping);

		// [@tgaengler]: no, we can't print this right now, or? (because we cannot make logObjectJSON static - otherwise, we could utilise logObjectJSON from *ServiceTestUtils
		//		try {
		//
		//			json = objectMapper.writeValueAsString(transformation2);
		//		} catch (final JsonProcessingException e) {
		//
		//			e.printStackTrace();
		//		}
		//
		//		MappingServiceTest.LOG.debug("transformation json: " + json);
		//
		//		try {
		//
		//			json = objectMapper.writeValueAsString(transformation);
		//		} catch (final JsonProcessingException e) {
		//
		//			e.printStackTrace();
		//		}
		//
		//		MappingServiceTest.LOG.debug("clean-up transformation json: " + json);
		//
		//		try {
		//
		//			json = objectMapper.writeValueAsString(component1);
		//		} catch (final JsonProcessingException e) {
		//
		//			e.printStackTrace();
		//		}
		//
		//		MappingServiceTest.LOG.debug("clean-up previous component json: " + json);
		//
		//		try {
		//
		//			json = objectMapper.writeValueAsString(component);
		//		} catch (final JsonProcessingException e) {
		//
		//			e.printStackTrace();
		//		}
		//
		//		MappingServiceTest.LOG.debug("clean-up main component json: " + json);
		//
		//		try {
		//
		//			json = objectMapper.writeValueAsString(component2);
		//		} catch (final JsonProcessingException e) {
		//
		//			e.printStackTrace();
		//		}
		//
		//		MappingServiceTest.LOG.debug("clean-up next component json: " + json);

		MappingServiceTest.LOG.debug("end complex mapping test");
	}
}
