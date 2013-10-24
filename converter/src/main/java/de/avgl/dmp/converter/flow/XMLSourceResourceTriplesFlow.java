package de.avgl.dmp.converter.flow;

import java.io.Reader;

import org.culturegraph.mf.framework.DefaultObjectPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.stream.converter.xml.XmlDecoder;
import org.culturegraph.mf.stream.source.ResourceOpener;
import org.culturegraph.mf.stream.source.StringReader;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Optional;

import de.avgl.dmp.converter.DMPConverterException;
import de.avgl.dmp.converter.mf.stream.source.XMLTripleEncoder;
import de.avgl.dmp.persistence.model.internal.impl.RDFModel;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.Resource;

public class XMLSourceResourceTriplesFlow {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(XMLSourceResourceTriplesFlow.class);

	final Optional<String>							recordTagName;
	final Optional<String>							xmlNameSpace;
	final Optional<String> configurationId;
	final Optional<String> resourceId;

	public XMLSourceResourceTriplesFlow(final Configuration configuration, final Resource resource) throws DMPConverterException {

		if (configuration == null) {

			throw new DMPConverterException("the configuration shouldn't be null");
		}

		if (configuration.getParameters() == null) {

			throw new DMPConverterException("the configuration parameters shouldn't be null");
		}
		
		if(configuration.getId() != null) {
			
			configurationId = Optional.of(configuration.getId().toString());
		} else {
			
			configurationId = Optional.absent();
		}
		
		if(resource != null && resource.getId() != null) {
			
			this.resourceId = Optional.of(resource.getId().toString());
		} else {
			
			this.resourceId = Optional.absent();
		}

		recordTagName = getStringParameter(configuration, "record_tag");
		xmlNameSpace = getStringParameter(configuration, "xml_namespace");
	}

	public RDFModel applyRecord(final String record) {

		final StringReader opener = new StringReader();

		return apply(record, opener);
	}

	public RDFModel applyResource(final String resourcePath) {

		final ResourceOpener opener = new ResourceOpener();

		return apply(resourcePath, opener);
	}

	public RDFModel apply(final String object, final DefaultObjectPipe<String, ObjectReceiver<Reader>> opener) {

		final XmlDecoder decoder = new XmlDecoder();

		final XMLTripleEncoder encoder;

		if (recordTagName.isPresent() && xmlNameSpace.isPresent()) {

			encoder = new XMLTripleEncoder(recordTagName.get(), xmlNameSpace.get(), configurationId, resourceId);
		} else {

			encoder = new XMLTripleEncoder(configurationId, resourceId);
		}
		final RDFModelReceiver writer = new RDFModelReceiver();

		opener.setReceiver(decoder).setReceiver(encoder).setReceiver(writer);

		opener.process(object);

		return writer.buildRDFModel();
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

	private static class RDFModelReceiver implements ObjectReceiver<RDFModel> {

		private RDFModel			rdfModel	= null;

		@Override
		public void process(final RDFModel rdfModel) {

			this.rdfModel = rdfModel;
		}

		@Override
		public void resetStream() {
			rdfModel = null;
		}

		@Override
		public void closeStream() {
			buildRDFModel();
		}

		private RDFModel buildRDFModel() {
			
			return rdfModel;
		}
	}
}
