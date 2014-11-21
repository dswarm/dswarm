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

import org.dswarm.persistence.model.job.Transformation;
import org.dswarm.persistence.model.job.proxy.ProxyTransformation;
import org.dswarm.persistence.service.job.TransformationService;
import org.dswarm.persistence.service.job.test.utils.TransformationServiceTestUtils;
import org.dswarm.persistence.service.test.IDBasicJPAServiceTest;

public class TransformationServiceTest extends IDBasicJPAServiceTest<ProxyTransformation, Transformation, TransformationService> {

	private static final Logger LOG = LoggerFactory.getLogger(TransformationServiceTest.class);

	private TransformationServiceTestUtils transformationServiceTestUtils;

	public TransformationServiceTest() {

		super("transformation", TransformationService.class);
	}

	@Override protected void initObjects() {
		super.initObjects();

		transformationServiceTestUtils = new TransformationServiceTestUtils();
	}

	@Test
	@Override
	public void testSimpleObject() throws Exception {

		TransformationServiceTest.LOG.debug("start simple transformation test");

		final Transformation transformation = transformationServiceTestUtils.createAndPersistDefaultObject();

		final Transformation updatedTransformation = transformationServiceTestUtils.updateAndCompareObject(transformation, transformation);

		logObjectJSON(updatedTransformation);

		TransformationServiceTest.LOG.debug("end simple transformation test");
	}

	@Test
	public void complexTransformationTest() throws Exception {

		TransformationServiceTest.LOG.debug("start complex transformation test");

		final Transformation transformation = transformationServiceTestUtils.createAndPeristDefaultCompleteObject();

		final Transformation updatedTransformation = transformationServiceTestUtils.updateAndCompareObject(transformation, transformation);

		logObjectJSON(updatedTransformation);

		// [@tgaengler]: no, we can't print this right now, or? (because we cannot make logObjectJSON static - otherwise, we could utilise logObjectJSON from *ServiceTestUtils
		//		TransformationServiceTest.LOG.debug("transformation json: " + json);
		//
		//		try {
		//
		//			json = objectMapper.writeValueAsString(component1);
		//		} catch (final JsonProcessingException e) {
		//
		//			e.printStackTrace();
		//		}
		//
		//		TransformationServiceTest.LOG.debug("previous component json: " + json);
		//
		//		try {
		//
		//			json = objectMapper.writeValueAsString(component);
		//		} catch (final JsonProcessingException e) {
		//
		//			e.printStackTrace();
		//		}
		//
		//		TransformationServiceTest.LOG.debug("main component json: " + json);
		//
		//		try {
		//
		//			json = objectMapper.writeValueAsString(component2);
		//		} catch (final JsonProcessingException e) {
		//
		//			e.printStackTrace();
		//		}
		//
		//		TransformationServiceTest.LOG.debug("next component json: " + json);

		TransformationServiceTest.LOG.debug("end complex transformation test");
	}
}
