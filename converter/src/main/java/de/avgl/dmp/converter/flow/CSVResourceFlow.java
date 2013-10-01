package de.avgl.dmp.converter.flow;

import java.io.Reader;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import org.culturegraph.mf.framework.ObjectPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.stream.source.FileOpener;

import de.avgl.dmp.converter.DMPConverterException;
import de.avgl.dmp.converter.mf.stream.reader.CsvReader;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.utils.ConfigurationStatics;

public abstract class CSVResourceFlow<T> {

	private static final org.apache.log4j.Logger	LOG						= org.apache.log4j.Logger.getLogger(CSVResourceFlow.class);

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

	private Optional<String> getStringParameter(final Configuration configuration, final String key) throws DMPConverterException {
		final JsonNode jsonNode = getParameterValue(configuration, key);
		if (jsonNode == null) {
			return Optional.absent();
		}

		return Optional.of(jsonNode.asText());
	}

	private Optional<Character> getCharParameter(final Configuration configuration, final String key) throws DMPConverterException {
		final JsonNode jsonNode = getParameterValue(configuration, key);
		if (jsonNode == null) {
			return Optional.absent();
		}

		return Optional.of(jsonNode.asText().charAt(0));
	}

	protected CSVResourceFlow() {
		this.encoding = defaultEncoding;
		this.escapeCharacter = defaultEscapeCharacter;
		this.quoteCharacter = defaultQuoteCharacter;
		this.columnDelimiter = defaultColumnDelimiter;
		this.rowDelimiter = defaultRowDelimiter;
	}

	public CSVResourceFlow(final String encoding, final Character escapeCharacter, final Character quoteCharacter,
						   final Character columnDelimiter, final String rowDelimiter) {

		this.encoding = encoding;
		this.escapeCharacter = escapeCharacter;
		this.quoteCharacter = quoteCharacter;
		this.columnDelimiter = columnDelimiter;
		this.rowDelimiter = rowDelimiter;
	}

	public CSVResourceFlow(final Configuration configuration) throws DMPConverterException {

		if (configuration == null) {

			throw new DMPConverterException("the configuration shouldn't be null");
		}

		if (configuration.getParameters() == null) {

			throw new DMPConverterException("the configuration parameters shouldn't be null");
		}

		final Optional<String> encodingOptional = getStringParameter(configuration, ConfigurationStatics.ENCODING);
		final Optional<Character> escapeCharacterOptional = getCharParameter(configuration, ConfigurationStatics.ESCAPE_CHARACTER);
		final Optional<Character> quoteCharacterOptional = getCharParameter(configuration, ConfigurationStatics.QUOTE_CHARACTER);
		final Optional<Character> columnDelimiterOptional = getCharParameter(configuration, ConfigurationStatics.COLUMN_DELIMITER);
		final Optional<String> rowDelimiterOptional = getStringParameter(configuration, ConfigurationStatics.ROW_DELIMITER);

		this.encoding = encodingOptional.or(defaultEncoding);
		this.escapeCharacter = escapeCharacterOptional.or(defaultEscapeCharacter);
		this.quoteCharacter = quoteCharacterOptional.or(defaultQuoteCharacter);
		this.columnDelimiter = columnDelimiterOptional.or(defaultColumnDelimiter);
		this.rowDelimiter = rowDelimiterOptional.or(defaultRowDelimiter);
	}

	public T applyFile(final String filePath) {

		FileOpener opener = new FileOpener();

		// set encoding
		final String finalEncoding = encoding != null ? encoding : defaultEncoding;
		opener.setEncoding(finalEncoding);

		return apply(filePath, opener);
	}

	public T apply(final String obj, final ObjectPipe<String, ObjectReceiver<Reader>> opener) {

		// set parsing attributes
		final CsvReader reader = new CsvReader(escapeCharacter, quoteCharacter, columnDelimiter, rowDelimiter);

		// TODO: process header from configuration
		reader.setHeader(true);


		CsvReader pipe = opener.setReceiver(reader);

		return process(opener, obj, pipe);
	}

	protected abstract T process(ObjectPipe<String, ObjectReceiver<Reader>> opener, String obj, CsvReader pipe);
}
