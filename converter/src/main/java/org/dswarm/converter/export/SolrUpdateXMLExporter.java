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
package org.dswarm.converter.export;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import org.apache.jena.vocabulary.RDF;
import org.codehaus.stax2.XMLOutputFactory2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscriber;

import org.dswarm.common.web.URI;
import org.dswarm.converter.DMPConverterError;
import org.dswarm.converter.DMPConverterException;

/**
 * @author tgaengler
 */
public class SolrUpdateXMLExporter implements Exporter<JsonNode> {

	private static final Logger LOG = LoggerFactory.getLogger(SolrUpdateXMLExporter.class);

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

	private static final String ADD_IDENTIFIER = "add";
	private static final String DOC_IDENTIFIER = "doc";
	private static final String FIELD_IDENTIFIER = "field";

	private static final String NAME_IDENTIFIER = "name";

	public Observable<JsonNode> generate(final Observable<JsonNode> recordGDM,
	                                     final OutputStream outputStream) throws XMLStreamException {

		LOG.debug("start generating Solr Update XML out of GDM");

		final XMLStreamWriter writer = xmlOutputFactory.createXMLStreamWriter(outputStream);

		writer.writeStartDocument(StandardCharsets.UTF_8.toString(), XML_VERSION);

		// process records to XML

		final SolrUpdateXMLRelationshipHandler relationshipHandler = new CBDRelationshipHandler(writer);
		final CBDNodeHandler connectRelsAndNodeHandler = new CBDNodeHandler(relationshipHandler);
		final SolrUpdateXMLNodeHandler startNodeHandler = new CBDStartNodeHandler(connectRelsAndNodeHandler, writer);

		final XMLExportOperator operator = new XMLExportOperator(writer, startNodeHandler);

		return recordGDM.lift(operator);
	}

	/**
	 * @param tag
	 * @param writer
	 * @return
	 * @throws javax.xml.stream.XMLStreamException
	 */
	private String writeXMLElement(final String tag,
	                               final XMLStreamWriter writer) throws XMLStreamException {

		// open record XML tag
		writer.writeStartElement(FIELD_IDENTIFIER);
		writer.writeAttribute(NAME_IDENTIFIER, tag);

		return tag;
	}

	private class XMLExportOperator implements Observable.Operator<JsonNode, JsonNode> {

		private final XMLStreamWriter writer;
		private AtomicBoolean wroteFirstRecord = new AtomicBoolean();
		private final SolrUpdateXMLNodeHandler startNodeHandler;
		private Optional<JsonNode> optionalFirstSingleRecordGDM;

		private XMLExportOperator(final XMLStreamWriter writerArg,
		                          final SolrUpdateXMLNodeHandler startNodeHandlerArg) {

			writer = writerArg;
			startNodeHandler = startNodeHandlerArg;
		}

		@Override
		public Subscriber<? super JsonNode> call(final Subscriber<? super JsonNode> subscriber) {

			LOG.debug("received subscriber at Solr Update XML export operator");

			final AtomicBoolean seenFirstRecord = new AtomicBoolean();
			final AtomicInteger counter = new AtomicInteger(0);

			return new Subscriber<JsonNode>() {

				@Override
				public void onCompleted() {

					if (wroteFirstRecord.get()) {

						// write end only, if at least one record was written
						endXML();
					}

					LOG.info("finished writing '{}' records for Solr Update XML export", counter.get());

					subscriber.onCompleted();
				}

				@Override
				public void onError(final Throwable throwable) {

					subscriber.onError(throwable);
				}

				@Override
				public void onNext(final JsonNode singleRecordGDM) {

					try {

						if (seenFirstRecord.compareAndSet(false, true)) {

							LOG.debug("received first record for Solr Update XML export");

							optionalFirstSingleRecordGDM = Optional.of(singleRecordGDM);

							return;
						}

						if (seenFirstRecord.get() && !wroteFirstRecord.get()) {

							startXML();

							LOG.debug("start writing first record for Solr Update XML export");

							// write first record
							startNodeHandler.handleNode(null, optionalFirstSingleRecordGDM.get());

							counter.incrementAndGet();

							wroteFirstRecord.compareAndSet(false, true);
						}

						startNodeHandler.handleNode(null, singleRecordGDM);

						counter.incrementAndGet();

						subscriber.onNext(singleRecordGDM);
					} catch (final DMPConverterException e) {

						throw DMPConverterError.wrap(e);
					} catch (final XMLStreamException e) {

						final String message = "couldn't finish Solr Update XML export successfully";

						final DMPConverterException converterException = new DMPConverterException(message, e);

						onError(converterException);
					}

				}
			};
		}

		private void startXML() {

			try {

				LOG.debug("start writing Solr Update XML for export");

				// outer xml (add element)
				writer.writeStartElement(ADD_IDENTIFIER);

			} catch (final XMLStreamException e) {

				final String message = "couldn't finish Solr Update XML export successfully";

				final DMPConverterException converterException = new DMPConverterException(message, e);

				throw DMPConverterError.wrap(converterException);
			}
		}

		private void endXML() {

			try {

				// close root nodes

				LOG.debug("finished record to Solr Update XML transformation");

				// close add element
				writer.writeEndElement();

				// close document
				writer.writeEndDocument();
			} catch (final XMLStreamException e) {

				final String message = "couldn't finish Solr Update XML export successfully";

				final DMPConverterException converterException = new DMPConverterException(message, e);

				throw DMPConverterError.wrap(converterException);
			}
		}
	}

	private class CBDNodeHandler implements SolrUpdateXMLNodeHandler {

		private final SolrUpdateXMLRelationshipHandler relationshipHandler;

		protected CBDNodeHandler(final SolrUpdateXMLRelationshipHandler relationshipHandlerArg) {

			relationshipHandler = relationshipHandlerArg;
		}

		@Override
		public void handleNode(final String previousPredicateTag,
		                       final JsonNode node) throws DMPConverterException, XMLStreamException {

			// record body is a JSON array, where each attribute has its own JSON object, i.e., each key/value pair is a single JSON object
			if (node.isArray()) {

				final Iterator<JsonNode> values = node.elements();

				while (values.hasNext()) {

					final JsonNode value = values.next();

					final JsonNodeType nodeType = value.getNodeType();

					switch (nodeType) {

						case OBJECT:

							final Iterator<Map.Entry<String, JsonNode>> fields = value.fields();

							while (fields.hasNext()) {

								final Map.Entry<String, JsonNode> field = fields.next();
								final String predicateString = field.getKey();
								final JsonNode objectNode = field.getValue();

								final URI predicateURI = new URI(predicateString);

								if(RDF.type.getURI().equals(predicateURI.toString())) {

									// skip rdf:type statements

									return;
								}

								relationshipHandler.handleRelationship(predicateURI.getLocalName(), objectNode);
							}

							break;
						case STRING:

							relationshipHandler.handleRelationship(previousPredicateTag, value);

							break;
						default:

							LOG.debug("didn't expect node type '{}' here", nodeType);
					}
				}
			}
		}
	}

	private class CBDStartNodeHandler implements SolrUpdateXMLNodeHandler {

		private final XMLStreamWriter writer;
		private final SolrUpdateXMLNodeHandler recordHandler;

		protected CBDStartNodeHandler(final SolrUpdateXMLNodeHandler recordHandlerArg,
		                              final XMLStreamWriter writerArg) {

			recordHandler = recordHandlerArg;
			writer = writerArg;
		}

		@Override
		public void handleNode(final String previousPredicateTag,
		                       final JsonNode record) throws DMPConverterException, XMLStreamException {

			// GDMModel overall node is a JSON object
			if (record.isObject()) {

				final Map.Entry<String, JsonNode> recordEntry = record.fields().next();
				// TODO: where shall we write the record URI (?) -> xml:id ?
				final String recordURIString = recordEntry.getKey();
				final JsonNode recordBody = recordEntry.getValue();

				// open record
				writer.writeStartElement(DOC_IDENTIFIER);

				// call usual NodeHandler with body
				recordHandler.handleNode(null, recordBody);
				// close record
				writer.writeEndElement();
			}
		}
	}

	/**
	 * Default handling: write literal objects as XML elements.
	 */
	private class CBDRelationshipHandler implements SolrUpdateXMLRelationshipHandler {

		protected final XMLStreamWriter writer;

		protected CBDRelationshipHandler(final XMLStreamWriter writerArg) {

			writer = writerArg;
		}

		@Override
		public void handleRelationship(final String predicateTag,
		                               final JsonNode node) throws DMPConverterException, XMLStreamException {

			// object => XML Element value or XML attribute value or further recursion

			if (!node.isContainerNode()) {

				// optionally, write literal value
				writeKeyValue(predicateTag, node);
			} else if (isTextArray(node)) {

				final Iterator<JsonNode> elements = node.elements();

				while (elements.hasNext()) {

					final JsonNode element = elements.next();

					writeKeyValue(predicateTag, element);
				}
			}
		}

		private boolean isTextArray(final JsonNode node) {

			if (!JsonNodeType.ARRAY.equals(node.getNodeType())) {

				return false;
			}

			final Iterator<JsonNode> elements = node.elements();

			if (!elements.hasNext()) {

				return false;
			}

			while (elements.hasNext()) {

				final JsonNode element = elements.next();

				if (!JsonNodeType.STRING.equals(element.getNodeType())) {

					return false;
				}
			}

			return true;
		}

		protected void writeKeyValue(final String predicateTag,
		                             final JsonNode objectGDMNode) throws XMLStreamException {

			// open tag
			writeXMLElement(predicateTag, writer);

			writer.writeCData(objectGDMNode.asText());

			// close
			writer.writeEndElement();
		}
	}
}
