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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.json.JSONException;
import org.junit.Assert;

import org.dswarm.persistence.model.job.Filter;
import org.dswarm.persistence.model.schema.Attribute;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.MappingAttributePathInstance;
import org.dswarm.persistence.model.schema.proxy.ProxyMappingAttributePathInstance;
import org.dswarm.persistence.service.job.test.utils.FilterServiceTestUtils;
import org.dswarm.persistence.service.schema.MappingAttributePathInstanceService;

public class MappingAttributePathInstanceServiceTestUtils
		extends
		AttributePathInstanceServiceTestUtils<MappingAttributePathInstanceService, ProxyMappingAttributePathInstance, MappingAttributePathInstance> {

	private final FilterServiceTestUtils fstUtils;

	public MappingAttributePathInstanceServiceTestUtils() {

		super(MappingAttributePathInstance.class, MappingAttributePathInstanceService.class);

		fstUtils = new FilterServiceTestUtils();
	}

	@Override public MappingAttributePathInstance createObject(String identifier) throws Exception {
		return null;
	}

	@Override public MappingAttributePathInstance createDefaultObject() throws Exception {

		return createMappingAttributePathInstance(attributePathServiceTestUtils.createDefaultObject());
	}

	@Override public MappingAttributePathInstance createDefaultCompleteObject() throws Exception {
		final MappingAttributePathInstance mapi = createDefaultObject();
		mapi.setOrdinal(1);
		mapi.setFilter(fstUtils.createDefaultObject());

		return updateAndCompareObject(mapi, mapi);
	}

	/**
	 * {@inheritDoc}<br />
	 * Assert ordinals are equal. <br />
	 * Assert either no filters are present or filters are equal, see
	 * {@link FilterServiceTestUtils#compareObjects(Filter, Filter)} for details.
	 *
	 * @param expectedMappingAttributePathInstance
	 * @param actualMappingAttributePathInstance
	 */
	@Override
	public void compareObjects(final MappingAttributePathInstance expectedMappingAttributePathInstance,
			final MappingAttributePathInstance actualMappingAttributePathInstance) throws JsonProcessingException, JSONException {

		super.compareObjects(expectedMappingAttributePathInstance, actualMappingAttributePathInstance);

		if (expectedMappingAttributePathInstance.getOrdinal() != null) {

			Assert.assertNotNull(actualMappingAttributePathInstance.getOrdinal());
			Assert.assertEquals("the ordinals of the mapping attribute path should be equal", expectedMappingAttributePathInstance.getOrdinal(),
					actualMappingAttributePathInstance.getOrdinal());
		} else {

			Assert.assertNull(actualMappingAttributePathInstance.getOrdinal());
		}

		if (expectedMappingAttributePathInstance.getFilter() == null) {

			Assert.assertNull("the actual mapping attribute path instance should not have a filter",
					actualMappingAttributePathInstance.getFilter());

		} else {

			fstUtils.compareObjects(expectedMappingAttributePathInstance.getFilter(),
					actualMappingAttributePathInstance.getFilter());
		}
	}

	public MappingAttributePathInstance createMappingAttributePathInstance(final String name, final AttributePath attributePath,
			final Integer ordinal, final Filter filter) throws Exception {
		final MappingAttributePathInstance mappingAttributePathInstance = new MappingAttributePathInstance();
		mappingAttributePathInstance.setName(name);
		mappingAttributePathInstance.setAttributePath(attributePath);
		mappingAttributePathInstance.setOrdinal(ordinal);
		mappingAttributePathInstance.setFilter(filter);
		final MappingAttributePathInstance updatedMappingAttributePathInstance = createAndCompareObject(mappingAttributePathInstance,
				mappingAttributePathInstance);
		Assert.assertNotNull(updatedMappingAttributePathInstance.getId());
		return updatedMappingAttributePathInstance;
	}

	@Override
	protected MappingAttributePathInstance createAttributePathInstance(String name, AttributePath attributePath, JsonNode objectDescription)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public MappingAttributePathInstance createMappingAttributePathInstance(final AttributePath attributePath) throws Exception {
		return createMappingAttributePathInstance(null, attributePath, 0, null);
	}

	/**
	 * Convenience method
	 */
	public MappingAttributePathInstance createMappingAttributePathInstance(final Attribute attribute) throws Exception {
		AttributePath attributePath = attributePathServiceTestUtils.createAttributePath(attribute);
		return createMappingAttributePathInstance(attributePath);
	}

	/**
	 * {@inheritDoc}<br/>
	 */
	@Override
	protected MappingAttributePathInstance prepareObjectForUpdate(final MappingAttributePathInstance objectWithUpdates,
			final MappingAttributePathInstance object) {

		super.prepareObjectForUpdate(objectWithUpdates, object);

		object.setFilter(objectWithUpdates.getFilter());
		object.setOrdinal(objectWithUpdates.getOrdinal());

		return object;
	}

	@Override
	public void reset() {

		super.reset();
		fstUtils.reset();
	}

	public MappingAttributePathInstance getDefaultInputMAPI() throws Exception {

		final AttributePath inputAttributePath = attributePathServiceTestUtils.getDctermsTitleAP();

		return createMappingAttributePathInstance("input mapping attribute path instance", inputAttributePath, null, null);
	}

	public MappingAttributePathInstance getDefaultOutputMAPI() throws Exception {

		final AttributePath outputAttributePath = attributePathServiceTestUtils.getRDFSLabelAP();

		return createMappingAttributePathInstance("output mapping attribute path instance", outputAttributePath, null, null);
	}

	public MappingAttributePathInstance getDctermsCreatorFoafFirstnameMAPI() throws Exception {

		final AttributePath dctermsCreatorFoafFirstnameAP = attributePathServiceTestUtils.getDctermsCreatorFoafFirstnameAP();

		return createMappingAttributePathInstance("dcterms:creator/foaf:firstName MAPI", dctermsCreatorFoafFirstnameAP, null, null);
	}

	public MappingAttributePathInstance getDctermsCreatorFoafFamilynameMAPI() throws Exception {

		final AttributePath dctermsCreatorFoafFamilynameAP = attributePathServiceTestUtils.getDctermsCreatorFoafFamilynameAP();

		return createMappingAttributePathInstance("dcterms:creator/foaf:familyName MAPI", dctermsCreatorFoafFamilynameAP, null, null);
	}

	public MappingAttributePathInstance getDctermsCreatorFoafNameMAPI() throws Exception {

		final AttributePath dctermsCreatorFoafMameAP = attributePathServiceTestUtils.getDctermsCreatorFoafNameAP();

		return createMappingAttributePathInstance("dcterms:creator/foaf:name MAPI", dctermsCreatorFoafMameAP, null, null);
	}

}

