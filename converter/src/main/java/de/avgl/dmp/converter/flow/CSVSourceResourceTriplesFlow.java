package de.avgl.dmp.converter.flow;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import de.avgl.dmp.converter.DMPConverterException;
import de.avgl.dmp.converter.mf.stream.reader.CsvReader;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.utils.ConfigurationStatics;
import org.culturegraph.mf.framework.DefaultObjectPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.stream.converter.StreamToTriples;
import org.culturegraph.mf.stream.source.FileOpener;
import org.culturegraph.mf.types.Triple;

import java.io.IOException;
import java.io.Reader;

public class CSVSourceResourceTriplesFlow {

	private static final org.apache.log4j.Logger	LOG						= org.apache.log4j.Logger.getLogger(CSVSourceResourceTriplesFlow.class);

	private final String							encoding;

	private final Character							escapeCharacter;

	private final Character							quoteCharacter;

	private final Character							columnDelimiter;

	private final String							rowDelimiter;

	private static final String						defaultEncoding			= Charsets.UTF_8.name();

	private static final Character					defaultEscapeCharacter	= '\\';

	private static final Character					defaultQuoteCharacter	= '"';

	private static final Character					defaultColumnDelimiter	= ';';

	private static final String						defaultRowDelimiter		= "\n";

	public CSVSourceResourceTriplesFlow(final String encoding, final Character escapeCharacter, final Character quoteCharacter,
										final Character columnDelimiter, final String rowDelimiter) {

		this.encoding = encoding;
		this.escapeCharacter = escapeCharacter;
		this.quoteCharacter = quoteCharacter;
		this.columnDelimiter = columnDelimiter;
		this.rowDelimiter = rowDelimiter;
	}

	public CSVSourceResourceTriplesFlow(final Configuration configuration) throws DMPConverterException {

		if (configuration == null) {

			throw new DMPConverterException("the configuration shouldn't be null");
		}

		if (configuration.getParameters() == null) {

			throw new DMPConverterException("the configuration parameters shouldn't be null");
		}

		final JsonNode encodingNode = getParameterValue(configuration, ConfigurationStatics.ENCODING);

		if (encodingNode != null) {

			encoding = encodingNode.asText();
		} else {

			encoding = defaultEncoding;
		}

		final JsonNode escapeCharacterNode = getParameterValue(configuration, ConfigurationStatics.ESCAPE_CHARACTER);

		if (escapeCharacterNode != null) {

			escapeCharacter = Character.valueOf(escapeCharacterNode.asText().charAt(0));
		} else {

			escapeCharacter = defaultEscapeCharacter;
		}

		final JsonNode quoteCharacterNode = getParameterValue(configuration, ConfigurationStatics.QUOTE_CHARACTER);

		if (quoteCharacterNode != null) {

			quoteCharacter = Character.valueOf(quoteCharacterNode.asText().charAt(0));
		} else {

			quoteCharacter = defaultQuoteCharacter;
		}

		final JsonNode columnDelimiterNode = getParameterValue(configuration, ConfigurationStatics.COLUMN_DELIMITER);

		if (columnDelimiterNode != null) {

			columnDelimiter = Character.valueOf(columnDelimiterNode.asText().charAt(0));
		} else {

			columnDelimiter = defaultColumnDelimiter;
		}

		final JsonNode rowDelimiterNode = getParameterValue(configuration, ConfigurationStatics.ROW_DELIMITER);

		if (rowDelimiterNode != null) {

			rowDelimiter = rowDelimiterNode.asText();
		} else {

			rowDelimiter = defaultRowDelimiter;
		}
	}

	public ImmutableList<Triple> applyFile(final String filePath) {

		FileOpener opener = new FileOpener();

		// set encoding
		final String finalEncoding;

		if (encoding != null) {

			finalEncoding = encoding;
		} else {

			finalEncoding = defaultEncoding;
		}

		opener.setEncoding(finalEncoding);

		return apply(filePath, opener);
	}

	public ImmutableList<Triple> apply(final String filePath, final DefaultObjectPipe<String, ObjectReceiver<Reader>> opener) {

		// set parsing attributes
		final CsvReader reader;

		reader = new CsvReader(escapeCharacter, quoteCharacter, columnDelimiter, rowDelimiter);

		// TODO: process header from configuration
		reader.setHeader(true);
		// TODO: process header from configuration

		class ListTripleReceiver implements ObjectReceiver<Triple> {
			ImmutableList.Builder<Triple> builder = ImmutableList.builder();
			ImmutableList<Triple> collection = null;

			@Override
			public void process(Triple obj) {
				builder.add(obj);
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


		ListTripleReceiver tripleReceiver = new ListTripleReceiver();

		opener.setReceiver(reader).setReceiver(new StreamToTriples()).setReceiver(tripleReceiver);
		opener.process(filePath);

		return tripleReceiver.getCollection();
	}

	public static CSVSourceResourceTriplesFlow fromConfiguration(final Configuration configuration) throws IOException, DMPConverterException {

		return new CSVSourceResourceTriplesFlow(configuration);
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
