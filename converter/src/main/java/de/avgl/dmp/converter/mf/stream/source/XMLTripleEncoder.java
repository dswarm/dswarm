package de.avgl.dmp.converter.mf.stream.source;

import java.util.Stack;
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

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

import de.avgl.dmp.persistence.model.types.Tuple;

/**
 * @author tgaengler
 */
@Description("triplifies records")
@In(XmlReceiver.class)
@Out(Model.class)
public final class XMLTripleEncoder extends DefaultXmlPipe<ObjectReceiver<Model>> {

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

	public XMLTripleEncoder() {
		super();
		this.recordTagName = System.getProperty("org.culturegraph.metamorph.xml.recordtag");
		if (recordTagName == null) {
			throw new MetafactureException("Missing name for the tag marking a record.");
		}
	}

	public XMLTripleEncoder(final String recordTagName, final String xmlNameSpace) {
		super();
		this.recordTagName = recordTagName;
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

		currentId = identifier;

		// TODO: determine record id and create uri from it
		recordResource = model.createResource(currentId);
		// TODO: determine record type and create type triple with it
		if (recordType == null) {

			recordType = model.createResource(uri + "#" + recordTagName);
		}

		recordResource.addProperty(RDF.type, recordType);

		// init
		entityStack = new Stack<Tuple<Resource, Property>>();
	}

	public void endRecord() {
		assert !isClosed();

		// write triples
		getReceiver().process(model);
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
		// TODO: handle nested entities
		recordResource.addProperty(entityTuple.v2(), entityTuple.v1());
	}

	public void literal(final String name, final String value) {
		assert !isClosed();

		// create triple
		// name = predicate (without namespace)
		// value = literal or object
		// TODO: only literals atm
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
