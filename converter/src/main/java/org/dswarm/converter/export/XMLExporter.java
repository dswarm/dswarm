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
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
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

import org.dswarm.common.DMPStatics;
import org.dswarm.common.model.Attribute;
import org.dswarm.common.model.AttributePath;
import org.dswarm.common.model.util.AttributePathUtil;
import org.dswarm.common.web.URI;
import org.dswarm.common.xml.utils.XMLStreamWriterUtils;
import org.dswarm.converter.DMPConverterError;
import org.dswarm.converter.DMPConverterException;

/**
 * TODO: implement namespace resetting at level change, see DD-1041
 *
 * @author tgaengler
 */
public class XMLExporter implements Exporter<JsonNode> {

	private static final Logger LOG = LoggerFactory.getLogger(XMLExporter.class);

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

	private final Map<String, URI>           predicates                 = new ConcurrentHashMap<>();
	private final Map<String, String>        namespacesPrefixesMap      = new ConcurrentHashMap<>();
	private final Map<String, String>        nameMap                    = new ConcurrentHashMap<>();
	private final Stack<Map<String, String>> namespacesPrefixesMapStack = new Stack<>();

	private boolean isElementOpen = false;

	private final URI                     recordClassURI;
	private final URI                     recordTagURI;
	private final Optional<AttributePath> optionalRootAttributePath;
	private final boolean                 originalDataTypeIsXML;

	public XMLExporter(final Optional<String> optionalRecordTagArg,
	                   final String recordClassUriArg,
	                   final Optional<String> optionalRootAttributePathArg,
	                   final Optional<String> optionalOriginalDataType) {

		recordClassURI = new URI(recordClassUriArg);

		if (optionalRecordTagArg.isPresent()) {

			recordTagURI = new URI(optionalRecordTagArg.get());
		} else {

			// record class URI as fall back

			recordTagURI = new URI(recordClassUriArg);
		}

		if (optionalRootAttributePathArg.isPresent()) {

			final String rootAttributePathString = optionalRootAttributePathArg.get();
			final AttributePath rootAttributePath = AttributePathUtil.parseAttributePathString(rootAttributePathString);

			optionalRootAttributePath = Optional.ofNullable(rootAttributePath);
		} else {

			optionalRootAttributePath = Optional.empty();
		}

		originalDataTypeIsXML = optionalOriginalDataType.isPresent() && DMPStatics.XML_DATA_TYPE.equals(optionalOriginalDataType.get());
	}

	public Observable<JsonNode> generate(final Observable<JsonNode> recordGDM,
	                                     final OutputStream outputStream) throws XMLStreamException {

		LOG.debug("start generating XML out of GDM");

		final XMLStreamWriter writer = xmlOutputFactory.createXMLStreamWriter(outputStream);

		writer.writeStartDocument(StandardCharsets.UTF_8.toString(), XML_VERSION);

		// process records to XML

		final XMLRelationshipHandler relationshipHandler;

		if (originalDataTypeIsXML) {

			relationshipHandler = new CBDRelationshipXMLDataModelHandler(writer);
		} else {

			relationshipHandler = new CBDRelationshipHandler(writer);
		}

		final CBDNodeHandler connectRelsAndNodeHandler = new CBDNodeHandler(relationshipHandler);
		final XMLNodeHandler startNodeHandler = new CBDStartNodeHandler(connectRelsAndNodeHandler, writer);

		final XMLExportOperator operator = new XMLExportOperator(writer, startNodeHandler);

		return recordGDM.lift(operator);
	}

	/**
	 * same method as in PropertyGraphXMLReader
	 *
	 * @param writer
	 * @throws XMLStreamException
	 */
	private void setDefaultNamespace(final XMLStreamWriter writer) throws XMLStreamException {

		// TODO: shall we cut the last character?

		final String defaultNameSpace;

		if (recordTagURI.hasNamespaceURI()) {

			defaultNameSpace = XMLStreamWriterUtils.determineBaseURI(recordTagURI);
		} else {

			defaultNameSpace = XMLStreamWriterUtils.determineBaseURI(recordClassURI);
		}

		writer.setDefaultNamespace(defaultNameSpace);
	}

	/**
	 * same method as in PropertyGraphXMLReader
	 *
	 * @param uri
	 * @param writer
	 * @return
	 * @throws XMLStreamException
	 */
	private URI determineAndWriteXMLElementAndNamespace(final URI uri,
	                                                    final XMLStreamWriter writer) throws XMLStreamException {

		final String prefix;
		final String namespace;
		final String finalURIString;
		final boolean namespaceAlreadySet;

		if (uri.hasNamespaceURI()) {

			namespace = XMLStreamWriterUtils.determineBaseURI(uri);
			namespaceAlreadySet = namespacesPrefixesMap.containsKey(namespace);
			prefix = XMLStreamWriterUtils.getPrefix(namespace, namespacesPrefixesMap);

			finalURIString = uri.getNamespaceURI() + uri.getLocalName();
		} else {

			namespace = XMLStreamWriterUtils.determineBaseURI(recordClassURI);
			namespaceAlreadySet = namespacesPrefixesMap.containsKey(namespace);
			prefix = XMLStreamWriterUtils.getPrefix(namespace, namespacesPrefixesMap);

			finalURIString = recordClassURI.getNamespaceURI() + uri.getLocalName();
		}

		final URI finalURI = new URI(finalURIString);

		// open record XML tag
		XMLStreamWriterUtils.writeXMLElementTag(writer, finalURI, namespacesPrefixesMap, nameMap, isElementOpen);
		isElementOpen = true;

		if (!namespaceAlreadySet) {

			writer.writeNamespace(prefix, namespace);
		}

		return finalURI;
	}

	private class XMLExportOperator implements Observable.Operator<JsonNode, JsonNode> {

		private final XMLStreamWriter writer;
		private AtomicBoolean hasAtLeastTwoRecords = new AtomicBoolean();
		private AtomicBoolean wroteFirstRecord     = new AtomicBoolean();
		private final XMLNodeHandler     startNodeHandler;
		private       Optional<JsonNode> optionalFirstSingleRecordGDM;

		private XMLExportOperator(final XMLStreamWriter writerArg,
		                          final XMLNodeHandler startNodeHandlerArg) {

			writer = writerArg;
			startNodeHandler = startNodeHandlerArg;
		}

		@Override public Subscriber<? super JsonNode> call(final Subscriber<? super JsonNode> subscriber) {

			LOG.debug("received subscriber at XML export operator");

			final AtomicBoolean seenFirstRecord = new AtomicBoolean();
			final AtomicInteger counter = new AtomicInteger(0);

			return new Subscriber<JsonNode>() {

				@Override public void onCompleted() {

					try {

						if (!hasAtLeastTwoRecords.get() && seenFirstRecord.get() && !wroteFirstRecord.get()) {

							// write first (and only) record, if no record was written before, i.e., the model consists only of one record

							startXML();

							LOG.info("start writing first record for XML export");

							startNodeHandler.handleNode(null, optionalFirstSingleRecordGDM.get());

							counter.incrementAndGet();

							wroteFirstRecord.compareAndSet(false, true);
						}

						if (wroteFirstRecord.get()) {

							// write end only, if at least one record was written
							endXML();
						}

						LOG.info("finished writing '{}' records for XML export", counter.get());

						subscriber.onCompleted();
					} catch (final DMPConverterException e) {

						throw DMPConverterError.wrap(e);
					} catch (final XMLStreamException e) {

						final String message = "couldn't finish xml export successfully";

						final DMPConverterException converterException = new DMPConverterException(message, e);

						onError(converterException);
					}
				}

				@Override public void onError(final Throwable throwable) {

					subscriber.onError(throwable);
				}

				@Override public void onNext(final JsonNode singleRecordGDM) {

					try {

						if (seenFirstRecord.compareAndSet(false, true)) {

							LOG.debug("received first record for XML export");

							optionalFirstSingleRecordGDM = Optional.of(singleRecordGDM);

							return;
						}

						if (seenFirstRecord.get() && !wroteFirstRecord.get()) {

							hasAtLeastTwoRecords.compareAndSet(false, true);

							startXML();

							LOG.debug("start writing first record for XML export");

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

						final String message = "couldn't finish xml export successfully";

						final DMPConverterException converterException = new DMPConverterException(message, e);

						onError(converterException);
					}

				}
			};
		}

		private void startXML() {

			try {

				LOG.debug("start writing XML for export");

				boolean defaultNamespaceWritten = false;

				if (optionalRootAttributePath.isPresent()) {

					// open root attribute path tags

					final AttributePath rootAttributePath = optionalRootAttributePath.get();

					for (final Attribute attribute : rootAttributePath.getAttributes()) {

						final URI attributeURI = new URI(attribute.getUri());

						if (!defaultNamespaceWritten && attributeURI.hasNamespaceURI()) {

							// set default namespace

							writer.setDefaultNamespace(attributeURI.getNamespaceURI());

							defaultNamespaceWritten = true;
						}

						XMLStreamWriterUtils.writeXMLElementTag(writer, attributeURI, namespacesPrefixesMap, nameMap, isElementOpen);
						isElementOpen = true;
					}
				} else if (hasAtLeastTwoRecords.get()) {

					// write default root
					final URI defaultRootURI = new URI(recordTagURI + "s");

					determineAndWriteXMLElementAndNamespace(defaultRootURI, writer);
				}

				if (!defaultNamespaceWritten && recordTagURI.hasNamespaceURI()) {

					// set default namespace
					setDefaultNamespace(writer);
				}

				// add all namespace from the root to the first level
				namespacesPrefixesMapStack.push(new ConcurrentHashMap<>());
				namespacesPrefixesMapStack.peek().putAll(namespacesPrefixesMap);
			} catch (final XMLStreamException e) {

				final String message = "couldn't finish xml export successfully";

				final DMPConverterException converterException = new DMPConverterException(message, e);

				throw DMPConverterError.wrap(converterException);
			}
		}

		private void endXML() {

			try {

				// close root nodes

				LOG.debug("finished record to XML transformation");

				if (optionalRootAttributePath.isPresent()) {

					// close root attribute path tags

					for (int i = 0; i < optionalRootAttributePath.get().getAttributes().size(); i++) {

						writer.writeEndElement();
					}
				} else if (hasAtLeastTwoRecords.get()) {

					// close default root
					writer.writeEndElement();
				}

				// close document
				writer.writeEndDocument();
			} catch (final XMLStreamException e) {

				final String message = "couldn't finish xml export successfully";

				final DMPConverterException converterException = new DMPConverterException(message, e);

				throw DMPConverterError.wrap(converterException);
			}
		}
	}

	private class CBDNodeHandler implements XMLNodeHandler {

		private final XMLRelationshipHandler relationshipHandler;

		protected CBDNodeHandler(final XMLRelationshipHandler relationshipHandlerArg) {

			relationshipHandler = relationshipHandlerArg;
			((CBDRelationshipHandler) relationshipHandler).setNodeHandler(this);
		}

		@Override
		public void handleNode(final URI previousPredicateURI,
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

								final URI predicateURI = getPredicate(predicateString);

								relationshipHandler.handleRelationship(predicateURI, objectNode);
							}

							break;
						case STRING:

							relationshipHandler.handleRelationship(previousPredicateURI, value);

							break;
						default:

							LOG.debug("didn't expect node type '{}' here", nodeType);

					}
				}
			}
		}
	}

	private class CBDStartNodeHandler implements XMLNodeHandler {

		private final XMLStreamWriter writer;
		private final XMLNodeHandler  recordHandler;

		protected CBDStartNodeHandler(final XMLNodeHandler recordHandlerArg,
		                              final XMLStreamWriter writerArg) {

			recordHandler = recordHandlerArg;
			writer = writerArg;
		}

		@Override
		public void handleNode(final URI previousPredicateURI,
		                       final JsonNode record) throws DMPConverterException, XMLStreamException {

			// GDMModel overall node is a JSON object
			if (record.isObject()) {

				final Map.Entry<String, JsonNode> recordEntry = record.fields().next();
				// TODO: where shall we write the record URI (?) -> xml:id ?
				final String recordURIString = recordEntry.getKey();
				final JsonNode recordBody = recordEntry.getValue();

				determineAndWriteXMLElementAndNamespace(recordTagURI, writer);

				// call usual NodeHandler with body
				recordHandler.handleNode(null, recordBody);
				// close record
				writer.writeEndElement();
				isElementOpen = false;

				// simply reset the namespaces prefixes map to that one from one level above
				namespacesPrefixesMap.clear();
				namespacesPrefixesMap.putAll(namespacesPrefixesMapStack.peek());
			}
		}
	}

	/**
	 * Default handling: don't export RDF types and write literal objects as XML elements.
	 */
	private class CBDRelationshipHandler implements XMLRelationshipHandler {

		protected final XMLStreamWriter writer;
		private         XMLNodeHandler  nodeHandler;

		protected CBDRelationshipHandler(final XMLStreamWriter writerArg) {

			writer = writerArg;
		}

		protected void setNodeHandler(final XMLNodeHandler nodeHandlerArg) {

			nodeHandler = nodeHandlerArg;
		}

		@Override
		public void handleRelationship(final URI predicateURI,
		                               final JsonNode node) throws DMPConverterException, XMLStreamException {

			// object => XML Element value or XML attribute value or further recursion

			if (!node.isContainerNode()) {

				// optionally, write literal value
				writeKeyValue(predicateURI, node);
			} else if (isTextArray(node)) {

				final Iterator<JsonNode> elements = node.elements();

				while (elements.hasNext()) {

					final JsonNode element = elements.next();

					writeKeyValue(predicateURI, element);
				}
			} else {

				// open tag
				XMLStreamWriterUtils.writeXMLElementTag(writer, predicateURI, namespacesPrefixesMap, nameMap, isElementOpen);
				isElementOpen = true;

				// continue traversal with object node
				nodeHandler.handleNode(predicateURI, node);

				// close
				writer.writeEndElement();
				isElementOpen = false;
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

		protected void writeKeyValue(final URI predicateURI,
		                             final JsonNode objectGDMNode) throws XMLStreamException {

			// default handling: don't export RDF types and write literal objects as XML elements
			if (!RDF.type.getURI().equals(predicateURI.toString())) {

				// open tag
				XMLStreamWriterUtils.writeXMLElementTag(writer, predicateURI, namespacesPrefixesMap, nameMap, isElementOpen);

				writer.writeCData(objectGDMNode.asText());

				// close
				writer.writeEndElement();
				isElementOpen = false;
			} else {

				// TODO: ???
			}
		}
	}

	/**
	 * Treat non-rdf:value/non-rdf:type statements with literal objects as XML attributes and rdf:value statements with literal
	 * objects as XML elements.
	 */
	private class CBDRelationshipXMLDataModelHandler extends CBDRelationshipHandler {

		protected CBDRelationshipXMLDataModelHandler(final XMLStreamWriter writerArg) {

			super(writerArg);
		}

		@Override
		protected void writeKeyValue(final URI predicateURI,
		                             final JsonNode objectGDMNode) throws XMLStreamException {

			if (!(RDF.type.getURI().equals(predicateURI.toString()) || RDF.value.getURI().equals(predicateURI.toString()))) {

				// predicate is an XML Attribute => write XML Attribute to this XML Element

				final String value = objectGDMNode.asText();

				XMLStreamWriterUtils
						.writeXMLAttribute(writer, predicateURI, value, namespacesPrefixesMap, nameMap);
			} else if (RDF.value.getURI().equals(predicateURI.toString())) {

				// predicate is an XML Element

				// TODO: what should we do with objects that are resources?
				writer.writeCData(objectGDMNode.asText());
			} else {

				// ??? - log these occurrences?
			}
		}

	}

	private URI getPredicate(final String predicateString) {

		if (!predicates.containsKey(predicateString)) {

			predicates.put(predicateString, new URI(predicateString));
		}

		return predicates.get(predicateString);
	}
}
