package de.avgl.dmp.converter.flow;

import java.io.Reader;

import org.culturegraph.mf.framework.DefaultObjectPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.stream.converter.xml.XmlDecoder;
import org.culturegraph.mf.stream.source.ResourceOpener;
import org.culturegraph.mf.stream.source.StringReader;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import de.avgl.dmp.converter.DMPConverterException;
import de.avgl.dmp.converter.mf.stream.source.XMLTripleEncoder;
import de.avgl.dmp.persistence.model.resource.Configuration;

public class XMLSourceResourceTriplesFlow {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(XMLSourceResourceTriplesFlow.class);

	final Optional<String>							recordTagName;
	final Optional<String>							xmlNameSpace;

	public XMLSourceResourceTriplesFlow(final Configuration configuration) throws DMPConverterException {

		if (configuration == null) {

			throw new DMPConverterException("the configuration shouldn't be null");
		}

		if (configuration.getParameters() == null) {

			throw new DMPConverterException("the configuration parameters shouldn't be null");
		}

		recordTagName = getStringParameter(configuration, "record_tag");
		xmlNameSpace = getStringParameter(configuration, "xml_namespace");
	}

	public ImmutableList<Triple> applyRecord(final String record) {

		final StringReader opener = new StringReader();

		return apply(record, opener);
	}

	public ImmutableList<Triple> applyResource(final String resourcePath) {

		final ResourceOpener opener = new ResourceOpener();

		return apply(resourcePath, opener);
	}

	public ImmutableList<Triple> apply(final String object, final DefaultObjectPipe<String, ObjectReceiver<Reader>> opener) {

		final XmlDecoder decoder = new XmlDecoder();

		final XMLTripleEncoder encoder;

		if (recordTagName.isPresent() && xmlNameSpace.isPresent()) {

			encoder = new XMLTripleEncoder(recordTagName.get(), xmlNameSpace.get());
		} else {

			encoder = new XMLTripleEncoder();
		}
		final ListModelReceiver writer = new ListModelReceiver();

		opener.setReceiver(decoder).setReceiver(encoder).setReceiver(writer);

		opener.process(object);

		return writer.getCollection();
	}

	private Optional<String> getStringParameter(final Configuration configuration, final String key) throws DMPConverterException {
		final JsonNode jsonNode = getParameterValue(configuration, key);
		if (jsonNode == null) {
			return Optional.absent();
		}

		return Optional.of(jsonNode.asText());
	}

	private JsonNode getParameterValue(final Configuration configuration, final String key) throws DMPConverterException {

		if (key == null) {

			throw new DMPConverterException("the parameter key shouldn't be null");
		}

		final JsonNode valueNode = configuration.getParameter(key);

		if (valueNode == null) {

			LOG.debug("couldn't find value for parameter '" + key + "'; try to utilise default value for this parameter");
		}

		return valueNode;
	}

	private static class ListModelReceiver implements ObjectReceiver<Model> {

		private ImmutableList.Builder<Triple>	builder		= ImmutableList.builder();
		private ImmutableList<Triple>			collection	= null;

		@Override
		public void process(final Model model) {

			final StmtIterator iter = model.listStatements();

			while (iter.hasNext()) {

				builder.add(iter.next().asTriple());
			}
		}

		@Override
		public void resetStream() {
			builder = ImmutableList.builder();
		}

		@Override
		public void closeStream() {
			buildCollection();
		}

		public ImmutableList<Triple> getCollection() {
			if (collection == null) {
				buildCollection();
			}
			return collection;
		}

		private void buildCollection() {
			collection = builder.build();
		}
	}
}
