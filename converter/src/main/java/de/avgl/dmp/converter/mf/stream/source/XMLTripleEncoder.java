package de.avgl.dmp.converter.mf.stream.source;

import java.net.URI;
import java.util.Stack;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

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

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

import de.avgl.dmp.persistence.model.internal.rdf.RDFModel;
import de.avgl.dmp.persistence.model.resource.DataModel;
import de.avgl.dmp.persistence.model.resource.utils.DataModelUtils;
import de.avgl.dmp.persistence.model.types.Tuple;

/**
 * Converts XML records to RDF triples.
 * 
 * @author tgaengler
 * @author phorn
 */
@Description("triplifies records")
@In(XmlReceiver.class)
@Out(RDFModel.class)
public final class XMLTripleEncoder extends DefaultXmlPipe<ObjectReceiver<RDFModel>> {

	private String								currentId;
	private Model								model;
	private Resource							recordResource;
	private Resource							entityResource;
	private Stack<Tuple<Resource, Property>>	entityStack;
	private final Stack<String>					elementURIStack;

	private static final Pattern				TABS			= Pattern.compile("\t+");

	/**
	 * note: recordTagName is not biunique, i.e., the record tag name can occur in different name spaces; hence, a record tag
	 * uniqueness is only give by a complete uri
	 */
	private final String						recordTagName;

	/**
	 * record tag URI should be unique
	 */
	private String								recordTagUri	= null;

	private boolean								inRecord;
	private StringBuilder						valueBuffer		= new StringBuilder();
	private String								uri;
	private Resource							recordType;

	private final Optional<DataModel>			dataModel;
	private final Optional<String>				dataModelUri;

	public XMLTripleEncoder(final Optional<DataModel> dataModel) {
		super();

		recordTagName = System.getProperty("org.culturegraph.metamorph.xml.recordtag");
		if (recordTagName == null) {
			throw new MetafactureException("Missing name for the tag marking a record.");
		}

		this.dataModel = dataModel;
		dataModelUri = init(dataModel);

		// init
		elementURIStack = new Stack<>();
	}

	public XMLTripleEncoder(final String recordTagName, final Optional<DataModel> dataModel) {
		super();
		this.recordTagName = Preconditions.checkNotNull(recordTagName);

		this.dataModel = dataModel;
		dataModelUri = init(dataModel);

		// init
		elementURIStack = new Stack<>();
	}

	@Override
	public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {

		this.uri = mintDataModelUri(uri);

		elementURIStack.push(this.uri);

		if (inRecord) {
			writeValue();
			startEntity(mintUri(uri, localName));
			writeAttributes(attributes);
		} else if (localName.equals(recordTagName)) {

			if (recordTagUri == null) {

				recordTagUri = mintUri(elementURIStack.peek(), localName);
			}

			if (recordTagUri.equals(mintUri(elementURIStack.peek(), localName))) {

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

			if (recordTagUri.equals(mintUri(elementUri, localName))) {
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
			valueBuffer.append(XMLTripleEncoder.TABS.matcher(new String(chars, start, length)).replaceAll(""));
		}
	}

	private void writeValue() {
		final String value = valueBuffer.toString();
		if (!value.trim().isEmpty()) {
			literal(RDF.value.getURI(), value.replace('\n', ' '));
		}
		valueBuffer = new StringBuilder();
	}

	private void writeAttributes(final Attributes attributes) {
		final int length = attributes.getLength();

		for (int i = 0; i < length; ++i) {
			final String name = mintUri(uri, attributes.getLocalName(i));
			final String value = attributes.getValue(i);
			literal(name, value);
		}
	}

	public void startRecord(final String identifier) {

		// System.out.println("in start record with: identifier = '" + identifier + "'");

		assert !isClosed();

		model = ModelFactory.createDefaultModel();

		currentId = isValidUri(identifier) ? identifier : mintRecordUri(identifier);

		recordResource = model.createResource(currentId);

		// init
		entityStack = new Stack<>();

		// TODO: determine record type and create type triple with it
		if (recordType == null) {

			final String recordTypeUri = recordTagUri + "Type";

			recordType = model.createResource(recordTypeUri);
		}

		recordResource.addProperty(RDF.type, recordType);
	}

	public void endRecord() {

		// System.out.println("in end record");

		assert !isClosed();

		inRecord = false;

		// write triples
		final RDFModel rdfModel = new RDFModel(model, currentId, recordType.getURI());

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

		getReceiver().process(rdfModel);
	}

	@Override
	public void startEntity(final String name) {

		// System.out.println("in start entity with name = '" + name + "'");

		assert !isClosed();

		// bnode or url
		entityResource = model.createResource();
		// sub resource type
		final Resource entityType = model.createResource(name + "Type");

		entityResource.addProperty(RDF.type, entityType);

		final Property entityProperty = model.createProperty(name);

		entityStack.push(new Tuple<>(entityResource, entityProperty));

		// System.out.println("in start entity with entity stact size: '" + entityStack.size() + "'");
	}

	public void endEntity() {

		// System.out.println("in end entity");

		assert !isClosed();

		// write sub resource
		final Tuple<Resource, Property> entityTuple = entityStack.pop();

		// System.out.println("in end entity with entity stact size: '" + entityStack.size() + "'");

		// add entity resource to parent entity resource (or to record resource, if there is no parent entity)
		if (!entityStack.isEmpty()) {

			entityResource = entityStack.peek().v1();

			final Tuple<Resource, Property> parentEntityTuple = entityStack.peek();

			parentEntityTuple.v1().addProperty(entityTuple.v2(), entityTuple.v1());
		} else {

			entityResource = null;

			recordResource.addProperty(entityTuple.v2(), entityTuple.v1());
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
			final Property attributeProperty = model.createProperty(name);

			if (null != entityResource) {

				entityResource.addProperty(attributeProperty, value);
			} else if (null != recordResource) {

				recordResource.addProperty(attributeProperty, value);
			} else {

				throw new MetafactureException("couldn't get a resource for adding this property");
			}
		}
	}

	private Optional<String> init(final Optional<DataModel> dataModel) {
		return dataModel.transform(new Function<DataModel, String>() {

			@Override
			public String apply(final DataModel dm) {
				return StringUtils.stripEnd(DataModelUtils.determineDataModelSchemaBaseURI(dm), "#");
			}
		});
	}

	private String mintDataModelUri(@Nullable final String uri) {
		if (Strings.isNullOrEmpty(uri)) {
			if (dataModelUri.isPresent()) {

				return dataModelUri.get();
			}

			return String.format("http://data.slub-dresden.de/records/%s", UUID.randomUUID());
		}

		return uri;
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
