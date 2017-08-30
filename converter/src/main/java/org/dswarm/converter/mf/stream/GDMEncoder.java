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
package org.dswarm.converter.mf.stream;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javaslang.Tuple;
import javaslang.Tuple2;
import org.apache.commons.lang3.StringUtils;
import org.culturegraph.mf.exceptions.MetafactureException;
import org.culturegraph.mf.framework.DefaultStreamPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.morph.functions.model.ValueConverter;
import org.culturegraph.mf.morph.functions.model.ValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * Converts records to GDM-JSON.
 *
 * @author polowins
 * @author tgaengler
 * @author phorn
 */
@Description("converts records to GDM-JSON")
@In(StreamReceiver.class)
@Out(GDMModel.class)
public final class GDMEncoder extends DefaultStreamPipe<ObjectReceiver<GDMModel>> {

	private static final Logger LOG = LoggerFactory.getLogger(GDMEncoder.class);

	private static final String RESOURCE_IDENTIFIER = "resource";
	private static final String RESOURCE_BASE_URI = SchemaUtils.BASE_URI + RESOURCE_IDENTIFIER + SchemaUtils.SLASH;
	private static final String KEY_PREFIX = "::";

	private String currentId;
	private Model internalGDMModel;
	private ResourceNode recordNode;
	private Node entityNode;
	private Resource currentResource;
	private Stack<Tuple2<Node, Predicate>> entityStack;

	private ResourceNode recordType;
	private Resource recordResource;

	private final Optional<DataModel> dataModel;
	private final Optional<String> dataModelUri;

	private final Map<String, Predicate> predicates = new HashMap<>();
	private final Map<String, AtomicLong> valueCounter = new HashMap<>();
	private final Map<String, String> uris = new HashMap<>();
	private Map<String, ResourceNode> resourceNodeCache;
	private AtomicLong bnodeCounter;
	private Map<String, Long> bnodeMap;
	private Map<String, Node> bnodeCache;

	private final AtomicInteger inComingCounter = new AtomicInteger(0);
	private final AtomicInteger outGoingCounter = new AtomicInteger(0);

	private final AtomicInteger inComingCounter2 = new AtomicInteger(0);
	private final AtomicInteger outGoingCounter2 = new AtomicInteger(0);

	public GDMEncoder(final Optional<DataModel> dataModel) {

		super();

		this.dataModel = dataModel;
		dataModelUri = init(dataModel);

	}

	public int getInComingCounter() {

		return inComingCounter.get();
	}

	public int getOutGoingCounter() {

		return outGoingCounter.get();
	}

	public int getInComingCounter2() {

		return inComingCounter2.get();
	}

	public int getOutGoingCounter2() {

		return outGoingCounter2.get();
	}

	@Override
	public void startRecord(final String identifier) {

		resourceNodeCache = new ConcurrentHashMap<>();
		// note: this should be resource-safe in reality (i.e. one counter, cache etc, per resource (not record; i.e. assume a record has multiple sub-resources))
		bnodeCounter = new AtomicLong(0);
		bnodeMap = new ConcurrentHashMap<>();
		bnodeCache = new ConcurrentHashMap<>();

		inComingCounter.incrementAndGet();

		assert !isClosed();

		inComingCounter2.incrementAndGet();

		currentId = SchemaUtils.isValidUri(identifier) ? identifier : SchemaUtils.mintRecordUri(identifier, currentId, dataModel);

		internalGDMModel = new Model();
		recordResource = getOrCreateResource(currentId);

		recordNode = getOrCreateResourceNode(currentId);

		currentResource = recordResource;

		// init
		entityStack = new Stack<>();
	}

	@Override
	public void endRecord() {

		outGoingCounter.incrementAndGet();

		assert !isClosed();

		outGoingCounter2.incrementAndGet();

		currentResource = null;

		// write triples
		final GDMModel gdmModel;

		if (recordType != null) {

			gdmModel = new GDMModel(internalGDMModel, currentId, recordType.getUri());
		} else {

			gdmModel = new GDMModel(internalGDMModel, currentId);
		}

		currentId = null;
		recordNode = null;
		recordType = null;

		getReceiver().process(gdmModel);
	}

	@Override
	public void startEntity(final String name) {

		assert !isClosed();

		currentResource = null;

		final Predicate entityPredicate = getPredicate(name);

		final String entityUri = mintEntityUri();

		entityNode = getOrCreateBNode(entityUri);

		if (entityStack.empty()) {

			addStatement(recordNode, entityPredicate, entityNode);
		} else {

			final Tuple2<Node, Predicate> parentEntityTuple = entityStack.peek();

			addStatement(parentEntityTuple._1, entityPredicate, entityNode);
		}

		entityStack.push(Tuple.of(entityNode, entityPredicate));
	}

	@Override
	public void endEntity() {

		assert !isClosed();

		entityStack.pop();

		if (!entityStack.isEmpty()) {

			entityNode = entityStack.peek()._1;
		} else {

			entityNode = null;
		}
	}

	@Override
	public void literal(final String name, final String value) {

		// System.out.println("in literal with name = '" + name + "' :: value = '" + value + "'");

		assert !isClosed();

		if (name == null) {

			return;
		}

		final String propertyUri;

		if (SchemaUtils.isValidUri(name)) {

			propertyUri = name;
		} else {

			propertyUri = SchemaUtils.mintUri(dataModelUri.get(), name);
		}

		final Node currentNode = entityStack.empty() ? recordNode : entityNode;

		if (null != currentNode) {

			final Predicate attributeProperty = getPredicate(propertyUri);

			// note: only non-empty values will be emitted right now
			if (value != null && !value.isEmpty()) {

				final Map.Entry<ValueType, String> valueEntry = ValueConverter.decodeTypeInfo(value);
				final ValueType valueType = valueEntry.getKey();
				final String realValue = valueEntry.getValue();

				final Node objectNode;

				switch (valueType) {

					case Resource:

						objectNode = getOrCreateResourceNode(realValue);

						break;
					case BNode:

						objectNode = getOrCreateBNode(realValue);

						break;
					default:

						// case = Literal

						// TODO: this is only a HOTFIX for creating resources from resource type uris
						// check, whether value is really a URI
						if (!(GDMUtil.RDF_type.equals(propertyUri) && SchemaUtils.isValidUri(realValue))) {

							objectNode = new LiteralNode(realValue);
						} else {

							final ResourceNode resourceTypeNode = getOrCreateResourceNode(realValue);
							objectNode = resourceTypeNode;

							if (recordResource.equals(currentResource)) {

								recordType = resourceTypeNode;
							}
						}
				}

				addStatement(currentNode, attributeProperty, objectNode);
			} else {

				if (LOG.isTraceEnabled()) {

					LOG.trace("won't write statement for subject '{}' + predicate '{}', because the value is not existing or empty", currentNode,
							attributeProperty);
				}
			}
		} else {

			throw new MetafactureException("couldn't get a resource for adding this property");
		}
	}

	private Optional<String> init(final Optional<DataModel> dataModel) {

		return Optional.ofNullable(dataModel
				.map(dm -> StringUtils.stripEnd(DataModelUtils.determineDataModelSchemaBaseURI(dm), SchemaUtils.HASH))
				.orElseGet(() -> StringUtils.stripEnd(DataModelUtils.determineDataModelSchemaBaseURI(null), SchemaUtils.HASH)));
	}

	private static String mintEntityUri() {

		return RESOURCE_BASE_URI + UUID.randomUUID();
	}

	private Predicate getPredicate(final String predicateId) {

		final String predicateURI = getURI(predicateId);

		return predicates.computeIfAbsent(predicateURI, predicateURI1 -> new Predicate(predicateURI));
	}

	private void addStatement(final Node subject, final Predicate predicate, final Node object) {

		String key;

		if (subject instanceof ResourceNode) {

			key = ((ResourceNode) subject).getUri();
		} else {

			key = subject.getId().toString();
		}

		key += KEY_PREFIX + predicate.getUri();

		final Long order = valueCounter.computeIfAbsent(key, key1 -> new AtomicLong(0)).incrementAndGet();

		recordResource.addStatement(subject, predicate, object, order);
	}

	private String getURI(final String id) {

		return uris.computeIfAbsent(id, id1 -> SchemaUtils.isValidUri(id) ? id : SchemaUtils.mintTermUri(null, id, dataModelUri));
	}

	private Resource getOrCreateResource(final String resourceURI) {

		final Resource resourceFromModel = internalGDMModel.getResource(resourceURI);

		if (resourceFromModel != null) {

			return resourceFromModel;
		}

		final Resource newResource = new Resource(resourceURI);
		internalGDMModel.addResource(newResource);

		return newResource;
	}

	private ResourceNode getOrCreateResourceNode(final String resourceURI) {

		return resourceNodeCache.computeIfAbsent(resourceURI, resourceURI1 -> new ResourceNode(resourceURI));
	}

	private Node getOrCreateBNode(final String bNodeId) {

		return bnodeCache.computeIfAbsent(bNodeId, bNodeId1 -> {

			final Long bNodeLongId = getOrCreateBNodeLongId(bNodeId);

			return new Node(bNodeLongId);
		});
	}

	private Long getOrCreateBNodeLongId(final String bnodeStringId) {

		return bnodeMap.computeIfAbsent(bnodeStringId, bnodeStringId1 -> bnodeCounter.incrementAndGet());
	}

}
