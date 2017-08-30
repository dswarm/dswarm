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

import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.google.common.base.Charsets;
import org.codehaus.stax2.XMLOutputFactory2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.common.model.util.AttributePathUtil;
import org.dswarm.common.types.Tuple;
import org.dswarm.common.web.URI;
import org.dswarm.persistence.model.schema.Attribute;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.model.schema.SchemaAttributePathInstance;

/**
 * @author tgaengler
 */
public final class SolrXMLDataSourceConfigGenerator {

	private static final Logger LOG = LoggerFactory.getLogger(SolrXMLDataSourceConfigGenerator.class);

	/**
	 * TODO: shall we produce XML 1.0 or XML 1.1?
	 */
	private static final String XML_VERSION = "1.0";
	private static final XMLOutputFactory2 xmlOutputFactory;

	static {

		System.setProperty("javax.xml.stream.XMLOutputFactory", "com.fasterxml.aalto.stax.OutputFactoryImpl");

		xmlOutputFactory = (XMLOutputFactory2) XMLOutputFactory.newFactory();
		xmlOutputFactory.configureForSpeed();
	}

	private static final String DATA_CONFIG_IDENTIFIER  = "dataConfig";
	private static final String DATA_SOURCE_IDENTIFIER  = "dataSource";
	private static final String TYPE_IDENTIFIER         = "type";
	private static final String HTTP_DATA_SOURCE        = "HttpDataSource";
	private static final String ENCODING_IDENTIFIER     = "encoding";
	private static final String DOCUMENT_IDENTIFIER     = "document";
	private static final String ENTITIY_IDENTIFIER      = "entity";
	private static final String NAME_IDENTIFIER         = "name";
	private static final String PROCESSOR_IDENTIFIER    = "processor";
	private static final String XPATH_ENTITIY_PROCESSOR = "XPathEntityProcessor";
	private static final String FOR_EACH_IDENTIFIER     = "forEach";
	private static final String FIELD_IDENTIFIER        = "field";
	private static final String COLUMN_IDENTIFIER       = "column";
	private static final String XPATH_IDENTIFIER        = "xpath";
	private static final String UNDERSCORE              = "_";

	public static void generateSolrXMLDataSourceConfig(final Schema schema, final Optional<String> optionalRecordTag,
			final Optional<String> optionalRootAttributePath, final OutputStream outputStream) throws XMLStreamException {

		if (schema == null) {

			LOG.error("schema shouldn't be null - couldn't generate Solr XML data source config");

			return;
		}

		final Collection<SchemaAttributePathInstance> sapis = schema.getAttributePaths();

		if (sapis == null) {

			LOG.error("schema attribute path instances of schema '{}' shouldn't be null - couldn't generate Solr XML data source config",
					schema.getUuid());

			return;
		}

		if (sapis.isEmpty()) {

			LOG.error("schema attribute path instances of schema '{}' shouldn't be empty - couldn't generate Solr XML data source config",
					schema.getUuid());

			return;
		}

		if (!optionalRecordTag.isPresent() && schema.getRecordClass() == null) {

			LOG.error(
					"record class of schema '{}' shouldn't be null, if no separate record tag is provided - couldn't generate Solr XML data source config",
					schema.getUuid());

			return;
		}

		final XMLStreamWriter writer = xmlOutputFactory.createXMLStreamWriter(outputStream);

		final StringBuilder loopXPathSB = new StringBuilder();

		if (optionalRootAttributePath.isPresent()) {

			final String rootAttributePathXPath = determineXPathFromAttributePathString(optionalRootAttributePath.get());

			loopXPathSB.append(rootAttributePathXPath);
		}

		final String recordTagXPath;

		if (optionalRecordTag.isPresent()) {

			recordTagXPath = determineXPathFromAttributePathString(optionalRecordTag.get());
		} else {

			recordTagXPath = determineXPathFromAttributePathString(schema.getRecordClass().getUri());
		}

		loopXPathSB.append(recordTagXPath);

		final String loopXPath = loopXPathSB.toString();

		prepareSolrXMLDataSourceConfig(writer, schema.getUuid(), loopXPath);
		generateFields(schema.getAttributePaths(),loopXPath, writer);
		finalizedSolrXMLDataSourceConfig(writer);
	}

	private static void prepareSolrXMLDataSourceConfig(final XMLStreamWriter writer, final String name, final String loopXPath)
			throws XMLStreamException {

		writer.writeStartDocument(Charsets.UTF_8.toString(), XML_VERSION);
		// dataConfig
		writer.writeStartElement(DATA_CONFIG_IDENTIFIER);

		// dataSource
		writer.writeStartElement(DATA_SOURCE_IDENTIFIER);
		writer.writeAttribute(TYPE_IDENTIFIER, HTTP_DATA_SOURCE);
		writer.writeAttribute(ENCODING_IDENTIFIER, Charsets.UTF_8.toString());
		writer.writeEndElement();

		// document
		writer.writeStartElement(DOCUMENT_IDENTIFIER);

		// entity
		writer.writeStartElement(ENTITIY_IDENTIFIER);
		writer.writeAttribute(NAME_IDENTIFIER, name);
		writer.writeAttribute(PROCESSOR_IDENTIFIER, XPATH_ENTITIY_PROCESSOR);
		writer.writeAttribute(FOR_EACH_IDENTIFIER, loopXPath);

	}

	private static void generateFields(final Collection<SchemaAttributePathInstance> sapis, final String loopXPath, final XMLStreamWriter writer) throws XMLStreamException {

		for (final SchemaAttributePathInstance sapi : sapis) {

			final AttributePath attributePath = sapi.getAttributePath();
			final Tuple<String, String> result = determineXPathFromAttributePath(attributePath);

			writer.writeStartElement(FIELD_IDENTIFIER);
			writer.writeAttribute(COLUMN_IDENTIFIER, result.v1());
			writer.writeAttribute(XPATH_IDENTIFIER, loopXPath + result.v2());
			writer.writeEndElement();
		}
	}

	private static void finalizedSolrXMLDataSourceConfig(final XMLStreamWriter writer) throws XMLStreamException {

		// close entity
		writer.writeEndElement();
		// close document
		writer.writeEndElement();
		// close dataConfig
		writer.writeEndElement();
		writer.writeEndDocument();
	}

	private static String determineXPathFromAttributePathString(final String attributePathString) {

		final org.dswarm.common.model.AttributePath attributePath = AttributePathUtil.parseAttributePathString(attributePathString);

		final StringBuilder sb = new StringBuilder();

		for (final org.dswarm.common.model.Attribute attribute : attributePath.getAttributes()) {

			sb.append(URI.SLASH);

			final URI attributeURI = new URI(attribute.getUri());

			sb.append(attributeURI.getLocalName());
		}

		return sb.toString();
	}

	/**
	 * v1 = column name
	 * v2 = xpath
	 *
	 * @param attributePath
	 * @return
	 */
	private static Tuple<String, String> determineXPathFromAttributePath(final AttributePath attributePath) {

		final List<Attribute> attributes = attributePath.getAttributePath();

		final StringBuilder sb = new StringBuilder();
		final StringBuilder sb2 = new StringBuilder();

		int i = 1;

		for (final Attribute attribute : attributes) {

			sb.append(URI.SLASH);

			final URI attributeURI = new URI(attribute.getUri());

			sb.append(attributeURI.getLocalName());
			sb2.append(attributeURI.getLocalName());

			if (i < attributes.size()) {

				sb2.append(UNDERSCORE);
			}

			i++;
		}

		return Tuple.tuple(sb2.toString(), sb.toString());
	}
}
