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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
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

public class AttributePathServiceTestUtils extends BasicJPAServiceTestUtils<AttributePathService, ProxyAttributePath, AttributePath, Long> {

	private static final Logger LOG = LoggerFactory.getLogger(AttributePathServiceTestUtils.class);

	public static final String DCTERMS_TITLE__DCTERMS_HASPART__DCTERMS_TITLE_AP =
			AttributeServiceTestUtils.DCTERMS_TITLE + DMPStatics.ATTRIBUTE_DELIMITER
					+ AttributeServiceTestUtils.DCTERMS_HASPART + DMPStatics.ATTRIBUTE_DELIMITER + AttributeServiceTestUtils.DCTERMS_TITLE;

	public static final String DCTERMS_TITLE__DCTERMS_HASPART_AP =
			AttributeServiceTestUtils.DCTERMS_TITLE + DMPStatics.ATTRIBUTE_DELIMITER
					+ AttributeServiceTestUtils.DCTERMS_HASPART;

	private final AttributeServiceTestUtils astUtils;

	private static final Set<List<String>> excludeAttributePaths = Sets.newHashSet();

	static {

		final List<String> attributePath32 = Lists.newLinkedList();
		attributePath32.add("http://purl.org/dc/terms/creator");
		attributePath32.add("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		AttributePathServiceTestUtils.excludeAttributePaths.add(attributePath32);

		final List<String> attributePath33 = Lists.newLinkedList();
		attributePath33.add("http://purl.org/dc/terms/creator");
		attributePath33.add("http://xmlns.com/foaf/0.1/familyName");
		AttributePathServiceTestUtils.excludeAttributePaths.add(attributePath33);

		final List<String> attributePath34 = Lists.newLinkedList();
		attributePath34.add("http://purl.org/dc/terms/creator");
		attributePath34.add("http://xmlns.com/foaf/0.1/givenName");
		AttributePathServiceTestUtils.excludeAttributePaths.add(attributePath34);

		final List<String> attributePath35 = Lists.newLinkedList();
		attributePath35.add("http://purl.org/dc/terms/contributor");
		attributePath35.add("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		AttributePathServiceTestUtils.excludeAttributePaths.add(attributePath35);

		final List<String> attributePath36 = Lists.newLinkedList();
		attributePath36.add("http://purl.org/dc/terms/contributor");
		attributePath36.add("http://xmlns.com/foaf/0.1/familyName");
		AttributePathServiceTestUtils.excludeAttributePaths.add(attributePath36);

		final List<String> attributePath37 = Lists.newLinkedList();
		attributePath37.add("http://purl.org/dc/terms/contributor");
		attributePath37.add("http://xmlns.com/foaf/0.1/givenName");
		AttributePathServiceTestUtils.excludeAttributePaths.add(attributePath37);

		final List<String> attributePath45 = Lists.newLinkedList();
		attributePath45.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#feld");
		attributePath45.add("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		AttributePathServiceTestUtils.excludeAttributePaths.add(attributePath45);

		final List<String> attributePath46 = Lists.newLinkedList();
		attributePath46.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#feld");
		attributePath46.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#id");
		AttributePathServiceTestUtils.excludeAttributePaths.add(attributePath46);

		final List<String> attributePath47 = Lists.newLinkedList();
		attributePath47.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#feld");
		attributePath47.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#nr");
		AttributePathServiceTestUtils.excludeAttributePaths.add(attributePath47);

		final List<String> attributePath48 = Lists.newLinkedList();
		attributePath48.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#feld");
		attributePath48.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#ind");
		AttributePathServiceTestUtils.excludeAttributePaths.add(attributePath48);

		final List<String> attributePath49 = Lists.newLinkedList();
		attributePath49.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#feld");
		attributePath49.add("http://www.w3.org/1999/02/22-rdf-syntax-ns#value");
		AttributePathServiceTestUtils.excludeAttributePaths.add(attributePath49);

		final List<String> attributePath50 = Lists.newLinkedList();
		attributePath50.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#feld");
		attributePath50.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#tf");
		AttributePathServiceTestUtils.excludeAttributePaths.add(attributePath50);

		final List<String> attributePath51 = Lists.newLinkedList();
		attributePath51.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#feld");
		attributePath51.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#tf");
		attributePath51.add("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		AttributePathServiceTestUtils.excludeAttributePaths.add(attributePath51);

		final List<String> attributePath52 = Lists.newLinkedList();
		attributePath52.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#feld");
		attributePath52.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#stw");
		AttributePathServiceTestUtils.excludeAttributePaths.add(attributePath52);

		final List<String> attributePath53 = Lists.newLinkedList();
		attributePath53.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#feld");
		attributePath53.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#stw");
		attributePath53.add("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		AttributePathServiceTestUtils.excludeAttributePaths.add(attributePath53);

		final List<String> attributePath54 = Lists.newLinkedList();
		attributePath54.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#feld");
		attributePath54.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#stw");
		attributePath54.add("http://www.w3.org/1999/02/22-rdf-syntax-ns#value");
		AttributePathServiceTestUtils.excludeAttributePaths.add(attributePath54);

		final List<String> attributePath55 = Lists.newLinkedList();
		attributePath55.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#feld");
		attributePath55.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#ns");
		AttributePathServiceTestUtils.excludeAttributePaths.add(attributePath55);

		final List<String> attributePath56 = Lists.newLinkedList();
		attributePath56.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#feld");
		attributePath56.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#ns");
		attributePath56.add("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		AttributePathServiceTestUtils.excludeAttributePaths.add(attributePath56);

		final List<String> attributePath57 = Lists.newLinkedList();
		attributePath57.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#feld");
		attributePath57.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#ns");
		attributePath57.add("http://www.w3.org/1999/02/22-rdf-syntax-ns#value");
		AttributePathServiceTestUtils.excludeAttributePaths.add(attributePath57);

		final List<String> attributePath58 = Lists.newLinkedList();
		attributePath58.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#feld");
		attributePath58.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#uf");
		AttributePathServiceTestUtils.excludeAttributePaths.add(attributePath58);

		final List<String> attributePath59 = Lists.newLinkedList();
		attributePath59.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#feld");
		attributePath59.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#uf");
		attributePath59.add("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		AttributePathServiceTestUtils.excludeAttributePaths.add(attributePath59);

		final List<String> attributePath60 = Lists.newLinkedList();
		attributePath60.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#feld");
		attributePath60.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#uf");
		attributePath60.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#id");
		AttributePathServiceTestUtils.excludeAttributePaths.add(attributePath60);

		final List<String> attributePath61 = Lists.newLinkedList();
		attributePath61.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#feld");
		attributePath61.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#uf");
		attributePath61.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#code");
		AttributePathServiceTestUtils.excludeAttributePaths.add(attributePath61);

		final List<String> attributePath62 = Lists.newLinkedList();
		attributePath62.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#feld");
		attributePath62.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#uf");
		attributePath62.add("http://www.w3.org/1999/02/22-rdf-syntax-ns#value");
		AttributePathServiceTestUtils.excludeAttributePaths.add(attributePath62);

		final List<String> attributePath63 = Lists.newLinkedList();
		attributePath63.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#feld");
		attributePath63.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#uf");
		attributePath63.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#tf");
		AttributePathServiceTestUtils.excludeAttributePaths.add(attributePath63);

		final List<String> attributePath64 = Lists.newLinkedList();
		attributePath64.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#feld");
		attributePath64.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#uf");
		attributePath64.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#tf");
		attributePath64.add("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		AttributePathServiceTestUtils.excludeAttributePaths.add(attributePath64);

		final List<String> attributePath65 = Lists.newLinkedList();
		attributePath65.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#feld");
		attributePath65.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#uf");
		attributePath65.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#stw");
		AttributePathServiceTestUtils.excludeAttributePaths.add(attributePath65);

		final List<String> attributePath66 = Lists.newLinkedList();
		attributePath66.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#feld");
		attributePath66.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#uf");
		attributePath66.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#stw");
		attributePath66.add("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		AttributePathServiceTestUtils.excludeAttributePaths.add(attributePath66);

		final List<String> attributePath67 = Lists.newLinkedList();
		attributePath67.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#feld");
		attributePath67.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#uf");
		attributePath67.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#stw");
		attributePath67.add("http://www.w3.org/1999/02/22-rdf-syntax-ns#value");
		AttributePathServiceTestUtils.excludeAttributePaths.add(attributePath67);

		final List<String> attributePath68 = Lists.newLinkedList();
		attributePath68.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#feld");
		attributePath68.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#uf");
		attributePath68.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#ns");
		AttributePathServiceTestUtils.excludeAttributePaths.add(attributePath68);

		final List<String> attributePath69 = Lists.newLinkedList();
		attributePath69.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#feld");
		attributePath69.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#uf");
		attributePath69.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#ns");
		attributePath69.add("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		AttributePathServiceTestUtils.excludeAttributePaths.add(attributePath69);

		final List<String> attributePath70 = Lists.newLinkedList();
		attributePath70.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#feld");
		attributePath70.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#uf");
		attributePath70.add("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#ns");
		attributePath70.add("http://www.w3.org/1999/02/22-rdf-syntax-ns#value");
		AttributePathServiceTestUtils.excludeAttributePaths.add(attributePath70);
	}

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
	public void compareObjects(final AttributePath expectedObject, final AttributePath actualObject) {

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

			Assert.assertNotNull("attributes of actual attribute path '" + actualObject.getId() + "' shouldn't be null", actualAttributes);
			Assert.assertFalse("attributes of actual attribute path '" + actualObject.getId() + "' shouldn't be empty", actualAttributes.isEmpty());

			final Map<Long, Attribute> actualAttributesMap = Maps.newHashMap();
			for (final Attribute actualAttribute : actualAttributes) {
				actualAttributesMap.put(actualAttribute.getId(), actualAttribute);
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
	public AttributePath createDefaultObject() throws Exception {

		return getDctermsTitleDctermHaspartDctermsTitleAP();
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

	/**
	 * using JPA
	 */
	private AttributePath createObject(final List<Attribute> attributePath) {
		AttributePath object = null;
		try {
			object = jpaService.createOrGetObjectTransactional(attributePath).getObject();
			LOG.debug(object.toString());
		} catch (final DMPPersistenceException e) {
			Assert.assertTrue("something went wrong during attribute path creation.\n" + e.getMessage(), false);
		}

		Assert.assertNotNull(type + " shouldn't be null", object);
		Assert.assertNotNull(type + " id shouldn't be null", object.getId());
		Assert.assertNotNull(type + " path shouldn't be null", object.getAttributePathAsJSONObjectString());

		LOG.debug("created new attribute path with id = '" + object.getId() + "'" + " and path = '" + object.getAttributePathAsJSONObjectString()
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
	public void deleteObject(final AttributePath object) {

		if (object == null) {

			return;
		}

		final Set<Attribute> attributes = object.getAttributes();

		if (attributes != null && !attributes.isEmpty() && attributes.size() == 1) {

			final Attribute attribute = attributes.iterator().next();

			if (attribute != null && attribute.getUri() != null && AttributeServiceTestUtils.excludeAttributes.contains(attribute.getUri())) {

				// don't delete attribute paths of attributes that should be excluded
				// from removal

				return;
			}
		}

		final List<Attribute> orderedAttributes = object.getAttributePath();

		if (orderedAttributes != null && !orderedAttributes.isEmpty()) {

			for (final List<String> excludeAttributePath : AttributePathServiceTestUtils.excludeAttributePaths) {

				if (excludeAttributePath.size() != orderedAttributes.size()) {

					// only compare attribute paths of the same size

					continue;
				}

				final Iterator<String> excludeIter = excludeAttributePath.iterator();

				boolean interrupted = false;

				while (excludeIter.hasNext()) {

					final Iterator<Attribute> iter = orderedAttributes.iterator();

					while (iter.hasNext()) {

						final Attribute orderedAttribute = iter.next();

						if (orderedAttribute.getUri() == null) {

							break;
						}

						final String excludeAttribute = excludeIter.next();

						if (orderedAttribute.getUri() == null) {

							interrupted = true;

							break;
						}

						if (!excludeAttribute.equals(orderedAttribute.getUri())) {

							interrupted = true;

							break;
						}

						if (!excludeIter.hasNext() && iter.hasNext()) {

							interrupted = true;

							break;
						}
					}

					if (interrupted) {

						break;
					}
				}

				if (!interrupted) {

					// found match

					return;
				}
			}
		}

		super.deleteObject(object);
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
