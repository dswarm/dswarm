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
package org.dswarm.persistence.service.schema.test.internalmodel;

import java.util.LinkedList;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.GuicedTest;
import org.dswarm.persistence.model.schema.Attribute;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.Clasz;
import org.dswarm.persistence.model.schema.NameSpacePrefixRegistry;
import org.dswarm.persistence.model.schema.SchemaAttributePathInstance;
import org.dswarm.persistence.service.schema.AttributePathService;
import org.dswarm.persistence.service.schema.AttributeService;
import org.dswarm.persistence.service.schema.ClaszService;
import org.dswarm.persistence.service.schema.SchemaAttributePathInstanceService;

public class AttributePathBuilder extends GuicedTest {

	private static final Logger				LOG				= LoggerFactory.getLogger(AttributePathBuilder.class);

	private AttributePath					pathUnderConstruction;

	private LinkedList<Attribute>			attributeList;

	private final ObjectMapper				objectMapper	= GuicedTest.injector.getInstance(ObjectMapper.class);

	private final Map<Long, Attribute>		attributes		= Maps.newLinkedHashMap();

	private final NameSpacePrefixRegistry	registry;

	private String							prefixPaths		= "";

	public AttributePathBuilder() {

		super();

		registry = new NameSpacePrefixRegistry();

	}

	public AttributePathBuilder start() {
		attributeList = Lists.newLinkedList();
		return this;
	}

	public AttributePathBuilder add(final String fullURI) {
		attributeList.add(createAttribute(fullURI, getLocalName(fullURI)));
		return this;
	}

	public AttributePath getPath() {

		pathUnderConstruction = createAttributePath(attributeList);
		return pathUnderConstruction;
	}

	public AttributePath parseAsAttributePath(final String pathInPrefixNotation) {

		// temp store prefix paths as a summary
		prefixPaths += pathInPrefixNotation + System.lineSeparator();

		start();

		final String[] attributesInPrefixNotation = pathInPrefixNotation.split("/");

		for (int i = 0; i < attributesInPrefixNotation.length; i++) {

			final String attributeInPrefixNotation = attributesInPrefixNotation[i];

			final String[] attributeParts = attributeInPrefixNotation.split(":");

			final String prefix = attributeParts[0];
			final String localName = attributeParts[1];
			final String namespace = registry.getNamespace(prefix);

			add(namespace + localName);
		}

		return getPath();

	}
	
	public SchemaAttributePathInstance parseAsAttributePathInstance(final String pathInPrefixNotation) {
		
		AttributePath attributePath = parseAsAttributePath(pathInPrefixNotation);
		
		SchemaAttributePathInstance attributePathInstance = createAttributePathInstance(attributePath);
		
		return attributePathInstance;
	}

	public String getPrefixPaths() {
		return prefixPaths;
	}
	
	private SchemaAttributePathInstance createAttributePathInstance(final AttributePath attributePathArg) {

		final SchemaAttributePathInstanceService attributePathInstanceService = GuicedTest.injector.getInstance(SchemaAttributePathInstanceService.class);

		Assert.assertNotNull("attribute path instance service shouldn't be null", attributePathInstanceService);

		SchemaAttributePathInstance persistedAttributePathInstance = null;
		
		try {
			persistedAttributePathInstance = attributePathInstanceService.createObjectTransactional(attributePathArg).getObject();
		} catch (final DMPPersistenceException e1) {
			Assert.fail("something went wrong while attribute path instance creation.\n" + e1.getMessage());
		}
		
		return persistedAttributePathInstance;
	}

	private AttributePath createAttributePath(final LinkedList<Attribute> attributePathArg) {

		final AttributePathService attributePathService = GuicedTest.injector.getInstance(AttributePathService.class);

		Assert.assertNotNull("attribute path service shouldn't be null", attributePathService);

		AttributePath updatedAttributePath = null;

		try {
			updatedAttributePath = attributePathService.createOrGetObjectTransactional(attributePathArg).getObject();
		} catch (final DMPPersistenceException e1) {
			Assert.fail("something went wrong while attribute path creation.\n" + e1.getMessage());
		}

		String json = null;

		try {

			json = objectMapper.writeValueAsString(updatedAttributePath);
			
		} catch (final JsonProcessingException e) {

			e.printStackTrace();
		}

		AttributePathBuilder.LOG.debug("attribute path json for attribute path '" + updatedAttributePath.getId() + "': " + json);

		return updatedAttributePath;
	}

	private Attribute createAttribute(final String id, final String name) {

		if (attributes.containsKey(id)) {

			return attributes.get(id);
		}

		final AttributeService attributeService = GuicedTest.injector.getInstance(AttributeService.class);

		Assert.assertNotNull("attribute service shouldn't be null", attributeService);

		// create first attribute

		Attribute attribute = null;

		try {
			attribute = attributeService.createOrGetObjectTransactional(id).getObject();
		} catch (final DMPPersistenceException e) {
			Assert.fail("something went wrong while attribute creation.\n" + e.getMessage());
		}

		Assert.assertNotNull("attribute shouldn't be null", attribute);
		Assert.assertNotNull("attribute id shouldn't be null", attribute.getId());

		attribute.setName(name);

		Attribute updatedAttribute = null;

		try {
			updatedAttribute = attributeService.updateObjectTransactional(attribute).getObject();
		} catch (final DMPPersistenceException e) {
			Assert.assertTrue("something went wrong while updating the attribute of id = '" + id + "'", false);
		}

		Assert.assertNotNull("updated attribute shouldn't be null", updatedAttribute);
		Assert.assertNotNull("updated attribute id shouldn't be null", updatedAttribute.getId());
		Assert.assertNotNull("updated attribute name shouldn't be null", updatedAttribute.getName());

		attributes.put(updatedAttribute.getId(), updatedAttribute);

		return updatedAttribute;
	}

	public Clasz createClass(final String id, final String name) {

		final ClaszService classService = GuicedTest.injector.getInstance(ClaszService.class);

		Assert.assertNotNull("class service shouldn't be null", classService);

		// create class

		Clasz clasz = null;

		try {
			clasz = classService.createOrGetObjectTransactional(id).getObject();
		} catch (final DMPPersistenceException e) {
			Assert.assertTrue("something went wrong while class creation.\n" + e.getMessage(), false);
		}

		Assert.assertNotNull("attribute shouldn't be null", clasz);
		Assert.assertNotNull("attribute id shouldn't be null", clasz.getId());

		clasz.setName(name);

		Clasz updatedClasz = null;

		try {
			updatedClasz = classService.updateObjectTransactional(clasz).getObject();
		} catch (final DMPPersistenceException e) {
			Assert.assertTrue("something went wrong while updating the class of id = '" + id + "'", false);
		}

		Assert.assertNotNull("updated class shouldn't be null", updatedClasz);
		Assert.assertNotNull("updated class id shouldn't be null", updatedClasz.getId());
		Assert.assertNotNull("updated class name shouldn't be null", updatedClasz.getName());

		return updatedClasz;
	}

	private String getLocalName(final String fullURI) {

		String localName = "";

		if (fullURI.contains("#")) {
			localName = fullURI.substring(StringUtils.lastIndexOf(fullURI, "#") + 1);
		} else {
			localName = fullURI.substring(StringUtils.lastIndexOf(fullURI, "/") + 1);
		}

		return localName;
	}
}
