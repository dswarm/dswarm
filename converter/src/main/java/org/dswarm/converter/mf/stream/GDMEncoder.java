package org.dswarm.converter.mf.stream;

import java.net.URI;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
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

	private String							currentId;
	private final Model						internalGDMModel;
	private Resource						recordResource;
	private ResourceNode					recordNode;
	// private Stack<Tuple<Node, Predicate>> entityStack;
	// private final Stack<String> elementURIStack; // TODO use stack when statements for deeper hierarchy levels are possible

	// not used: private ResourceNode recordType;

	private final Optional<DataModel>		dataModel;
	private final Optional<String>			dataModelUri;

	private String							recordTypeUri;

	private final Map<String, Predicate>	predicates		= Maps.newHashMap();
	private final Map<String, AtomicLong>	valueCounter	= Maps.newHashMap();
	private final Map<String, String>		uris			= Maps.newHashMap();

	public GDMEncoder(final Optional<DataModel> dataModel) {

		super();

		this.dataModel = dataModel;
		dataModelUri = init(dataModel);

		// init
		// elementURIStack = new Stack<>(); // TODO use stack when statements for deeper hierarchy levels are possible
		internalGDMModel = new Model();

	}

	@Override
	public void startRecord(final String identifier) {

		// System.out.println("in start record with: identifier = '" + identifier + "'");

		assert !isClosed();

		currentId = isValidUri(identifier) ? identifier : mintRecordUri(identifier);

		recordResource = new Resource(currentId);
		recordNode = new ResourceNode(currentId);

	}

	@Override
	public void endRecord() {

		assert !isClosed();

		internalGDMModel.addResource(recordResource);

		// write triples
		final GDMModel gdmModel;

		if (recordTypeUri == null) {

			gdmModel = new GDMModel(internalGDMModel, currentId);
		} else {

			gdmModel = new GDMModel(internalGDMModel, currentId, recordTypeUri);
		}

		getReceiver().process(gdmModel);
	}

	@Override
	public void startEntity(final String name) {

		// System.out.println("in start entity with name = '" + name + "'");

		assert !isClosed();

	}

	@Override
	public void endEntity() {

		// System.out.println("in end entity");

		assert !isClosed();

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

		if (isValidUri(name)) {

			propertyUri = name;
		} else {

			propertyUri = mintUri(dataModelUri.get(), name);
		}

		if (value != null && !value.isEmpty()) {

			final Predicate attributeProperty = getPredicate(propertyUri);
			final LiteralNode literalObject = new LiteralNode(value);

			if (null != recordResource) {

				// TODO: this is only a HOTFIX for creating resources from resource type uris

				if (!GDMUtil.RDF_type.equals(propertyUri)) {

					// recordResource.addProperty(attributeProperty, value);
					recordResource.addStatement(recordNode, attributeProperty, literalObject);
				} else {

					// check, whether value is really a URI
					if (isValidUri(value)) {

						final ResourceNode typeResource = new ResourceNode(value);// ResourceFactory.createResource(value);

						// recordResource.addStatement(entityNode, attributeProperty, typeResource);
						addStatement(recordNode, attributeProperty, typeResource);

						recordTypeUri = value;
					} else {

						// recordResource.addStatement(entityNode, attributeProperty, literalObject);
						addStatement(recordNode, attributeProperty, literalObject);
					}
				}
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

				final URI uri = URI.create(identifier);

				return uri != null && uri.getScheme() != null;
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
