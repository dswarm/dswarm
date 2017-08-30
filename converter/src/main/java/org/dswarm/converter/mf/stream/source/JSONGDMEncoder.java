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
package org.dswarm.converter.mf.stream.source;

import java.util.Map;
import java.util.Optional;
import java.util.Stack;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Nullable;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.culturegraph.mf.exceptions.MetafactureException;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.XmlReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.common.types.Tuple;
import org.dswarm.converter.mf.framework.DefaultJsonPipe;
import org.dswarm.graph.json.LiteralNode;
import org.dswarm.graph.json.Model;
import org.dswarm.graph.json.Node;
import org.dswarm.graph.json.Predicate;
import org.dswarm.graph.json.Resource;
import org.dswarm.graph.json.ResourceNode;
import org.dswarm.persistence.model.AdvancedDMPJPAObject;
import org.dswarm.persistence.model.internal.gdm.GDMModel;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.resource.utils.DataModelUtils;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.model.schema.utils.SchemaUtils;
import org.dswarm.persistence.util.GDMUtil;

/**
 * Converts JSON records to GDM triples.
 *
 * @author tgaengler
 */
@Description("triplifies records to our graph data model")
@In(XmlReceiver.class)
@Out(GDMModel.class)
public class JSONGDMEncoder extends DefaultJsonPipe<ObjectReceiver<GDMModel>> {

	private static final Logger LOG = LoggerFactory.getLogger(JSONGDMEncoder.class);

	private       String                        currentId;
	private       Model                         model;
	private       Resource                      recordResource;
	private       ResourceNode                  recordNode;
	private       Node                          entityNode;
	private       Stack<Tuple<Node, Predicate>> entityStack;
	private final Stack<String>                 elementURIStack;

	private static final String DATA_MODEL_BASE_URI = SchemaUtils.DATA_MODEL_BASE_URI + "%s";

	/**
	 * note: recordTagName is optional right now, i.e., now record tag means that the whole JSON node is the record
	 *
	 * note: recordTagName is not biunique, i.e., the record tag name can occur in different levels; hence, a record tag
	 * uniqueness is only give by a complete JSON Pointer (see RFC 6901)
	 */
	private final Optional<String> optionalRecordTagName;

	/**
	 * record tag URI should be unique
	 */
	private String recordTagUri = null;

	private boolean      inRecord;
	private String       uri;
	private ResourceNode recordType;

	private final Optional<DataModel>                         dataModel;
	private final Optional<Schema>                            optionalSchema;
	private final Optional<Map<String, AdvancedDMPJPAObject>> optionalTermMap;
	private final Optional<String>                            dataModelUri;

	private       AtomicLong                nodeIdCounter = new AtomicLong(1);
	private final Predicate                 rdfType       = new Predicate(GDMUtil.RDF_type);
	private final Map<String, Predicate>    predicates    = Maps.newHashMap();
	private final Map<String, ResourceNode> types         = Maps.newHashMap();
	private final Map<String, AtomicLong>   valueCounter  = Maps.newHashMap();
	private final Map<String, String>       uris          = Maps.newHashMap();

	public JSONGDMEncoder(final Optional<DataModel> dataModel, final boolean utiliseExistingSchema) {

		super();

		// TODO: do we need an optiopnal record tag
		optionalRecordTagName = Optional.ofNullable(System.getProperty("org.culturegraph.metamorph.xml.recordtag"));

		if (!optionalRecordTagName.isPresent()) {

			throw new MetafactureException("Missing name for the tag marking a record.");
		}

		this.dataModel = dataModel;
		dataModelUri = init(dataModel);

		// init
		elementURIStack = new Stack<>();

		final Tuple<Optional<Schema>, Optional<Map<String, AdvancedDMPJPAObject>>> tuple = getOptionalSchema(utiliseExistingSchema);
		optionalSchema = tuple.v1();
		optionalTermMap = tuple.v2();
	}

	public JSONGDMEncoder(final Optional<String> optionalRecordTagName, final Optional<DataModel> dataModel, final boolean utiliseExistingSchema) {

		super();

		this.optionalRecordTagName = optionalRecordTagName;

		this.dataModel = dataModel;
		dataModelUri = init(dataModel);

		// init
		elementURIStack = new Stack<>();

		final Tuple<Optional<Schema>, Optional<Map<String, AdvancedDMPJPAObject>>> tuple = getOptionalSchema(utiliseExistingSchema);
		optionalSchema = tuple.v1();
		optionalTermMap = tuple.v2();
	}

	@Override
	public void startObject(final String name) {

		startElement(name);
	}

	@Override
	public void endObject(final String name) {

		endElement(name);
	}

	@Override
	public void startArray(final String name) {

		// TODO: we don't need this right now, or?

		//startElement(name);
	}

	@Override
	public void endArray(final String name) {

		// TODO: we don't need this right now, or?

		//endElement(name);
	}

	private void startElement(final String name) {

		this.uri = determineElementURI(name);

		elementURIStack.push(this.uri);

		if (inRecord) {

			// TODO: is this the correct URI?
			startEntity(getTermURI(this.uri, name));
		} else if (optionalRecordTagName.isPresent() && optionalRecordTagName.get().equals(name)) {

			if (recordTagUri == null) {

				recordTagUri = getRecordTagURI(elementURIStack.peek(), name);
			}

			if (recordTagUri.equals(getRecordTagURI(elementURIStack.peek(), name))) {

				// TODO: how to determine the id of an record, or should we mint uris? - e.g. with help of a given schema that contains a content schema with a legacy record identifer
				//final String identifier = attributes.getValue("id");
				final String identifier = null;
				startRecord(identifier);
				inRecord = true;
			}

			// TODO: implement no-record-tag variant
		}
	}

	private void endElement(final String name) {

		if (inRecord) {

			final String elementUriFromStack = elementURIStack.pop();
			final String elementUri = determineElementURI(name);

			if (recordTagUri.equals(elementUriFromStack)) {
				inRecord = false;
				endRecord();
			} else {

				if (!elementUriFromStack.equals(elementUri)) {

					// LOG.debug("try to close entity for element URI '{}' with element URI '{}'", elementUriFromStack, elementUri);

					// re-add
					elementURIStack.push(elementUriFromStack);

					return;
				}

				endEntity();
			}
		}
	}

	@Override
	public void literal(final String name, final String value) {

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

	public void startRecord(final String identifier) {

		assert !isClosed();

		currentId = SchemaUtils.isValidUri(identifier) ? identifier : SchemaUtils.mintRecordUri(identifier, currentId, dataModel);

		model = new Model();
		recordResource = new Resource(currentId);
		recordNode = new ResourceNode(currentId);

		// init
		entityStack = new Stack<>();

		if (recordType == null) {

			final String recordTypeUri = recordTagUri + SchemaUtils.TYPE_POSTFIX;

			recordType = getType(recordTypeUri);
		}

		addStatement(recordNode, rdfType, recordType);
	}

	public void endRecord() {

		assert !isClosed();

		inRecord = false;

		model.addResource(recordResource);

		// write triples
		final GDMModel gdmModel = new GDMModel(model, currentId, recordType.getUri());

		// reset id
		currentId = null;

		getReceiver().process(gdmModel);
	}

	public void startEntity(final String name) {

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
	}

	public void endEntity() {

		assert !isClosed();

		// write sub resource
		entityStack.pop();

		// add entity resource to parent entity resource (or to record resource, if there is no parent entity)
		if (!entityStack.isEmpty()) {

			entityNode = entityStack.peek().v1();
		} else {

			entityNode = null;
		}
	}

	private static Optional<String> init(final Optional<DataModel> dataModel) {

		return dataModel.map(dm -> StringUtils.stripEnd(DataModelUtils.determineDataModelSchemaBaseURI(dm), SchemaUtils.HASH));
	}

	private String mintDataModelUri(@Nullable final String uri) {

		if (Strings.isNullOrEmpty(uri) || !SchemaUtils.isValidUri(uri)) {
			if (dataModelUri.isPresent()) {

				return dataModelUri.get();
			}

			return String.format(DATA_MODEL_BASE_URI, UUID.randomUUID());
		}

		return uri;
	}

	private long getNewNodeId() {

		return nodeIdCounter.incrementAndGet();
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

	private Optional<String> getOptionalURI(final String identifier) {

		if (identifier == null) {

			return Optional.empty();
		}

		return Optional.of(getURI(identifier));
	}

	private String getRecordTagURI(final String uri, final String localName) {

		if (takeURIAsIs(uri, localName)) {

			return uri;
		}

		final String typedLocalName = localName + SchemaUtils.TYPE_POSTFIX;

		final String typeRecordTagURI = getTermURI(uri, typedLocalName);

		if (!typeRecordTagURI.endsWith(SchemaUtils.TYPE_POSTFIX)) {

			return recordTagUri;
		}

		return typeRecordTagURI.substring(0, typeRecordTagURI.length() - 4);
	}

	private String getTermURI(final String uri, final String localName) {

		if (takeURIAsIs(uri, localName))

			return uri;

		if (!optionalSchema.isPresent()) {

			// do URI minting as usual
			return SchemaUtils.mintUri(uri, localName);
		}

		if (!optionalTermMap.isPresent()) {

			// do URI minting as usual
			return SchemaUtils.mintUri(uri, localName);
		}

		final Map<String, AdvancedDMPJPAObject> termMap = optionalTermMap.get();

		if (!termMap.containsKey(localName)) {

			// do URI minting as usual
			return SchemaUtils.mintUri(uri, localName);
		}

		final AdvancedDMPJPAObject term = termMap.get(localName);

		// make use of existing term uri
		return term.getUri();
	}

	private Tuple<Optional<Schema>, Optional<Map<String, AdvancedDMPJPAObject>>> getOptionalSchema(final boolean utiliseExistingSchema) {

		if (!utiliseExistingSchema) {

			return Tuple.tuple(Optional.empty(), Optional.<Map<String, AdvancedDMPJPAObject>>empty());
		}

		if (!dataModel.isPresent()) {

			return Tuple.tuple(Optional.empty(), Optional.<Map<String, AdvancedDMPJPAObject>>empty());
		}

		final Schema schema = dataModel.get().getSchema();
		final Optional<Map<String, AdvancedDMPJPAObject>> optionalTermMap = Optional.ofNullable(SchemaUtils.generateTermMap(schema));

		return Tuple.tuple(Optional.ofNullable(schema), optionalTermMap);
	}

	private String determineElementURI(final String name) {

		return getOptionalURI(name).orElseGet(() -> mintDataModelUri(name));
	}

	private boolean takeURIAsIs(final String uri, final String localName) {

		return uri != null && localName != null && uri.contains(SchemaUtils.HASH) && uri.endsWith(localName);
	}
}
