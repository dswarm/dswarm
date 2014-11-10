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
import org.dswarm.persistence.model.schema.MappingAttributePathInstance;
import org.dswarm.persistence.model.schema.proxy.ProxyMappingAttributePathInstance;
import org.dswarm.persistence.service.schema.MappingAttributePathInstanceService;
import org.dswarm.persistence.service.schema.test.utils.MappingAttributePathInstanceServiceTestUtils;
import org.dswarm.persistence.service.test.IDBasicJPAServiceTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
		
		MappingAttributePathInstance mapi = mapisUtils.createDefaultMappingAttributePathInstance();
		mapi.setOrdinal( mapisUtils.createDefaultOrdinal() );
		mapi.setFilter( mapisUtils.createDefaultFilter() );

		final MappingAttributePathInstance updatedMappingAttributePathInstance = updateObjectTransactional( mapi ).getObject();

		mapisUtils.compareObjects( mapi, updatedMappingAttributePathInstance );
		
//		Assert.assertNotNull(
//				"the mapping attribute path instance's attribute paths of the updated mapping attribute path instance shouldn't be null",
//				updatedMappingAttributePathInstance.getAttributePath());
//		Assert.assertEquals("the mapping attribute path instance's attribute paths are not equal",
//				mappingAttributePathInstance.getAttributePath(), updatedMappingAttributePathInstance.getAttributePath());
//		Assert.assertNotNull("the attribute path's attributes of the attribute path '" + attributePath1.getId()
//				+ "' of the updated mapping attribute path instance shouldn't be null", updatedMappingAttributePathInstance.getAttributePath()
//				.getAttributes());
//		Assert.assertEquals("the attribute path's attributes size of attribute path '" + attributePath1.getId() + "' are not equal",
//				attributePath1.getAttributes(), updatedMappingAttributePathInstance.getAttributePath().getAttributes());
//		Assert.assertEquals("the first attributes of attribute path '" + attributePath1.getId() + "' are not equal", attributePath1
//				.getAttributePath().get(0), updatedMappingAttributePathInstance.getAttributePath().getAttributePath().get(0));
//		Assert.assertNotNull("the attribute path string of attribute path '" + attributePath1.getId()
//				+ "' of the updated mapping attribute path instance shouldn't be null", updatedMappingAttributePathInstance.getAttributePath()
//				.toAttributePath());
//		Assert.assertEquals("the attribute path's strings attribute path '" + attributePath1.getId() + "' are not equal",
//				attributePath1.toAttributePath(), updatedMappingAttributePathInstance.getAttributePath().toAttributePath());
//		Assert.assertNotNull("the mapping attribute path instance's ordinals of the updated mapping attribute path instance shouldn't be null",
//				updatedMappingAttributePathInstance.getAttributePath());
//		Assert.assertEquals("the mapping attribute path instance's ordinals are not equal", mappingAttributePathInstance.getOrdinal(),
//				updatedMappingAttributePathInstance.getOrdinal());
//		Assert.assertNotNull("the mapping attribute path instance's filters of the updated mapping attribute path instance shouldn't be null",
//				updatedMappingAttributePathInstance.getFilter());
//		Assert.assertEquals("the mapping attribute path instance's filters are not equal", mappingAttributePathInstance.getFilter(),
//				updatedMappingAttributePathInstance.getFilter());
//		Assert.assertEquals("the mapping attribute path instance's filter's expressions are not equal", mappingAttributePathInstance
//				.getFilter().getExpression(), updatedMappingAttributePathInstance.getFilter().getExpression());

		String json = null;
		try {
			json = objectMapper.writeValueAsString( mapi );
		} catch( final JsonProcessingException e ) {
			LOG.error( e.getMessage(), e );
		}
		MappingAttributePathInstanceServiceTest.LOG.debug("mapping attribute path instance json: " + json);
	}
}
