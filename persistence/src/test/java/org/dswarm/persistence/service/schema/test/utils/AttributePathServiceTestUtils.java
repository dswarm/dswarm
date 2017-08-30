/**
 * Copyright (C) 2013 – 2017 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.json.JSONException;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.init.util.DMPStatics;
import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.schema.Attribute;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.proxy.ProxyAttributePath;
import org.dswarm.persistence.model.schema.utils.SchemaUtils;
import org.dswarm.persistence.service.schema.AttributePathService;
import org.dswarm.persistence.service.test.utils.BasicJPAServiceTestUtils;

public class AttributePathServiceTestUtils extends BasicJPAServiceTestUtils<AttributePathService, ProxyAttributePath, AttributePath> {

	private static final Logger LOG = LoggerFactory.getLogger(AttributePathServiceTestUtils.class);

	public static final String DCTERMS_TITLE__DCTERMS_HASPART__DCTERMS_TITLE_AP =
			AttributeServiceTestUtils.DCTERMS_TITLE + DMPStatics.ATTRIBUTE_DELIMITER
					+ AttributeServiceTestUtils.DCTERMS_HASPART + DMPStatics.ATTRIBUTE_DELIMITER + AttributeServiceTestUtils.DCTERMS_TITLE;

	public static final String DCTERMS_TITLE__DCTERMS_HASPART_AP =
			AttributeServiceTestUtils.DCTERMS_TITLE + DMPStatics.ATTRIBUTE_DELIMITER
					+ AttributeServiceTestUtils.DCTERMS_HASPART;

	public static final String DCTERMS_CREATOR__FOAF_FIRSTNAME_AP  =
			AttributeServiceTestUtils.DCTERMS_CREATOR + DMPStatics.ATTRIBUTE_DELIMITER + AttributeServiceTestUtils.FOAF_FIRSTNAME;
	public static final String DCTERMS_CREATOR__FOAF_FAMILYNAME_AP =
			AttributeServiceTestUtils.DCTERMS_CREATOR + DMPStatics.ATTRIBUTE_DELIMITER + AttributeServiceTestUtils.FOAF_FAMILYNAME;
	public static final String DCTERMS_CREATOR__FOAF_NAME_AP       =
			AttributeServiceTestUtils.DCTERMS_CREATOR + DMPStatics.ATTRIBUTE_DELIMITER + AttributeServiceTestUtils.FOAF_NAME;

	private final AttributeServiceTestUtils astUtils;

	private static final Map<String, List<String>> commonAttributePathsMap = new HashMap<>();

	static {

		final List<String> ap1 = Lists.newArrayList();
		ap1.add(AttributeServiceTestUtils.DCTERMS_TITLE);
		ap1.add(AttributeServiceTestUtils.DCTERMS_HASPART);
		ap1.add(AttributeServiceTestUtils.DCTERMS_TITLE);

		commonAttributePathsMap.put(DCTERMS_TITLE__DCTERMS_HASPART__DCTERMS_TITLE_AP, ap1);

		final List<String> ap2 = Lists.newArrayList();
		ap2.add(AttributeServiceTestUtils.DCTERMS_TITLE);
		ap2.add(AttributeServiceTestUtils.DCTERMS_HASPART);

		commonAttributePathsMap.put(DCTERMS_TITLE__DCTERMS_HASPART_AP, ap2);

		final List<String> ap3 = Lists.newArrayList();
		ap3.add(AttributeServiceTestUtils.DCTERMS_CREATED);

		commonAttributePathsMap.put(AttributeServiceTestUtils.DCTERMS_CREATED, ap3);

		final List<String> ap4 = Lists.newArrayList();
		ap4.add(AttributeServiceTestUtils.RDF_VALUE);

		commonAttributePathsMap.put(AttributeServiceTestUtils.RDF_VALUE, ap4);

		final List<String> ap5 = Lists.newArrayList();
		ap5.add(AttributeServiceTestUtils.MABXML_ID);

		commonAttributePathsMap.put(AttributeServiceTestUtils.MABXML_ID, ap5);

		final List<String> ap6 = Lists.newArrayList();
		ap6.add(AttributeServiceTestUtils.RDFS_LABEL);

		commonAttributePathsMap.put(AttributeServiceTestUtils.RDFS_LABEL, ap6);

		final List<String> ap7 = Lists.newArrayList();
		ap7.add(AttributeServiceTestUtils.DCTERMS_TITLE);

		commonAttributePathsMap.put(AttributeServiceTestUtils.DCTERMS_TITLE, ap7);

		final List<String> ap8 = Lists.newArrayList();
		ap8.add(AttributeServiceTestUtils.DCTERMS_CREATOR);
		ap8.add(AttributeServiceTestUtils.FOAF_FIRSTNAME);

		commonAttributePathsMap.put(DCTERMS_CREATOR__FOAF_FIRSTNAME_AP, ap8);

		final List<String> ap9 = Lists.newArrayList();
		ap9.add(AttributeServiceTestUtils.DCTERMS_CREATOR);
		ap9.add(AttributeServiceTestUtils.FOAF_FAMILYNAME);

		commonAttributePathsMap.put(DCTERMS_CREATOR__FOAF_FAMILYNAME_AP, ap9);

		final List<String> ap10 = Lists.newArrayList();
		ap10.add(AttributeServiceTestUtils.DCTERMS_CREATOR);
		ap10.add(AttributeServiceTestUtils.FOAF_NAME);

		commonAttributePathsMap.put(DCTERMS_CREATOR__FOAF_NAME_AP, ap10);
	}

	public AttributePathServiceTestUtils() {

		super(AttributePath.class, AttributePathService.class);

		astUtils = new AttributeServiceTestUtils();
	}

	/**
	 * {@inheritDoc} <br />
	 * Assert that both {@link AttributePath}s have either no {@link Attribute}s
	 * or {@link Attribute}s are equal. See
	 * {@link BasicJPAServiceTestUtils#compareObjects(Set, Map)} for details
	 *
	 * @param expectedObject
	 * @param actualObject
	 */
	@Override
	public void compareObjects(final AttributePath expectedObject, final AttributePath actualObject) throws JsonProcessingException, JSONException {

		super.compareObjects(expectedObject, actualObject);

		Assert.assertNotNull("the attribute path string of the actual attribute path shouldn't be null",
				actualObject.toAttributePath());
		Assert.assertEquals("the attribute path's strings are not equal", expectedObject.toAttributePath(),
				actualObject.toAttributePath());

		if (expectedObject.getAttributes() == null || expectedObject.getAttributes().isEmpty()) {
			final boolean actualHasNoAttributes = actualObject.getAttributes() == null || actualObject.getAttributes().isEmpty();
			Assert.assertTrue("the actual attribute path should not have any attributes", actualHasNoAttributes);
		} else {
			// !null && !empty
			final Set<Attribute> actualAttributes = actualObject.getAttributes();

			Assert.assertNotNull("attributes of actual attribute path '" + actualObject.getUuid() + "' shouldn't be null", actualAttributes);
			Assert.assertFalse("attributes of actual attribute path '" + actualObject.getUuid() + "' shouldn't be empty", actualAttributes.isEmpty());

			final Map<String, Attribute> actualAttributesMap = Maps.newHashMap();
			for (final Attribute actualAttribute : actualAttributes) {
				actualAttributesMap.put(actualAttribute.getUuid(), actualAttribute);
			}

			astUtils.compareObjects(expectedObject.getAttributes(), actualAttributesMap);
		}
	}

	public AttributePath createAttributePath(final List<Attribute> attributePathArg) throws Exception {
		final AttributePath attributePath = new AttributePath(attributePathArg);
		return createAndCompareObject(attributePath, attributePath);
	}

	/**
	 * Convenience method to be able to create attribute paths more dynamically
	 *
	 * @param attributes
	 * @return
	 * @throws Exception
	 */
	public AttributePath createAttributePath(Attribute... attributes) throws Exception {
		return createAttributePath(Arrays.asList(attributes));
	}

	/**
	 * Constructs a sample attribute path consisting of the following attributes:<br>
	 * "http://purl.org/dc/terms/title"<br>
	 * "http://purl.org/dc/terms/hasPart"
	 *
	 * @return
	 * @throws Exception
	 */
	@Override
	public AttributePath createAndPersistDefaultObject() throws Exception {

		return getDctermsTitleDctermHaspartDctermsTitleAP();
	}

	@Override public AttributePath createDefaultObject() throws Exception {
		return null;
	}

	public AttributePath getDctermsTitleDctermHaspartDctermsTitleAP() throws Exception {

		return createObject(DCTERMS_TITLE__DCTERMS_HASPART__DCTERMS_TITLE_AP);
	}

	public AttributePath getDctermsTitleDctermHaspartAP() throws Exception {

		return createObject(DCTERMS_TITLE__DCTERMS_HASPART_AP);
	}

	public AttributePath getDctermsCreatedAP() throws Exception {

		return createObject(AttributeServiceTestUtils.DCTERMS_CREATED);
	}

	public AttributePath getRDFValueAP() throws Exception {

		return createObject(AttributeServiceTestUtils.RDF_VALUE);
	}

	public AttributePath getMABXMLIDAP() throws Exception {

		return createObject(AttributeServiceTestUtils.MABXML_ID);
	}

	public AttributePath getRDFSLabelAP() throws Exception {

		return createObject(AttributeServiceTestUtils.RDFS_LABEL);
	}

	public AttributePath getDctermsTitleAP() throws Exception {

		return createObject(AttributeServiceTestUtils.DCTERMS_TITLE);
	}

	public AttributePath getDctermsCreatorFoafFirstnameAP() throws Exception {

		return createObject(DCTERMS_CREATOR__FOAF_FIRSTNAME_AP);
	}

	public AttributePath getDctermsCreatorFoafFamilynameAP() throws Exception {

		return createObject(DCTERMS_CREATOR__FOAF_FAMILYNAME_AP);
	}

	public AttributePath getDctermsCreatorFoafNameAP() throws Exception {

		return createObject(DCTERMS_CREATOR__FOAF_NAME_AP);
	}

	private AttributePath createObject(final List<Attribute> attributePath) {
		AttributePath object = null;
		try {
			object = jpaService.createOrGetObjectTransactional(attributePath).getObject();
			LOG.debug(object.toString());
		} catch (final DMPPersistenceException e) {
			Assert.assertTrue("something went wrong during attribute path creation.\n" + e.getMessage(), false);
		}

		Assert.assertNotNull(type + " shouldn't be null", object);
		Assert.assertNotNull(type + " id shouldn't be null", object.getUuid());
		Assert.assertNotNull(type + " path shouldn't be null", object.getAttributePathAsJSONObjectString());

		LOG.debug("created new attribute path with id = '" + object.getUuid() + "'" + " and path = '" + object.getAttributePathAsJSONObjectString()
				+ "'");
		return object;
	}

	/**
	 * {@inheritDoc}<br/>
	 * Updates the name and list of attributes of the attribute path.
	 */
	@Override
	protected AttributePath prepareObjectForUpdate(final AttributePath objectWithUpdates, final AttributePath object) {
		object.setAttributePath(objectWithUpdates.getAttributePath());
		return object;
	}

	@Override
	protected ProxyAttributePath createObject(final AttributePath object) throws DMPPersistenceException {

		return jpaService.createObjectTransactional(object);
	}

	@Override
	public void reset() {

		astUtils.reset();
	}

	/**
	 * Convenience method for creating simple attribute path of length 1 as they
	 * are frequently needed in sub-schema contexts
	 *
	 * @param attribute
	 * @return an attribute path of length 1
	 * @throws Exception
	 */
	public AttributePath createAttributePath(Attribute attribute) throws Exception {
		final List<Attribute> attributeList = new LinkedList<>();
		attributeList.add(attribute);
		return createAttributePath(attributeList);
	}

	/**
	 * note: results will be cahced
	 *
	 * @param attributeIds
	 * @return
	 * @throws Exception
	 */
	public AttributePath getAttributePath(final String... attributeIds) throws Exception {

		final StringBuilder key = new StringBuilder();

		int i = 1;

		for (final String attributeId : attributeIds) {

			key.append(attributeId);

			if (i < attributeIds.length) {

				key.append(DMPStatics.ATTRIBUTE_DELIMITER);
			}

			i++;
		}

		return createObject(key.toString());
	}

	public AttributePath getNonCachedAttributePath(final String... attributeIds) throws Exception {

		final List<String> attributeIdList = Lists.newArrayList();

		Collections.addAll(attributeIdList, attributeIds);

		return getNonCachedAttributePath(attributeIdList);

	}

	public AttributePath getNonCachedAttributePath(final List<String> attributeIds) throws Exception {

		final List<Attribute> attributes = getNonCachedAttributes(attributeIds);

		return createAttributePath(attributes);
	}

	public AttributePath getNonCachedDctermsTitleDctermsHaspartDctermsTitleAP() throws Exception {

		return getNonCachedAttributePath(commonAttributePathsMap.get(DCTERMS_TITLE__DCTERMS_HASPART__DCTERMS_TITLE_AP));
	}

	@Override
	public AttributePath createObject(JsonNode objectDescription) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override public AttributePath createObject(final String identifier) throws Exception {

		if (!cache.containsKey(identifier)) {

			if (!commonAttributePathsMap.containsKey(identifier)) {

				throw new DMPPersistenceException(
						identifier + " is no common attribute path, please define it or utilise another appropriated method for creating it");
			}

			final List<String> attributeIds = commonAttributePathsMap.get(identifier);

			final List<Attribute> attrs = getAttributes(attributeIds);

			cache.put(identifier, createAttributePath(attrs));
		}

		return cache.get(identifier);
	}

	private List<Attribute> getAttributes(final List<String> attributeIds) throws Exception {

		final List<Attribute> attrs = new ArrayList<>();

		for (final String attrStr : attributeIds) {

			attrs.add(astUtils.createObject(attrStr));
		}

		return attrs;
	}

	private List<Attribute> getNonCachedAttributes(final List<String> attributeIds) throws Exception {

		final List<Attribute> attrs = new ArrayList<>();

		for (final String attrStr : attributeIds) {

			attrs.add(astUtils.createObject(attrStr, SchemaUtils.determineRelativeURIPart(attrStr)));
		}

		return attrs;
	}
}
