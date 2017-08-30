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
package org.dswarm.converter.schema;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.io.ByteSource;
import com.google.common.io.Resources;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import org.dswarm.persistence.model.internal.helper.AttributePathHelper;
import org.dswarm.persistence.model.internal.helper.AttributePathHelperHelper;
import org.dswarm.persistence.service.schema.AttributePathService;
import org.dswarm.persistence.service.schema.AttributeService;
import org.dswarm.persistence.service.schema.ClaszService;
import org.dswarm.persistence.service.schema.SchemaAttributePathInstanceService;
import org.dswarm.persistence.service.schema.SchemaService;
import org.dswarm.persistence.util.GDMUtil;
import org.dswarm.xsd2jsonschema.JsonSchemaParser;
import org.dswarm.xsd2jsonschema.model.JSRoot;

/**
 * @author tgaengler
 */
public class XMLSchemaParser extends AbstractJSONSchemaParser {

	private static final Logger LOG = LoggerFactory.getLogger(XMLSchemaParser.class);

	@Inject
	public XMLSchemaParser(final Provider<SchemaService> schemaServiceProviderArg,
	                       final Provider<ClaszService> classServiceProviderArg,
	                       final Provider<AttributePathService> attributePathServiceProviderArg,
	                       final Provider<SchemaAttributePathInstanceService> schemaAttributePathInstanceServiceProviderArg,
	                       final Provider<AttributeService> attributeServiceProviderArg,
	                       final Provider<ObjectMapper> objectMapperProviderArg) {

		super(schemaServiceProviderArg,
				classServiceProviderArg,
				attributePathServiceProviderArg,
				schemaAttributePathInstanceServiceProviderArg,
				attributeServiceProviderArg,
				objectMapperProviderArg);
	}

	@Override
	protected Optional<ObjectNode> getJSONSchema(final String schemaFilePath) {

		final JsonSchemaParser schemaParser = new JsonSchemaParser();

		final URL resourceURL = Resources.getResource(schemaFilePath);
		final ByteSource byteSource = Resources.asByteSource(resourceURL);

		try {

			schemaParser.parse(byteSource.openStream());
		} catch (final SAXException e) {

			LOG.error("couldn't parse XML schema '{}'", schemaFilePath, e);

			return Optional.empty();
		} catch (final IOException e) {

			LOG.error("couldn't read XML schema '{}'", schemaFilePath, e);

			return Optional.empty();
		}
		final JSRoot root;

		try {

			root = schemaParser.apply(XMLSchemaParser.ROOT_NODE_IDENTIFIER);
		} catch (final SAXException e) {

			LOG.error("couldn't convert XSD to JSON schema for '{}'", schemaFilePath, e);

			return Optional.empty();
		}

		final ObjectNode json;

		try {

			json = root.toJson(objectMapperProvider.get());
		} catch (final IOException e) {

			LOG.error("couldn't serialize JSON schema for '{}'", schemaFilePath, e);

			return Optional.empty();
		}

		return Optional.ofNullable(json);
	}

	protected void optionalAddRDFTypeAttributePath(final Set<AttributePathHelper> attributePaths,
	                                               final String type,
	                                               final String attribute,
	                                               final AttributePathHelper finalAttributePathHelper) {

		if ((type.equals(AbstractJSONSchemaParser.STRING_JSON_SCHEMA_ATTRIBUTE_TYPE) || type.equals(OBJECT_JSON_SCHEMA_ATTRIBUTE_TYPE))
				&& !isXMLAttribute(attribute)) {

			// add rdf:type attribute
			final Boolean multivalue = null;
			AttributePathHelperHelper.addAttributePath(GDMUtil.RDF_type, multivalue, attributePaths, finalAttributePathHelper);
		}
	}

	protected void optionalAddRDFValueAttributePath(final boolean addRDFValueAttributePath,
	                                                final Set<AttributePathHelper> attributePaths,
	                                                final AttributePathHelper attributePath) {

		if (addRDFValueAttributePath) {

			// add rdf:value attribute
			final Boolean multivalue = null;
			AttributePathHelperHelper.addAttributePath(GDMUtil.RDF_value, multivalue, attributePaths, attributePath);
		}
	}

	protected String determineAttributeName(final String attribute) {

		final boolean isXMLAttribute = isXMLAttribute(attribute);

		final String finalAttribute;

		if (isXMLAttribute) {

			finalAttribute = attribute.substring(1, attribute.length());
		} else {

			finalAttribute = attribute;
		}

		return finalAttribute;
	}

	protected boolean doAddRDFValueAttributePath(final JsonNode jsonSchemaAttributeContentNode,
	                                             final String type,
	                                             final String attribute) {

		final JsonNode mixedNode = jsonSchemaAttributeContentNode.get(AbstractJSONSchemaParser.JSON_SCHEMA_MIXED_IDENTIFIER);

		final boolean isMixed = mixedNode != null && mixedNode.asBoolean();

		final boolean addRDFValueAttributePath =
				(type.equals(AbstractJSONSchemaParser.STRING_JSON_SCHEMA_ATTRIBUTE_TYPE) || (type.equals(OBJECT_JSON_SCHEMA_ATTRIBUTE_TYPE) && isMixed))
						&& !isXMLAttribute(attribute);

		return  addRDFValueAttributePath;
	}

	protected void addAttributeNode(final List<JsonNode> newAttributeNodes,
	                                final List<JsonNode> newElementNodes,
	                                final ObjectNode newJSONSchemaAttributeNode,
	                                final String newAttribute) {

		if (isXMLAttribute(newAttribute)) {

			newAttributeNodes.add(newJSONSchemaAttributeNode);
		} else {

			newElementNodes.add(newJSONSchemaAttributeNode);
		}
	}

	private boolean isXMLAttribute(final String attribute) {

		return attribute.startsWith("@");
	}
}
