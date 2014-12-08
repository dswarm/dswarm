/**
 * Copyright (C) 2013, 2014 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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

import java.util.Map;
import java.util.Stack;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.culturegraph.mf.exceptions.MetafactureException;
import org.culturegraph.mf.framework.DefaultStreamPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;

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
import org.dswarm.persistence.model.types.Tuple;
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

	private static final String RESOURCE_BASE_URI = SchemaUtils.BASE_URI + "/resource/";
	private       String                                currentId;
	private final Model                                 internalGDMModel;
	private       ResourceNode                          recordNode;
	private       ResourceNode                          entityNode;
	private       Resource                              currentResource;
	private       Stack<Tuple<ResourceNode, Predicate>> entityStack;
	private       Stack<Resource>                       resourceStack;

	private ResourceNode recordType;
	private Resource     recordResource;

	private final Optional<DataModel> dataModel;
	private final Optional<String>    dataModelUri;

	private final Map<String, Predicate>    predicates   = Maps.newHashMap();
	private final Map<String, AtomicLong>   valueCounter = Maps.newHashMap();
	private final Map<String, ResourceNode> types        = Maps.newHashMap();
	private final Map<String, String>       uris         = Maps.newHashMap();

	public GDMEncoder(final Optional<DataModel> dataModel) {

		super();

		this.dataModel = dataModel;
		dataModelUri = init(dataModel);

		internalGDMModel = new Model();

		resourceStack = new Stack<>();

	}

	@Override
	public void startRecord(final String identifier) {

		assert !isClosed();

		currentId = SchemaUtils.isValidUri(identifier) ? identifier : SchemaUtils.mintRecordUri(identifier, currentId, dataModel);

		recordResource = getOrCreateResource(currentId);

		recordNode = new ResourceNode(currentId);

		resourceStack.push(recordResource);

		currentResource = recordResource;

		// init
		entityStack = new Stack<>();

	}

	@Override
	public void endRecord() {

		assert !isClosed();

		resourceStack.clear();

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

		// TODO: remove this, when everything works fine
		//		System.out.println("###############################");
		//		try {
		//			System.out.println(Util.getJSONObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(internalGDMModel));
		//		} catch (JsonProcessingException e) {
		//			e.printStackTrace();
		//		}
		//		System.out.println("###############################");
	}

	@Override
	public void startEntity(final String name) {

		assert !isClosed();

		final Predicate entityPredicate = getPredicate(name);

		final String entityUri = mintEntityUri();

		final Resource entityResource = getOrCreateResource(entityUri);

		entityNode = new ResourceNode(entityUri);

		if (entityStack.empty()) {

			addStatement(recordNode, entityPredicate, entityNode);
		} else {

			final Tuple<ResourceNode, Predicate> parentEntityTuple = entityStack.peek();

			addStatement(parentEntityTuple.v1(), entityPredicate, entityNode);
		}

		currentResource = entityResource;

		// TODO: not, this should be done in a different way (i.e. the type should already be assigned from somewhere else and not minted on demand!)
		// addStatement(entityNode, getPredicate(GDMUtil.RDF_type), getType(name + SchemaUtils.TYPE_POSTFIX));

		entityStack.push(new Tuple<>(entityNode, entityPredicate));

		resourceStack.push(entityResource);

	}

	@Override
	public void endEntity() {

		assert !isClosed();

		resourceStack.pop();

		currentResource = resourceStack.peek();

		entityStack.pop();

		if (!entityStack.isEmpty()) {

			entityNode = entityStack.peek().v1();
		} else {

			entityNode = null;
		}
	}

	@Override
	public void literal(final String name, final String value) {

		// System.out.println("in literal with name = '" + name + "' :: value = '" + value + "'");

		assert !isClosed();

		// create triple
		// name = predicate
		// value = literal or object
		// TODO: only literals atm, i.e., how to determine other resources?
		// => still valid: how to determine other resources!
		// ==> @phorn proposed to utilise "<" ">" to identify resource ids (uris)
		if (name == null) {

			return;
		}

		final String propertyUri;

		if (SchemaUtils.isValidUri(name)) {

			propertyUri = name;
		} else {

			propertyUri = SchemaUtils.mintUri(dataModelUri.get(), name);
		}

		if (value != null && !value.isEmpty()) {

			final Predicate attributeProperty = getPredicate(propertyUri);
			final LiteralNode literalObject = new LiteralNode(value);

			final Node currentNode = entityStack.empty() ? recordNode : entityNode;

			if (null != currentNode) {

				// TODO: this is only a HOTFIX for creating resources from resource type uris

				if (!GDMUtil.RDF_type.equals(propertyUri)) {

					currentResource.addStatement(currentNode, attributeProperty, literalObject);
				} else {

					// check, whether value is really a URI
					if (SchemaUtils.isValidUri(value)) {

						final ResourceNode typeResource = new ResourceNode(value);// ResourceFactory.createResource(value);

						currentResource.addStatement(currentNode, attributeProperty, typeResource);

						if (currentResource.equals(recordResource)) {

							recordType = typeResource;
						}
					} else {

						currentResource.addStatement(currentNode, attributeProperty, literalObject);
					}
				}
			} else {

				throw new MetafactureException("couldn't get a resource for adding this property");
			}
		}

	}

	private Optional<String> init(final Optional<DataModel> dataModel) {

		if (!dataModel.isPresent()) {

			return Optional.fromNullable(StringUtils.stripEnd(DataModelUtils.determineDataModelSchemaBaseURI(null), SchemaUtils.HASH));
		}

		return dataModel.transform(new Function<DataModel, String>() {

			@Override
			public String apply(final DataModel dm) {
				return StringUtils.stripEnd(DataModelUtils.determineDataModelSchemaBaseURI(dm), SchemaUtils.HASH);
			}
		});
	}

	private String mintEntityUri() {

		return RESOURCE_BASE_URI + UUID.randomUUID().toString();
	}

	private Predicate getPredicate(final String predicateId) {

		final String predicateURI = getURI(predicateId);

		if (!predicates.containsKey(predicateURI)) {

			final Predicate predicate = new Predicate(predicateURI);

			predicates.put(predicateURI, predicate);
		}

		return predicates.get(predicateURI);
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

		currentResource.addStatement(subject, predicate, object, order);
	}

	private String getURI(final String id) {

		if (!uris.containsKey(id)) {

			final String uri = SchemaUtils.isValidUri(id) ? id : SchemaUtils.mintTermUri(null, id, dataModelUri);

			uris.put(id, uri);
		}

		return uris.get(id);
	}

	private ResourceNode getType(final String typeId) {

		final String typeURI = getURI(typeId);

		if (!types.containsKey(typeURI)) {

			final ResourceNode type = new ResourceNode(typeURI);

			types.put(typeURI, type);
		}

		return types.get(typeURI);
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

}
