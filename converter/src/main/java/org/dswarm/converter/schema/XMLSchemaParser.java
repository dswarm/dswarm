/**
 * Copyright (C) 2013 – 2016 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.io.ByteSource;
import com.google.common.io.Resources;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import org.dswarm.persistence.service.schema.AttributePathService;
import org.dswarm.persistence.service.schema.AttributeService;
import org.dswarm.persistence.service.schema.ClaszService;
import org.dswarm.persistence.service.schema.SchemaAttributePathInstanceService;
import org.dswarm.persistence.service.schema.SchemaService;
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
	protected Optional<ObjectNode> getJSONSchema(final String xmlSchemaFilePath) {

		final JsonSchemaParser schemaParser = new JsonSchemaParser();

		final URL resourceURL = Resources.getResource(xmlSchemaFilePath);
		final ByteSource byteSource = Resources.asByteSource(resourceURL);

		try {

			schemaParser.parse(byteSource.openStream());
		} catch (final SAXException e) {

			LOG.error("couldn't parse XML schema '{}'", xmlSchemaFilePath, e);

			return Optional.empty();
		} catch (final IOException e) {

			LOG.error("couldn't read XML schema '{}'", xmlSchemaFilePath, e);

			return Optional.empty();
		}
		final JSRoot root;

		try {

			root = schemaParser.apply(XMLSchemaParser.ROOT_NODE_IDENTIFIER);
		} catch (final SAXException e) {

			LOG.error("couldn't convert XSD to JSON schema for '{}'", xmlSchemaFilePath, e);

			return Optional.empty();
		}

		final ObjectNode json;

		try {

			json = root.toJson(objectMapperProvider.get());
		} catch (final IOException e) {

			LOG.error("couldn't serialize JSON schema for '{}'", xmlSchemaFilePath, e);

			return Optional.empty();
		}

		return Optional.ofNullable(json);
	}
}
