/**
 * Copyright (C) 2013 – 2015 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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
package org.dswarm.converter.mf.stream.source;

import java.util.Map;
import java.util.Optional;
import java.util.Stack;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.culturegraph.mf.exceptions.MetafactureException;
import org.culturegraph.mf.framework.DefaultXmlPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.XmlReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.dswarm.common.types.Tuple;
import org.dswarm.graph.json.LiteralNode;
import org.dswarm.graph.json.Model;
import org.dswarm.graph.json.Node;
import org.dswarm.graph.json.Predicate;
import org.dswarm.graph.json.Resource;
import org.dswarm.graph.json.ResourceNode;
import org.dswarm.persistence.model.internal.gdm.GDMModel;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.resource.utils.DataModelUtils;
import org.dswarm.persistence.model.schema.utils.SchemaUtils;
import org.dswarm.persistence.util.GDMUtil;

/**
 * Converts XML records to GDM triples.
 *
 * @author tgaengler
 * @author phorn
 */
@Description("triplifies records to our graph data model")
@In(XmlReceiver.class)
@Out(GDMModel.class)
public final class XMLGDMEncoder extends DefaultXmlPipe<ObjectReceiver<GDMModel>> {

	private       String                        currentId;
	private       Model                         model;
	private       Resource                      recordResource;
	private       ResourceNode                  recordNode;
	private       Node                          entityNode;
	private       Stack<Tuple<Node, Predicate>> entityStack;
	private final Stack<String>                 elementURIStack;

	private static final String DATA_MODEL_BASE_URI = SchemaUtils.DATA_MODEL_BASE_URI + "%s";

	private static final Pattern TABS = Pattern.compile("\t+");

	/**
	 * note: recordTagName is not biunique, i.e., the record tag name can occur in different name spaces; hence, a record tag
	 * uniqueness is only give by a complete uri
	 */
	private final String recordTagName;

	/**
	 * record tag URI should be unique
	 */
	private String recordTagUri = null;

	private boolean inRecord;
	private StringBuilder valueBuffer = new StringBuilder();
	private String       uri;
	private ResourceNode recordType;

	private final Optional<DataModel> dataModel;
	private final Optional<String>    dataModelUri;

	private       long                      nodeIdCounter = 1;
	private final Predicate                 rdfType       = new Predicate(GDMUtil.RDF_type);
	private final Map<String, Predicate>    predicates    = Maps.newHashMap();
	private final Map<String, ResourceNode> types         = Maps.newHashMap();
	private final Map<String, AtomicLong>   valueCounter  = Maps.newHashMap();
	private final Map<String, String>       uris          = Maps.newHashMap();

	public XMLGDMEncoder(final Optional<DataModel> dataModel) {
		super();

		recordTagName = System.getProperty("org.culturegraph.metamorph.xml.recordtag");
		if (recordTagName == null) {
			throw new MetafactureException("Missing name for the tag marking a record.");
		}

		this.dataModel = dataModel;
		dataModelUri = init(dataModel);

		// init
		elementURIStack = new Stack<>();
		//model = new Model();
	}

	public XMLGDMEncoder(final String recordTagName, final Optional<DataModel> dataModel) {
		super();
		this.recordTagName = Preconditions.checkNotNull(recordTagName);

		this.dataModel = dataModel;
		dataModelUri = init(dataModel);

		// init
		elementURIStack = new Stack<>();
		//model = new Model();
	}

	@Override
	public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {

		this.uri = mintDataModelUri(uri);

		elementURIStack.push(this.uri);

		if (inRecord) {
			writeValue();
			startEntity(SchemaUtils.mintUri(uri, localName));
			writeAttributes(attributes);
		} else if (localName.equals(recordTagName)) {

			if (recordTagUri == null) {

				recordTagUri = SchemaUtils.mintUri(elementURIStack.peek(), localName);
			}

			if (recordTagUri.equals(SchemaUtils.mintUri(elementURIStack.peek(), localName))) {

				// TODO: how to determine the id of an record, or should we mint uris?
				final String identifier = attributes.getValue("id");
				startRecord(identifier);
				writeAttributes(attributes);
				inRecord = true;
			}
		}
	}

	@Override
	public void endElement(final String uri, final String localName, final String qName) throws SAXException {

		// System.out.println("in end element with: uri = '" + uri + "' :: local name = '" + localName + "'");

		if (inRecord) {
			writeValue();

			final String elementUri = elementURIStack.pop();

			if (recordTagUri.equals(SchemaUtils.mintUri(elementUri, localName))) {
				inRecord = false;
				endRecord();
			} else {
				endEntity();
			}
		}
	}

	@Override
	public void characters(final char[] chars, final int start, final int length) throws SAXException {
		if (inRecord) {
			valueBuffer.append(XMLGDMEncoder.TABS.matcher(new String(chars, start, length)).replaceAll(""));
		}
	}

	private void writeValue() {
		final String value = valueBuffer.toString();
		if (!value.trim().isEmpty()) {
			literal(GDMUtil.RDF_value, value.replace('\n', ' '));
		}
		valueBuffer = new StringBuilder();
	}

	private void writeAttributes(final Attributes attributes) {
		final int length = attributes.getLength();

		for (int i = 0; i < length; ++i) {
			final String name = SchemaUtils.mintUri(uri, attributes.getLocalName(i));
			final String value = attributes.getValue(i);
			literal(name, value);
		}
	}

	public void startRecord(final String identifier) {

		// System.out.println("in start record with: identifier = '" + identifier + "'");

		assert !isClosed();

		currentId = SchemaUtils.isValidUri(identifier) ? identifier : SchemaUtils.mintRecordUri(identifier, currentId, dataModel);

		model = new Model();
		recordResource = new Resource(currentId);
		recordNode = new ResourceNode(currentId);

		// init
		entityStack = new Stack<>();

		// TODO: determine record type and create type triple with it
		if (recordType == null) {

			final String recordTypeUri = recordTagUri + SchemaUtils.TYPE_POSTFIX;

			recordType = getType(recordTypeUri);
		}

		addStatement(recordNode, rdfType, recordType);
	}

	public void endRecord() {

		// System.out.println("in end record");

		assert !isClosed();

		inRecord = false;

		model.addResource(recordResource);

		// write triples
		final GDMModel gdmModel = new GDMModel(model, currentId, recordType.getUri());

		// OutputStream os;
		// try {
		// os = new FileOutputStream("test.n3");
		//
		// rdfModel.getModel().write(os, "N3");
		//
		// final PrintStream printStream = new PrintStream(os);
		// printStream.close();
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		// reset id
		currentId = null;

		getReceiver().process(gdmModel);
	}

	@Override
	public void startEntity(final String name) {

		// System.out.println("in start entity with name = '" + name + "'");

		assert !isClosed();

		// bnode or url
		entityNode = new Node(getNewNodeId());

		final Predicate entityPredicate = getPredicate(name);

		// write sub resource statement
		if (!entityStack.isEmpty()) {

			final Tuple<Node, Predicate> parentEntityTuple = entityStack.peek();

			addStatement(parentEntityTuple.v1(), entityPredicate, entityNode);
		} else {

			addStatement(recordNode, entityPredicate, entityNode);
		}

		// sub resource type
		final ResourceNode entityType = getType(name + SchemaUtils.TYPE_POSTFIX);

		addStatement(entityNode, rdfType, entityType);

		entityStack.push(new Tuple<>(entityNode, entityPredicate));

		// System.out.println("in start entity with entity stact size: '" + entityStack.size() + "'");
	}

	public void endEntity() {

		// System.out.println("in end entity");

		assert !isClosed();

		// write sub resource
		entityStack.pop();

		// System.out.println("in end entity with entity stact size: '" + entityStack.size() + "'");

		// add entity resource to parent entity resource (or to record resource, if there is no parent entity)
		if (!entityStack.isEmpty()) {

			entityNode = entityStack.peek().v1();
		} else {

			entityNode = null;
		}
	}

	public void literal(final String name, final String value) {

		// System.out.println("in literal with name = '" + name + "' :: value = '" + value + "'");

		assert !isClosed();

		// create triple
		// name = predicate
		// value = literal or object
		// TODO: only literals atm, i.e., how to determine other resources?
		if (value != null && !value.isEmpty()) {
			final Predicate attributeProperty = getPredicate(name);
			final LiteralNode literalObject = new LiteralNode(value);

			if (null != entityNode) {

				addStatement(entityNode, attributeProperty, literalObject);
			} else if (null != recordResource) {

				addStatement(recordNode, attributeProperty, literalObject);
			} else {

				throw new MetafactureException("couldn't get a resource for adding this property");
			}
		}
	}

	private static Optional<String> init(final Optional<DataModel> dataModel) {
		return dataModel.map(dm -> StringUtils.stripEnd(DataModelUtils.determineDataModelSchemaBaseURI(dm), SchemaUtils.HASH));
	}

	private String mintDataModelUri(@Nullable final String uri) {
		if (Strings.isNullOrEmpty(uri)) {
			if (dataModelUri.isPresent()) {

				return dataModelUri.get();
			}

			return String.format(DATA_MODEL_BASE_URI, UUID.randomUUID());
		}

		return uri;
	}

	private long getNewNodeId() {

		final long newNodeId = nodeIdCounter;
		nodeIdCounter++;

		return newNodeId;
	}

	private Predicate getPredicate(final String predicateId) {

		final String predicateURI = getURI(predicateId);

		if (!predicates.containsKey(predicateURI)) {

			final Predicate predicate = new Predicate(predicateURI);

			predicates.put(predicateURI, predicate);
		}

		return predicates.get(predicateURI);
	}

	private ResourceNode getType(final String typeId) {

		final String typeURI = getURI(typeId);

		if (!types.containsKey(typeURI)) {

			final ResourceNode type = new ResourceNode(typeURI);

			types.put(typeURI, type);
		}

		return types.get(typeURI);
	}

	private void addStatement(final Node subject, final Predicate predicate, final Node object) {

		String key;

		if (subject instanceof ResourceNode) {

			key = ((ResourceNode) subject).getUri();
		} else {

			key = subject.getId().toString();
		}

		key += "::" + predicate.getUri();

		if (!valueCounter.containsKey(key)) {

			final AtomicLong valueCounterForKey = new AtomicLong(0);
			valueCounter.put(key, valueCounterForKey);
		}

		final Long order = valueCounter.get(key).incrementAndGet();

		recordResource.addStatement(subject, predicate, object, order);
	}

	private String getURI(final String id) {

		if (!uris.containsKey(id)) {

			final String uri = SchemaUtils.isValidUri(id) ? id : SchemaUtils.mintTermUri(null, id, dataModelUri);

			uris.put(id, uri);
		}

		return uris.get(id);
	}
}
