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
package org.dswarm.persistence.service.schema.test.utils;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Assert;

import org.dswarm.init.util.DMPStatics;
import org.dswarm.persistence.model.schema.Attribute;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.AttributePathInstance;
import org.dswarm.persistence.model.schema.AttributePathInstanceType;
import org.dswarm.persistence.model.schema.proxy.ProxyAttributePathInstance;
import org.dswarm.persistence.service.schema.AttributePathInstanceService;
import org.dswarm.persistence.service.test.utils.BasicDMPJPAServiceTestUtils;

public abstract class AttributePathInstanceServiceTestUtils<POJOCLASSPERSISTENCESERVICE extends AttributePathInstanceService<PROXYPOJOCLASS, POJOCLASS>, PROXYPOJOCLASS extends ProxyAttributePathInstance<POJOCLASS>, POJOCLASS extends AttributePathInstance>
		extends BasicDMPJPAServiceTestUtils<POJOCLASSPERSISTENCESERVICE, PROXYPOJOCLASS, POJOCLASS> {

	protected AttributePathServiceTestUtils	attributePathServiceTestUtils;
	
	protected abstract POJOCLASS createAttributePathInstance( final String name, final AttributePath attributePath, final JsonNode objectDescription ) throws Exception;
	
	
	public AttributePathInstanceServiceTestUtils(final Class<POJOCLASS> pojoClassArg,
			final Class<POJOCLASSPERSISTENCESERVICE> persistenceServiceClassArg) {

		super(pojoClassArg, persistenceServiceClassArg);

		attributePathServiceTestUtils = new AttributePathServiceTestUtils();
	}

	protected AttributePathServiceTestUtils getAttributePathServiceTestUtils() {

		return attributePathServiceTestUtils;
	}

	/**
	 * {@inheritDoc} <br />
	 * Assert both object's {@link AttributePathInstanceType}s are equal. <br />
	 * Assert that either both objects have no {@link AttributePath}s, or (in case {@link AttributePath}s are present), both
	 * {@link AttributePath} have either no {@link Attribute}s or the same number of {@link Attribute}s and the {@link Attribute}s
	 * are equal regarding id and name.
	 * 
	 * @param expectedObject
	 * @param actualObject
	 */
	@Override
	public void compareObjects(final POJOCLASS expectedObject, final POJOCLASS actualObject) {

		super.compareObjects(expectedObject, actualObject);

		Assert.assertEquals("the " + pojoClassName + " attribute path instance types should be equal", expectedObject.getAttributePathInstanceType(),
				actualObject.getAttributePathInstanceType());

		if (expectedObject.getAttributePath() == null) {

			Assert.assertNull("the actual attribute path instance should be null", actualObject.getAttributePath());

		} else {

			attributePathServiceTestUtils.compareObjects(expectedObject.getAttributePath(), actualObject.getAttributePath());
		}
	}

	/**
	 * {@inheritDoc}<br/>
	 */
	@Override
	protected POJOCLASS prepareObjectForUpdate(final POJOCLASS objectWithUpdates, final POJOCLASS object) {

		super.prepareObjectForUpdate(objectWithUpdates, object);

		object.setAttributePath(objectWithUpdates.getAttributePath());

		return object;
	}

	@Override
	public void reset() {

		attributePathServiceTestUtils.reset();
	}
	
	
	
	@Override
	public POJOCLASS createObject(final JsonNode objectDescription) throws Exception {
		//TODO externalize valid keys
		final String name = objectDescription.get("name") != null ? objectDescription.get("name").asText( null ) : null;
		
		final StringBuilder keySB = new StringBuilder();
		//TODO externalize valid keys
		final List<String> temp = new ArrayList<>();
		int i = 1;
		final JsonNode attributeIds = objectDescription.get( "attribute_ids" );
		for( final JsonNode jn : attributeIds) {
			temp.add( jn.asText() );

			keySB.append(jn.asText());

			if(i < attributeIds.size()) {

				keySB.append(DMPStatics.ATTRIBUTE_DELIMITER);
			}
		}

		final String key = keySB.toString();
		
		if( !cache.containsKey(key) ) {
			final AttributePath ap = getAttributePath(temp.toArray(new String[temp.size()]));
			cache.put( key, createAttributePathInstance( name, ap, objectDescription ) );
		}

		return cache.get(key);
	}
	
	
	protected AttributePath getAttributePath( final String... attributeIds ) throws Exception {
		return attributePathServiceTestUtils.getAttributePath(attributeIds);
	}
}
