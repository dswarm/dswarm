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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;
import javaslang.Tuple;
import javaslang.Tuple2;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.schema.Attribute;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.model.schema.SchemaAttributePathInstance;
import org.dswarm.persistence.model.schema.proxy.ProxyAttribute;
import org.dswarm.persistence.model.schema.proxy.ProxyAttributePath;
import org.dswarm.persistence.model.schema.proxy.ProxySchema;
import org.dswarm.persistence.model.schema.utils.SchemaUtils;
import org.dswarm.persistence.service.UUIDService;
import org.dswarm.persistence.service.schema.AttributePathService;
import org.dswarm.persistence.service.schema.AttributeService;
import org.dswarm.persistence.service.schema.ClaszService;
import org.dswarm.persistence.service.schema.SchemaAttributePathInstanceService;
import org.dswarm.persistence.service.schema.SchemaService;

/**
 * Transforms a given Solr schema file (schema.xml) to a d:swarm schema.
 *
 * @author tgaengler
 */
public class SolrSchemaParser {

	private static final Logger LOG = LoggerFactory.getLogger(SolrSchemaParser.class);

	private final Provider<SchemaService> schemaServiceProvider;

	private final Provider<ClaszService> classServiceProvider;

	private final Provider<AttributePathService> attributePathServiceProvider;

	private final Provider<AttributeService> attributeServiceProvider;

	private final Provider<SchemaAttributePathInstanceService> schemaAttributePathInstanceServiceProvider;

	private static final String SCHEMA_IDENTIFIER = "schema";
	private static final String FIELDS_IDENTIFIER = "fields";
	private static final String FIELD_IDENTIFIER = "field";
	private static final String FIELDS_XPATH_EXPRESSION =
			SchemaUtils.SLASH + SCHEMA_IDENTIFIER + SchemaUtils.SLASH + FIELDS_IDENTIFIER + SchemaUtils.SLASH + FIELD_IDENTIFIER;
	private static final String NAME_IDENTIFIER = "name";
	private static final String MULTI_VALUED_IDENTIFIER = "multiValued";
	private static final String DEFAULT_RECORD_CLASS_LOCAL_NAME = "RecordType";

	private static final DocumentBuilderFactory documentBuilderFactory;
	private static final XPathFactory xPathfactory;

	static {

		documentBuilderFactory = DocumentBuilderFactory.newInstance();
		xPathfactory = XPathFactory.newInstance();
	}

	@Inject
	public SolrSchemaParser(final Provider<SchemaService> schemaServiceProviderArg,
	                        final Provider<ClaszService> classServiceProviderArg,
	                        final Provider<AttributePathService> attributePathServiceProviderArg,
	                        final Provider<AttributeService> attributeServiceProviderArg,
	                        final Provider<SchemaAttributePathInstanceService> schemaAttributePathInstanceServiceProviderArg) {

		schemaServiceProvider = schemaServiceProviderArg;
		classServiceProvider = classServiceProviderArg;
		attributePathServiceProvider = attributePathServiceProviderArg;
		attributeServiceProvider = attributeServiceProviderArg;
		schemaAttributePathInstanceServiceProvider = schemaAttributePathInstanceServiceProviderArg;
	}

	public Optional<Schema> parse(final String solrSchemaFilePath,
	                              final String schemaUUID,
	                              final String schemaName,
	                              final String baseURI,
	                              final Optional<Map<String, String>> optionalAttributePathsSAPIUUIDs) throws DMPPersistenceException {

		final Optional<Document> optionalDocument = readXML(solrSchemaFilePath);

		if (!optionalDocument.isPresent()) {

			LOG.error("parsed Solr schema (from '{}') is not present", solrSchemaFilePath);

			return Optional.empty();
		}

		final Document document = optionalDocument.get();

		final Optional<NodeList> optionalFields = getFields(document, solrSchemaFilePath);

		if (!optionalFields.isPresent()) {

			LOG.error("couldn't find fields in the Solr schema (from '{}')", solrSchemaFilePath);

			return Optional.empty();
		}

		final Schema schema = createSchema(schemaUUID, schemaName, baseURI);
		final String schemaBaseURI;

		if (baseURI != null && !baseURI.trim().isEmpty()) {

			schemaBaseURI = baseURI;
		} else {

			schemaBaseURI = SchemaUtils.determineSchemaNamespaceURI(schema.getUuid());
		}

		final NodeList fields = optionalFields.get();
		final javaslang.collection.List<Tuple2<Attribute, Optional<Boolean>>> attributesList = determineAndCreateAttributes(fields, schemaBaseURI);

		if (attributesList.isEmpty()) {

			LOG.error("could not extract any attribute from the Solr schema at '{}'", solrSchemaFilePath);

			return Optional.empty();
		}

		// attribute paths
		final javaslang.collection.List<Tuple2<AttributePath, Optional<Boolean>>> attributePathsList = createAttributePaths(attributesList);

		if (attributePathsList.isEmpty()) {

			LOG.error("couldn't create any attribute path from the extracted attributes from the Solr schema at '{}'", solrSchemaFilePath);

			return Optional.empty();
		}

		// schema attribute paths
		for (final Tuple2<AttributePath, Optional<Boolean>> attributePathTuple : attributePathsList) {

			final SchemaAttributePathInstance schemaAttributePathInstance = createOrGetSchemaAttributePathInstance(attributePathTuple, optionalAttributePathsSAPIUUIDs);

			schema.addAttributePath(schemaAttributePathInstance);
		}

		// record class
		final String recordClassURI = schemaBaseURI + DEFAULT_RECORD_CLASS_LOCAL_NAME;

		SchemaUtils.addRecordClass(schema, recordClassURI, classServiceProvider);

		final Optional<ProxySchema> optionalProxySchema = Optional.ofNullable(schemaServiceProvider.get().createObjectTransactional(schema));

		if (!optionalProxySchema.isPresent()) {

			return Optional.empty();
		}

		return Optional.ofNullable(optionalProxySchema.get().getObject());
	}

	public Optional<Schema> parse(final String solrSchemaFilePath,
	                              final String schemaUUID,
	                              final String schemaName,
	                              final String baseURI) throws DMPPersistenceException {

		return parse(solrSchemaFilePath, schemaUUID, schemaName, baseURI, Optional.empty());
	}

	private Optional<Document> readXML(final String solrSchemaFilePath) {

		final DocumentBuilder documentBuilder;

		try {

			documentBuilder = documentBuilderFactory.newDocumentBuilder();
		} catch (final ParserConfigurationException e) {

			LOG.error("couldn't create document builder for parsing the Solr schema file", e);

			return Optional.empty();
		}

		final InputStream inputStream = ClassLoader.getSystemResourceAsStream(solrSchemaFilePath);

		final Document document;

		try {
			document = documentBuilder.parse(inputStream);

			return Optional.ofNullable(document);
		} catch (final SAXException e) {

			LOG.error("couldn't parse the Solr schema file at '{}'", solrSchemaFilePath, e);

			return Optional.empty();
		} catch (final IOException e) {

			LOG.error("couldn't read the Solr schema fila at '{}'", solrSchemaFilePath, e);

			return Optional.empty();
		}
	}

	private Optional<NodeList> getFields(final Document document, final String solrSchemaFilePath) {

		final XPath xpath = xPathfactory.newXPath();

		final XPathExpression expr;

		try {

			expr = xpath.compile(FIELDS_XPATH_EXPRESSION);
		} catch (final XPathExpressionException e) {

			LOG.error("could not compile xpath", e);

			return Optional.empty();
		}

		final NodeList result;

		try {

			result = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
		} catch (final XPathExpressionException e) {

			LOG.error("could not execute xpath query against document (from '{}')", solrSchemaFilePath, e);

			return Optional.empty();
		}

		return Optional.ofNullable(result);
	}

	private javaslang.collection.List<Tuple2<Attribute, Optional<Boolean>>> determineAndCreateAttributes(final NodeList fields, final String schemaBaseURI) throws DMPPersistenceException {

		final List<Tuple2<Attribute, Optional<Boolean>>> attributes = new ArrayList<>();

		// determine and mint attributes
		for (int i = 0; i < fields.getLength(); i++) {

			final Node field = fields.item(i);

			if (!field.hasAttributes()) {

				if (LOG.isDebugEnabled()) {

					LOG.debug("field ('{}') has no attributes, cannot parse an attribute from it", ToStringBuilder.reflectionToString(field));
				}

				continue;
			}

			final NamedNodeMap fieldAttributes = field.getAttributes();

			final Optional<Node> optionalNameNode = Optional.ofNullable(fieldAttributes.getNamedItem(NAME_IDENTIFIER));

			if (!optionalNameNode.isPresent()) {

				if (LOG.isDebugEnabled()) {

					LOG.debug("field ('{}') has no name XML attribute, cannot parse an attribute from it", ToStringBuilder.reflectionToString(field));
				}

				continue;
			}

			final Node nameNode = optionalNameNode.get();

			final Optional<String> optionalName = Optional.ofNullable(nameNode.getNodeValue());

			if (!optionalName.isPresent()) {

				if (LOG.isDebugEnabled()) {

					LOG.debug("field ('{}') has no name, cannot parse an attribute from it", ToStringBuilder.reflectionToString(field));
				}

				continue;
			}

			final Optional<Node> optionalMultiValuedNode = Optional.ofNullable(fieldAttributes.getNamedItem(MULTI_VALUED_IDENTIFIER));

			final Optional<Boolean> optionalIsMultiValued;

			if (optionalMultiValuedNode.isPresent()) {

				final Node multiValuedNode = optionalMultiValuedNode.get();

				optionalIsMultiValued = Optional.ofNullable(Boolean.valueOf(multiValuedNode.getNodeValue()));
			} else {

				optionalIsMultiValued = Optional.empty();
			}

			final Optional<Attribute> optionalAttribute = createAttribute(schemaBaseURI, optionalName.get());

			if (optionalAttribute.isPresent()) {

				attributes.add(Tuple.of(optionalAttribute.get(), optionalIsMultiValued));
			}
		}

		return javaslang.collection.List.ofAll(attributes);
	}

	private javaslang.collection.List<Tuple2<AttributePath, Optional<Boolean>>> createAttributePaths(final javaslang.collection.List<Tuple2<Attribute, Optional<Boolean>>> attributesList) throws DMPPersistenceException {

		final List<Tuple2<AttributePath, Optional<Boolean>>> attributePaths = new ArrayList<>();

		for (final Tuple2<Attribute, Optional<Boolean>> attributeTuple : attributesList) {

			final Optional<AttributePath> optionalAttributePath = createAttributePath(attributeTuple._1);

			if (optionalAttributePath.isPresent()) {

				attributePaths.add(Tuple.of(optionalAttributePath.get(), attributeTuple._2));
			}
		}

		return javaslang.collection.List.ofAll(attributePaths);
	}

	private Schema createSchema(final String uuid, final String name, final String baseURI) {

		final String finalUUID;

		if (uuid != null && !uuid.trim().isEmpty()) {

			finalUUID = uuid;
		} else {

			finalUUID = UUIDService.getUUID(Schema.class.getSimpleName());
		}

		final Schema schema = new Schema(finalUUID);

		if (name != null && !name.trim().isEmpty()) {

			schema.setName(name);
		}

		if (baseURI != null && !baseURI.trim().isEmpty()) {

			schema.setBaseURI(baseURI);
		}

		return schema;
	}

	private Optional<Attribute> createAttribute(final String schemaBaseURI, final String attributeName) throws DMPPersistenceException {

		final String uuid = UUIDService.getUUID(Attribute.class.getSimpleName());
		final String uri = SchemaUtils.mintAttributeURI(attributeName, schemaBaseURI);

		final Attribute attribute = new Attribute(uuid, uri, attributeName);

		final Optional<ProxyAttribute> optionalProxyAttribute = Optional
				.ofNullable(attributeServiceProvider.get().createObjectTransactional(attribute));

		if (!optionalProxyAttribute.isPresent()) {

			return Optional.empty();
		}

		return Optional.ofNullable(optionalProxyAttribute.get().getObject());
	}

	private Optional<AttributePath> createAttributePath(final Attribute attribute) throws DMPPersistenceException {

		final String uuid = UUIDService.getUUID(AttributePath.class.getSimpleName());

		final AttributePath attributePath = new AttributePath(uuid);
		attributePath.addAttribute(attribute);

		final Optional<ProxyAttributePath> optionalProxyAttributePath = Optional
				.ofNullable(attributePathServiceProvider.get().createObject(attributePath));

		if (!optionalProxyAttributePath.isPresent()) {

			return Optional.empty();
		}

		return Optional.ofNullable(optionalProxyAttributePath.get().getObject());
	}

	private SchemaAttributePathInstance createOrGetSchemaAttributePathInstance(final Tuple2<AttributePath, Optional<Boolean>> attributePathTuple,
	                                                                           final Optional<Map<String, String>> optionalAttributePathsSAPIUUIDs) throws DMPPersistenceException {

		if (!optionalAttributePathsSAPIUUIDs.isPresent()) {

			return createSchemaAttributePathInstance(attributePathTuple);
		}

		final Map<String, String> attributePathsSAPIUUIDs = optionalAttributePathsSAPIUUIDs.get();

		final String attributePathString = attributePathTuple._1.toAttributePath();

		final Optional<String> optionalSAPIUUID = Optional.ofNullable(attributePathsSAPIUUIDs.getOrDefault(attributePathString, null));

		if (!optionalSAPIUUID.isPresent()) {

			return createSchemaAttributePathInstance(attributePathTuple);
		}

		final String sapiUUID = optionalSAPIUUID.get();

		final Optional<SchemaAttributePathInstance> optionalSAPI = Optional.ofNullable(schemaAttributePathInstanceServiceProvider.get().getObject(sapiUUID));

		if (!optionalSAPI.isPresent()) {

			return createSchemaAttributePathInstance(attributePathTuple);
		}

		// utilise existing SAPI
		final SchemaAttributePathInstance schemaAttributePathInstance = optionalSAPI.get();

		final Optional<Boolean> optionalIsMultiValued = attributePathTuple._2;
		final Boolean finalIsMultiValued;

		if(optionalIsMultiValued.isPresent()) {

			finalIsMultiValued = optionalIsMultiValued.get();
		} else {

			finalIsMultiValued = null;
		}

		schemaAttributePathInstance.setMultivalue(finalIsMultiValued);

		return schemaAttributePathInstance;
	}

	private SchemaAttributePathInstance createSchemaAttributePathInstance(final Tuple2<AttributePath, Optional<Boolean>> attributePathTuple) throws DMPPersistenceException {

		final String uuid = UUIDService.getUUID(SchemaAttributePathInstance.class.getSimpleName());

		final SchemaAttributePathInstance schemaAttributePathInstance = new SchemaAttributePathInstance(uuid);
		schemaAttributePathInstance.setAttributePath(attributePathTuple._1);

		final Optional<Boolean> optionalIsMultiValued = attributePathTuple._2;

		if (optionalIsMultiValued.isPresent()) {

			final Boolean isMultiValued = optionalIsMultiValued.get();

			schemaAttributePathInstance.setMultivalue(isMultiValued);
		}

		return schemaAttributePathInstance;
	}
}
