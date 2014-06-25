package org.dswarm.persistence.service.schema.test.internalmodel;

import java.util.LinkedList;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.GuicedTest;
import org.dswarm.persistence.model.schema.Attribute;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.Clasz;
import org.dswarm.persistence.model.schema.NameSpacePrefixRegistry;
import org.dswarm.persistence.service.schema.AttributePathService;
import org.dswarm.persistence.service.schema.AttributeService;
import org.dswarm.persistence.service.schema.ClaszService;

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

	public AttributePath parsePrefixPath(final String pathInPrefixNotation) {

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

	public String getPrefixPaths() {
		return prefixPaths;
	}

	private AttributePath createAttributePath(final LinkedList<Attribute> attributePathArg) {

		final AttributePathService attributePathService = GuicedTest.injector.getInstance(AttributePathService.class);

		Assert.assertNotNull("attribute path service shouldn't be null", attributePathService);

		final AttributePath attributePath = new AttributePath(attributePathArg);

		AttributePath updatedAttributePath = null;

		try {

			updatedAttributePath = attributePathService.createOrGetObjectTransactional(attributePathArg).getObject();
		} catch (final DMPPersistenceException e1) {

			Assert.assertTrue("something went wrong while attribute path creation.\n" + e1.getMessage(), false);
		}

		Assert.assertNotNull("updated attribute path shouldn't be null", updatedAttributePath);
		Assert.assertNotNull("updated attribute path id shouldn't be null", updatedAttributePath.getId());
		Assert.assertNotNull("the attribute path's attribute of the updated attribute path shouldn't be null", updatedAttributePath.getAttributes());
		Assert.assertEquals("the attribute path's attributes size are not equal", attributePath.getAttributes(), updatedAttributePath.getAttributes());
		Assert.assertEquals("the first attributes of the attribute path are not equal", attributePath.getAttributePath().get(0), updatedAttributePath
				.getAttributePath().get(0));
		Assert.assertNotNull("the attribute path string of the updated attribute path shouldn't be null", updatedAttributePath.toAttributePath());
		Assert.assertEquals("the attribute path's strings are not equal", attributePath.toAttributePath(), updatedAttributePath.toAttributePath());

		String json = null;

		try {

			json = objectMapper.writeValueAsString(updatedAttributePath);
		} catch (final JsonProcessingException e) {

			e.printStackTrace();
		}

		AttributePathBuilder.LOG.debug("attribute path json for attribute path '" + attributePath.getId() + "': " + json);

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

			Assert.assertTrue("something went wrong while attribute creation.\n" + e.getMessage(), false);
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
