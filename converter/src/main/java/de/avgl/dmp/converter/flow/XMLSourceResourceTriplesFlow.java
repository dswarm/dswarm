package de.avgl.dmp.converter.flow;

import java.io.Reader;
import java.util.List;

import org.culturegraph.mf.framework.DefaultObjectPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.stream.converter.xml.XmlDecoder;
import org.culturegraph.mf.stream.source.StringReader;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import de.avgl.dmp.converter.DMPConverterException;
import de.avgl.dmp.converter.mf.stream.source.BOMResourceOpener;
import de.avgl.dmp.converter.mf.stream.source.XMLTripleEncoder;
import de.avgl.dmp.persistence.model.internal.impl.RDFModel;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.DataModel;

/**
 * Flow that transforms a given XML source into RDF triples.
 * 
 * @author tgaengler
 *
 */
public class XMLSourceResourceTriplesFlow {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(XMLSourceResourceTriplesFlow.class);

	private final Optional<String>					recordTagName;
	private final Optional<String>					dataModelId;

	public XMLSourceResourceTriplesFlow(final DataModel dataModel) throws DMPConverterException {

		if (dataModel != null && dataModel.getId() != null) {

			this.dataModelId = Optional.of(dataModel.getId().toString());
		} else {

			this.dataModelId = Optional.absent();
		}

		if (dataModel == null) {

			throw new DMPConverterException("the data model shouldn't be null");
		}

		if (dataModel.getConfiguration() == null) {

			throw new DMPConverterException("the data model configuration shouldn't be null");
		}

		if (dataModel.getConfiguration().getParameters() == null) {

			throw new DMPConverterException("the data model configuration parameters shouldn't be null");
		}

		recordTagName = getStringParameter(dataModel.getConfiguration(), "record_tag");
	}

	public List<RDFModel> applyRecord(final String record) {

		final StringReader opener = new StringReader();

		return apply(record, opener);
	}

	public List<RDFModel> applyResource(final String resourcePath) {

		final BOMResourceOpener opener = new BOMResourceOpener();

		return apply(resourcePath, opener);
	}

	List<RDFModel> apply(final String object, final DefaultObjectPipe<String, ObjectReceiver<Reader>> opener) {

		final XmlDecoder decoder = new XmlDecoder();

		final XMLTripleEncoder encoder;

		if (recordTagName.isPresent()) {

			encoder = new XMLTripleEncoder(recordTagName.get(), dataModelId);
		} else {

			encoder = new XMLTripleEncoder(dataModelId);
		}
		final RDFModelReceiver writer = new RDFModelReceiver();

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

	private static class RDFModelReceiver implements ObjectReceiver<RDFModel> {

		private ImmutableList.Builder<RDFModel>	builder	= ImmutableList.builder();
		private ImmutableList<RDFModel>			collection;

		@Override
		public void process(final RDFModel rdfModel) {

			builder.add(rdfModel);
		}

		@Override
		public void resetStream() {

			builder = ImmutableList.builder();
		}

		@Override
		public void closeStream() {

			buildCollection();
		}

		public ImmutableList<RDFModel> getCollection() {

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
