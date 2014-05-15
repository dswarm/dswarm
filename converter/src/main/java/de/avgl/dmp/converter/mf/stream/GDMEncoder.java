package de.avgl.dmp.converter.mf.stream;

import java.net.URI;
import java.util.Map;
import java.util.Stack;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.culturegraph.mf.exceptions.MetafactureException;
import org.culturegraph.mf.framework.DefaultStreamPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;

import com.google.common.base.Function;
import com.google.common.base.Optional;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.hp.hpl.jena.vocabulary.RDF;

import de.avgl.dmp.graph.json.LiteralNode;
import de.avgl.dmp.graph.json.Model;
import de.avgl.dmp.graph.json.Node;
import de.avgl.dmp.graph.json.Predicate;
import de.avgl.dmp.graph.json.Resource;
import de.avgl.dmp.graph.json.ResourceNode;
import de.avgl.dmp.persistence.model.internal.gdm.GDMModel;
import de.avgl.dmp.persistence.model.resource.DataModel;
import de.avgl.dmp.persistence.model.resource.utils.DataModelUtils;
import de.avgl.dmp.persistence.model.types.Tuple;

/**
 * Converts records to RDF triples.
 * 
 * @author polowins
 * @author tgaengler
 * @author phorn
 */
@Description("converts records into GDM-JSON")
@In(StreamReceiver.class)
@Out(GDMModel.class)
public final class GDMEncoder extends DefaultStreamPipe<ObjectReceiver<GDMModel>> {

	private String							currentId;
	private final Model						internalGDMModel;
	private Resource						recordResource;
	private ResourceNode					recordNode;
	private Node							entityNode;
	private Stack<Tuple<Node, Predicate>>	entityStack;
	private final Stack<String>				elementURIStack;

	private static final Pattern			TABS			= Pattern.compile("\t+");
	
	
	private boolean							inRecord;
	private StringBuilder					valueBuffer		= new StringBuilder();
	private String							uri;
	private ResourceNode					recordType;

	private final Optional<DataModel>		dataModel;
	private final Optional<String>			dataModelUri;


	/**
	 * record tag URI should be unique
	 */
	private String							initialRecordTypeUri		= "testUri_to_be_replaced_in_GDM_ENCODER";
	
	private long							nodeIdCounter	= 1;
	private final Predicate					rdfType			= new Predicate(RDF.type.getURI());
	private final Map<String, Predicate>	predicates		= Maps.newHashMap();
	private final Map<String, ResourceNode>	types			= Maps.newHashMap();
	private final Map<String, AtomicLong>	valueCounter	= Maps.newHashMap();
	private final Map<String, String>		uris			= Maps.newHashMap();

	public GDMEncoder(final Optional<DataModel> dataModel) {

		super();
		
		this.dataModel = dataModel;
		dataModelUri = init(dataModel);
		
		// init
		elementURIStack = new Stack<>();
		internalGDMModel = new Model();

	}

	@Override
	public void startRecord(final String identifier) {

		// System.out.println("in start record with: identifier = '" + identifier + "'");

		assert !isClosed();

		currentId = isValidUri(identifier) ? identifier : mintRecordUri(identifier);

		recordResource = new Resource(currentId);
		recordNode = new ResourceNode(currentId);
		
		// init
		entityStack = new Stack<>();
		
		// TODO: determine record type and create type triple with it
		if (recordType == null) {

			final String recordTypeUri = initialRecordTypeUri + "Type";

			recordType = getType(recordTypeUri);
		}
		
		
		addStatement(recordNode, rdfType, recordType);
	}

	@Override
	public void endRecord() {

		assert !isClosed();
		
		inRecord = false;

		internalGDMModel.addResource(recordResource);

		// write triples
		final GDMModel gdmModel;

		if (recordType.getUri() == null) {

			gdmModel = new GDMModel(internalGDMModel, currentId);
		} else {

			gdmModel = new GDMModel(internalGDMModel, currentId, recordType.getUri());
		}

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
		final ResourceNode entityType = getType(name + "Type");

		addStatement(entityNode, rdfType, entityType);

		entityStack.push(new Tuple<>(entityNode, entityPredicate));

		// System.out.println("in start entity with entity stact size: '" + entityStack.size() + "'");
	}

	@Override
	public void endEntity() {

		// System.out.println("in end entity");

		assert !isClosed();

		// write sub resource
		final Tuple<Node, Predicate> entityTuple = entityStack.pop();

		// System.out.println("in end entity with entity stact size: '" + entityStack.size() + "'");

		// add entity resource to parent entity resource (or to record resource, if there is no parent entity)
		if (!entityStack.isEmpty()) {

			entityNode = entityStack.peek().v1();

			final Tuple<Node, Predicate> parentEntityTuple = entityStack.peek();

			// addStatement(parentEntityTuple.v1(), entityTuple.v2(), entityTuple.v1());
		} else {

			entityNode = null;

			// addStatement(recordNode, entityTuple.v2(), entityTuple.v1());
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

	private Optional<String> init(final Optional<DataModel> dataModel) {

		if (!dataModel.isPresent()) {

			return Optional.fromNullable(StringUtils.stripEnd(DataModelUtils.determineDataModelSchemaBaseURI(null), "#"));
		}

		return dataModel.transform(new Function<DataModel, String>() {

			@Override
			public String apply(final DataModel dm) {
				return StringUtils.stripEnd(DataModelUtils.determineDataModelSchemaBaseURI(dm), "#");
			}
		});
	}
	
	private String mintDataModelTermUri(@Nullable final String uri, @Nullable final String localName) {

		final boolean canUseLocalName = !Strings.isNullOrEmpty(localName);

		if (Strings.isNullOrEmpty(uri)) {

			if (dataModelUri.isPresent()) {

				if (canUseLocalName) {

					return mintUri(dataModelUri.get(), localName);
				} else {

					return dataModelUri.get() + "#" + UUID.randomUUID();
				}
			}

			return String.format("http://data.slub-dresden.de/terms/%s", UUID.randomUUID());
		}

		if (canUseLocalName) {

			return mintUri(uri, localName);
		} else {

			return String.format("http://data.slub-dresden.de/terms/%s", UUID.randomUUID());
		}
	}

	private boolean isValidUri(@Nullable final String identifier) {

		if (identifier != null) {

			try {

				final URI _uri = URI.create(identifier);

				return _uri != null && _uri.getScheme() != null;
			} catch (final IllegalArgumentException e) {

				return false;
			}
		}

		return false;
	}

	private String mintRecordUri(@Nullable final String identifier) {

		if (currentId == null) {

			// mint completely new uri

			final StringBuilder sb = new StringBuilder();

			if (dataModel.isPresent()) {

				// create uri from resource id and configuration id and random uuid

				sb.append("http://data.slub-dresden.de/datamodels/").append(dataModel.get().getId()).append("/records/");
			} else {

				// create uri from random uuid

				sb.append("http://data.slub-dresden.de/records/");
			}

			return sb.append(UUID.randomUUID()).toString();
		}

		// create uri with help of given record id

		final StringBuilder sb = new StringBuilder();

		if (dataModel.isPresent()) {

			// create uri from resource id and configuration id and identifier

			sb.append("http://data.slub-dresden.de/datamodels/").append(dataModel.get().getId()).append("/records/").append(identifier);
		} else {

			// create uri from identifier

			sb.append("http://data.slub-dresden.de/records/").append(identifier);
		}

		return sb.toString();
	}

	private String mintUri(final String uri, final String localName) {

		// allow has and slash uris
		if (uri != null && uri.endsWith("/")) {

			return uri + localName;
		}

		return uri + "#" + localName;
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

			final String uri = isValidUri(id) ? id : mintDataModelTermUri(null, id);

			uris.put(id, uri);
		}

		return uris.get(id);
	}
}
