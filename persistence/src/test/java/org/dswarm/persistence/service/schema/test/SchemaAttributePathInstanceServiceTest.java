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

import java.util.Set;

import org.dswarm.persistence.GuicedTest;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.SchemaAttributePathInstance;
import org.dswarm.persistence.model.schema.proxy.ProxySchemaAttributePathInstance;
import org.dswarm.persistence.service.schema.SchemaAttributePathInstanceService;
import org.dswarm.persistence.service.schema.test.utils.AttributePathServiceTestUtils;
import org.dswarm.persistence.service.schema.test.utils.SchemaAttributePathInstanceServiceTestUtils;
import org.dswarm.persistence.service.test.IDBasicJPAServiceTest;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Sets;

public class SchemaAttributePathInstanceServiceTest extends
		IDBasicJPAServiceTest<ProxySchemaAttributePathInstance, SchemaAttributePathInstance, SchemaAttributePathInstanceService> {

	private static final Logger LOG = LoggerFactory.getLogger(SchemaServiceTest.class);

	private final ObjectMapper objectMapper = GuicedTest.injector.getInstance(ObjectMapper.class);

	// TODO: generalize
	private SchemaAttributePathInstanceServiceTestUtils schemaAttributePathInstanceServiceTestUtils;


	public SchemaAttributePathInstanceServiceTest() {
		super("schema attribute path instance", SchemaAttributePathInstanceService.class);
	}


	@Override
	protected void initObjects() {
		super.initObjects();

		// TODO: generalize
		schemaAttributePathInstanceServiceTestUtils = new SchemaAttributePathInstanceServiceTestUtils();
	}
	
	@Test
	public void idGenerationTest2() throws Exception {

		LOG.debug("start id generation test for " + type);

		final Set<SchemaAttributePathInstance> objectes = Sets.newLinkedHashSet();
		
		final AttributePathServiceTestUtils apstu = new AttributePathServiceTestUtils();

		for (int i = 0; i < 10; i++) {
			
			final AttributePath ap = apstu.getAttributePath("http://purl.org/dc/terms/title", "http://purl.org/dc/terms/hasPart", "http://purl.org/dc/terms/title");

			final ProxySchemaAttributePathInstance proxyObject = jpaService.createObjectTransactional(ap);

			objectes.add(proxyObject.getObject());
		}

		Assert.assertEquals(type + "s set size should be 10", 10, objectes.size());

		// clean-up DB table
		for (final SchemaAttributePathInstance object : objectes) {

			jpaService.deleteObject(object.getId());
		}

		LOG.debug("end id generation test for " + type);
	}


	@Test
	public void testSimpleSchemaAttributePathInstance() throws Exception {

		ObjectNode objectDescription = objectMapper.createObjectNode();
		
		ArrayNode attributeIds = objectMapper.createArrayNode();
		attributeIds.add( "http://purl.org/dc/terms/title" );
		attributeIds.add( "http://purl.org/dc/terms/hasPart" );
		attributeIds.add( "http://purl.org/dc/terms/title" );
		
		objectDescription.set( "attribute_ids", attributeIds );
		
		SchemaAttributePathInstance mapi = schemaAttributePathInstanceServiceTestUtils.getObject( objectDescription );

		// update mapping attribute path instance

		final SchemaAttributePathInstance updatedMappingAttributePathInstance = updateObjectTransactional(mapi).getObject();

		// Assert.assertNotNull( "the mapping attribute path instance's attribute paths of the updated mapping attribute path instance shouldn't be null", updatedMappingAttributePathInstance.getAttributePath() );
		// Assert.assertEquals("the mapping attribute path instance's attribute paths are not equal",
		// mappingAttributePathInstance.getAttributePath(),
		// updatedMappingAttributePathInstance.getAttributePath());
		// Assert.assertNotNull("the attribute path's attributes of the attribute path '"
		// + attributePath1.getId()
		// +
		// "' of the updated mapping attribute path instance shouldn't be null",
		// updatedMappingAttributePathInstance.getAttributePath()
		// .getAttributes());
		// Assert.assertEquals("the attribute path's attributes size of attribute path '"
		// + attributePath1.getId() + "' are not equal",
		// attributePath1.getAttributes(),
		// updatedMappingAttributePathInstance.getAttributePath().getAttributes());
		// Assert.assertEquals("the first attributes of attribute path '" +
		// attributePath1.getId() + "' are not equal", attributePath1
		// .getAttributePath().get(0),
		// updatedMappingAttributePathInstance.getAttributePath().getAttributePath().get(0));
		// Assert.assertNotNull("the attribute path string of attribute path '"
		// + attributePath1.getId()
		// +
		// "' of the updated mapping attribute path instance shouldn't be null",
		// updatedMappingAttributePathInstance.getAttributePath()
		// .toAttributePath());
		// Assert.assertEquals("the attribute path's strings attribute path '" +
		// attributePath1.getId() + "' are not equal",
		// attributePath1.toAttributePath(),
		// updatedMappingAttributePathInstance.getAttributePath().toAttributePath());
		// Assert.assertNotNull("the mapping attribute path instance's ordinals of the updated mapping attribute path instance shouldn't be null",
		// updatedMappingAttributePathInstance.getAttributePath());
		// Assert.assertEquals("the mapping attribute path instance's ordinals are not equal",
		// mappingAttributePathInstance.getOrdinal(),
		// updatedMappingAttributePathInstance.getOrdinal());
		// Assert.assertNotNull("the mapping attribute path instance's filters of the updated mapping attribute path instance shouldn't be null",
		// updatedMappingAttributePathInstance.getFilter());
		// Assert.assertEquals("the mapping attribute path instance's filters are not equal",
		// mappingAttributePathInstance.getFilter(),
		// updatedMappingAttributePathInstance.getFilter());
		// Assert.assertEquals("the mapping attribute path instance's filter's expressions are not equal",
		// mappingAttributePathInstance.getFilter()
		// .getExpression(),
		// updatedMappingAttributePathInstance.getFilter().getExpression());

		String json = null;

		try {

			json = objectMapper.writeValueAsString(updatedMappingAttributePathInstance);
		} catch( final JsonProcessingException e ) {

			e.printStackTrace();
		}

		SchemaAttributePathInstanceServiceTest.LOG.debug("schema attribute path instance json: " + json);
	}
}
