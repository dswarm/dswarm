package de.avgl.dmp.converter.mf.stream;

import java.net.URI;
import java.util.UUID;

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
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

import de.avgl.dmp.persistence.model.internal.rdf.RDFModel;
import de.avgl.dmp.persistence.model.resource.DataModel;
import de.avgl.dmp.persistence.model.resource.utils.DataModelUtils;

/**
 * Converts records to RDF triples.
 * 
 * @author tgaengler
 * @author phorn
 */
@Description("triplifies records")
@In(StreamReceiver.class)
@Out(RDFModel.class)
public final class TripleEncoder extends DefaultStreamPipe<ObjectReceiver<RDFModel>> {

	private String						currentId;
	private Model						model;
	private Resource					recordResource;

	private final Optional<DataModel>	dataModel;
	private final Optional<String>		dataModelUri;

	public TripleEncoder(final Optional<DataModel> dataModel) {

		super();

		this.dataModel = dataModel;
		this.dataModelUri = init(dataModel);

	}

	@Override
	public void startRecord(final String identifier) {

		// System.out.println("in start record with: identifier = '" + identifier + "'");

		assert !isClosed();

		model = ModelFactory.createDefaultModel();

		currentId = isValidUri(identifier) ? identifier : mintRecordUri(identifier);

		recordResource = model.createResource(currentId);
	}

	@Override
	public void endRecord() {

		// System.out.println("in end record");

		assert !isClosed();

		// write triples
		final RDFModel rdfModel = new RDFModel(model, currentId);

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

		getReceiver().process(rdfModel);
	}

	@Override
	public void startEntity(final String name) {

		// System.out.println("in start entity with name = '" + name + "'");

		assert !isClosed();

		// not really needed, because it shouldn't be hit

		// final Property entityProperty = model.createProperty(name);

		// System.out.println("in start entity with entity stact size: '" + entityStack.size() + "'");
	}

	@Override
	public void endEntity() {

		// System.out.println("in end entity");

		assert !isClosed();

		// not really needed, because it shouldn't be hit

		// recordResource.addProperty(entityTuple.v2(), entityTuple.v1());
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

			final Property attributeProperty = model.createProperty(propertyUri);

			if (null != recordResource) {

				recordResource.addProperty(attributeProperty, value);
			} else {

				throw new MetafactureException("couldn't get a resource for adding this property");
			}
		}
	}

	private Optional<String> init(final Optional<DataModel> dataModel) {
		
		if(!dataModel.isPresent()) {
			
			return Optional.fromNullable(StringUtils.stripEnd(DataModelUtils.determineDataModelSchemaBaseURI(null), "#"));
		}

		return dataModel.transform(new Function<DataModel, String>() {

			@Override
			public String apply(final DataModel dm) {
				return StringUtils.stripEnd(DataModelUtils.determineDataModelSchemaBaseURI(dm), "#");
			}
		});
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
}
