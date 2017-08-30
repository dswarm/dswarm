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
import java.io.InputStream;
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

import org.dswarm.persistence.model.internal.helper.AttributePathHelper;
import org.dswarm.persistence.model.internal.helper.AttributePathHelperHelper;
import org.dswarm.persistence.service.schema.AttributePathService;
import org.dswarm.persistence.service.schema.AttributeService;
import org.dswarm.persistence.service.schema.ClaszService;
import org.dswarm.persistence.service.schema.SchemaAttributePathInstanceService;
import org.dswarm.persistence.service.schema.SchemaService;
import org.dswarm.persistence.util.GDMUtil;

/**
 * @author tgaengler
 */
public class JSONSchemaParser extends AbstractJSONSchemaParser {

	private static final Logger LOG = LoggerFactory.getLogger(JSONSchemaParser.class);

	@Inject
	public JSONSchemaParser(final Provider<SchemaService> schemaServiceProviderArg,
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

		final URL resourceURL = Resources.getResource(schemaFilePath);
		final ByteSource byteSource = Resources.asByteSource(resourceURL);

		try (final InputStream inputStream = byteSource.openStream()) {

			return Optional.ofNullable(objectMapperProvider.get().readValue(inputStream, ObjectNode.class));
		} catch (final IOException e) {

			LOG.error("couldn't read XML schema '{}'", schemaFilePath, e);

			return Optional.empty();
		}
	}

	@Override
	protected void optionalAddRDFTypeAttributePath(final Set<AttributePathHelper> attributePaths,
	                                               final String type,
	                                               final String attribute,
	                                               final AttributePathHelper finalAttributePathHelper) {

		if(type.equals(OBJECT_JSON_SCHEMA_ATTRIBUTE_TYPE)) {

			// add rdf:type attribute
			final Boolean multivalue = null;
			AttributePathHelperHelper.addAttributePath(GDMUtil.RDF_type, multivalue, attributePaths, finalAttributePathHelper);
		}
	}

	@Override
	protected boolean doAddRDFValueAttributePath(final JsonNode jsonSchemaAttributeContentNode,
	                                             final String type,
	                                             final String attribute) {

		return false;
	}

	@Override
	protected void optionalAddRDFValueAttributePath(final boolean addRDFValueAttributePath,
	                                                final Set<AttributePathHelper> attributePaths,
	                                                final AttributePathHelper attributePath) {

		// do nothing
	}

	@Override
	protected void addAttributeNode(final List<JsonNode> newAttributeNodes,
	                                final List<JsonNode> newElementNodes,
	                                final ObjectNode newJSONSchemaAttributeNode, String newAttribute) {

		newElementNodes.add(newJSONSchemaAttributeNode);
	}
}
