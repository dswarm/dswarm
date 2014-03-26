package de.avgl.dmp.converter.flow;

import java.io.Reader;
import java.util.List;

import org.culturegraph.mf.framework.DefaultObjectPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.stream.converter.xml.XmlDecoder;
import org.culturegraph.mf.stream.source.StringReader;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Optional;

import de.avgl.dmp.converter.DMPConverterException;
import de.avgl.dmp.converter.mf.stream.RDFModelReceiver;
import de.avgl.dmp.converter.mf.stream.source.BOMResourceOpener;
import de.avgl.dmp.converter.mf.stream.source.XMLTripleEncoder;
import de.avgl.dmp.persistence.model.internal.impl.RDFModel;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.DataModel;
import de.avgl.dmp.persistence.model.resource.utils.ConfigurationStatics;

/**
 * Flow that transforms a given XML source into RDF triples.
 *
 * @author tgaengler
 *
 */
public class XMLSourceResourceTriplesFlow {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(XMLSourceResourceTriplesFlow.class);

	private final Optional<String>					recordTagName;
	private final Optional<DataModel>				dataModel;

	public XMLSourceResourceTriplesFlow(final DataModel dataModel) throws DMPConverterException {

		if (dataModel == null) {

			throw new DMPConverterException("the data model shouldn't be null");
		}

		if (dataModel.getConfiguration() == null) {

			throw new DMPConverterException("the data model configuration shouldn't be null");
		}

		if (dataModel.getConfiguration().getParameters() == null) {

			throw new DMPConverterException("the data model configuration parameters shouldn't be null");
		}

		if (dataModel.getId() != null) {

			this.dataModel = Optional.of(dataModel);
		} else {

			this.dataModel = Optional.absent();
		}

		recordTagName = getStringParameter(dataModel.getConfiguration(), ConfigurationStatics.RECORD_TAG);
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

			encoder = new XMLTripleEncoder(recordTagName.get(), dataModel);
		} else {

			encoder = new XMLTripleEncoder(dataModel);
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
}
