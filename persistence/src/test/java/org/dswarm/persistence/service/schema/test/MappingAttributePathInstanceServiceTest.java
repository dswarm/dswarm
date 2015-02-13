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
package org.dswarm.persistence.service.schema.test;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.persistence.model.schema.MappingAttributePathInstance;
import org.dswarm.persistence.model.schema.proxy.ProxyMappingAttributePathInstance;
import org.dswarm.persistence.service.schema.MappingAttributePathInstanceService;
import org.dswarm.persistence.service.schema.test.utils.MappingAttributePathInstanceServiceTestUtils;
import org.dswarm.persistence.service.test.BasicJPAServiceTest;

public class MappingAttributePathInstanceServiceTest extends
		BasicJPAServiceTest<ProxyMappingAttributePathInstance, MappingAttributePathInstance, MappingAttributePathInstanceService> {

	private static final Logger LOG = LoggerFactory.getLogger(SchemaServiceTest.class);

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
	@Override
	public void testSimpleObject() throws Exception {

		final MappingAttributePathInstance mapi = mapisUtils.createAndPersistDefaultCompleteObject();

		final MappingAttributePathInstance updatedMappingAttributePathInstance = mapisUtils.updateAndCompareObject(mapi, mapi);

		logObjectJSON(updatedMappingAttributePathInstance);
	}
}
