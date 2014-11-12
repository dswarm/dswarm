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
import org.dswarm.persistence.model.schema.MappingAttributePathInstance;
import org.dswarm.persistence.model.schema.proxy.ProxyMappingAttributePathInstance;
import org.dswarm.persistence.service.schema.MappingAttributePathInstanceService;
import org.dswarm.persistence.service.schema.test.utils.MappingAttributePathInstanceServiceTestUtils;
import org.dswarm.persistence.service.test.IDBasicJPAServiceTest;

public class MappingAttributePathInstanceServiceTest extends
		IDBasicJPAServiceTest<ProxyMappingAttributePathInstance, MappingAttributePathInstance, MappingAttributePathInstanceService> {

	private static final Logger LOG = LoggerFactory.getLogger(SchemaServiceTest.class);

	private final ObjectMapper objectMapper = GuicedTest.injector.getInstance(ObjectMapper.class);

	private MappingAttributePathInstanceServiceTestUtils mapisUtils;

	public MappingAttributePathInstanceServiceTest() {
		super("mapping attribute path instance", MappingAttributePathInstanceService.class);
	}

	@Override
	protected void initObjects() {
		super.initObjects();
		mapisUtils = new MappingAttributePathInstanceServiceTestUtils();
	}

	@Test
	public void testSimpleMappingAttributePathInstance() throws Exception {

		final MappingAttributePathInstance mapi = mapisUtils.createDefaultCompleteObject();

		final MappingAttributePathInstance updatedMappingAttributePathInstance = mapisUtils.updateAndCompareObject(mapi, mapi);

		String json = null;
		try {
			json = objectMapper.writeValueAsString(updatedMappingAttributePathInstance);
		} catch (final JsonProcessingException e) {
			LOG.error(e.getMessage(), e);
		}
		MappingAttributePathInstanceServiceTest.LOG.debug("mapping attribute path instance json: " + json);
	}
}
