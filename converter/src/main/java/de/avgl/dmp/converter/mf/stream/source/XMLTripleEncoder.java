package de.avgl.dmp.converter.mf.stream.source;

import java.net.URI;
import java.util.Stack;
import java.util.UUID;
import java.util.regex.Pattern;

import org.culturegraph.mf.exceptions.MetafactureException;
import org.culturegraph.mf.framework.DefaultXmlPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.XmlReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.google.common.base.Optional;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

import de.avgl.dmp.persistence.model.internal.impl.RDFModel;
import de.avgl.dmp.persistence.model.types.Tuple;

/**
 * @author tgaengler
 */
@Description("triplifies records")
@In(XmlReceiver.class)
@Out(RDFModel.class)
public final class XMLTripleEncoder extends DefaultXmlPipe<ObjectReceiver<RDFModel>> {

	private String						currentId;
	private Model						model			= null;
	private Resource					recordResource	= null;
	private Resource					entityResource	= null;
	Stack<Tuple<Resource, Property>>	entityStack		= null;

	private static final Pattern		TABS			= Pattern.compile("\t+");
	private final String				recordTagName;
	private boolean						inRecord;
	private StringBuilder				valueBuffer		= new StringBuilder();
	private String						uri;
	private Resource					recordType		= null;

	private final Optional<String>		resourceId;
	private final Optional<String>		configurationId;

	public XMLTripleEncoder(final Optional<String> resourceId, final Optional<String> configurationId) {
		super();

		this.resourceId = resourceId;
		this.configurationId = configurationId;

		this.recordTagName = System.getProperty("org.culturegraph.metamorph.xml.recordtag");
		if (recordTagName == null) {
			throw new MetafactureException("Missing name for the tag marking a record.");
		}
	}

	public XMLTripleEncoder(final String recordTagName, final Optional<String> resourceId,
							final Optional<String> configurationId) {
		super();
		this.recordTagName = recordTagName;
		this.resourceId = resourceId;
		this.configurationId = configurationId;
	}

	@Override
	public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {

		this.uri = uri;

		if (inRecord) {
			writeValue();
			startEntity(uri + "#" + localName);
			writeAttributes(attributes);
		} else if (localName.equals(recordTagName)) {
			// TODO: how to determine the id of an record, or should we mint uris?
			final String identifier = attributes.getValue("id");
			startRecord(identifier);
			writeAttributes(attributes);
			inRecord = true;
		}
	}

	@Override
	public void endElement(final String uri, final String localName, final String qName) throws SAXException {
		if (inRecord) {
			writeValue();
			if (localName.equals(recordTagName)) {
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
			valueBuffer.append(TABS.matcher(new String(chars, start, length)).replaceAll(""));
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
			final String name = uri + "#" + attributes.getLocalName(i);
			final String value = attributes.getValue(i);
			literal(name, value);
		}
	}

	public void startRecord(final String identifier) {

		assert !isClosed();

		model = ModelFactory.createDefaultModel();

		// determine record id and create uri from it
		if (identifier != null) {

			URI uri = null;

			try {

				uri = URI.create(identifier);
			} catch (final Exception e) {

				e.printStackTrace();
			}

			if (uri != null) {

				// identifier could act as resource uri

				currentId = identifier;
			} else {

				// create uri with help of given record id

				final StringBuilder sb = new StringBuilder();

				if (resourceId.isPresent() && configurationId.isPresent()) {

					// create uri from resource id and configuration id and identifier

					sb.append("http://data.slub-dresden.de/resources/").append(resourceId.get()).append("/configurations/")
							.append(configurationId.get()).append("/records/").append(identifier);
				} else {

					// create uri from identifier

					sb.append("http://data.slub-dresden.de/records/").append(identifier);
				}

				currentId = sb.toString();
			}
		}

		if (currentId == null) {

			// mint completely new uri

			final StringBuilder sb = new StringBuilder();

			if (resourceId.isPresent() && configurationId.isPresent()) {

				// create uri from resource id and configuration id and random uuid

				sb.append("http://data.slub-dresden.de/resources/").append(resourceId.get()).append("/configurations/").append(configurationId.get())
						.append("/records/").append(UUID.randomUUID());
			} else {

				// create uri from random uuid

				sb.append("http://data.slub-dresden.de/records/").append(UUID.randomUUID());
			}

			currentId = sb.toString();
		}

		recordResource = model.createResource(currentId);

		// init
		entityStack = new Stack<Tuple<Resource, Property>>();

		// TODO: determine record type and create type triple with it
		if (recordType == null) {

			final String recordTypeUri = uri + "#" + recordTagName;

			startEntity(recordTypeUri);
			recordType = model.createResource(recordTypeUri + "Type");
		}

//		recordResource.addProperty(RDF.type, recordType);
	}

	public void endRecord() {

		assert !isClosed();

		endEntity();

		// write triples
		getReceiver().process(new RDFModel(model, currentId));
	}

	public void startEntity(final String name) {

		assert !isClosed();

		// bnode or url
		entityResource = model.createResource();
		// sub resource type
		final Resource entityType = model.createResource(name + "Type");
		entityResource.addProperty(RDF.type, entityType);
		final Property entityProperty = model.createProperty(name);
		entityStack.push(new Tuple<Resource, Property>(entityResource, entityProperty));
	}

	public void endEntity() {

		assert !isClosed();

		// write sub resource
		final Tuple<Resource, Property> entityTuple = entityStack.pop();

		// add entity resource to parent entity resource (or to record resource, if there is no parent entity)
		if (!entityStack.isEmpty()) {

			final Tuple<Resource, Property> parentEntityTuple = entityStack.peek();

			parentEntityTuple.v1().addProperty(entityTuple.v2(), entityTuple.v1());
		} else {
			recordResource.addProperty(entityTuple.v2(), entityTuple.v1());
		}
	}

	public void literal(final String name, final String value) {

		assert !isClosed();

		// create triple
		// name = predicate (without namespace)
		// value = literal or object
		// TODO: only literals atm, i.e., how to determine other resources?
		if (value != null && !value.trim().isEmpty()) {
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
}
