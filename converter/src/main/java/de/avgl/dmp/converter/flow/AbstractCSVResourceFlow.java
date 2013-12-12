package de.avgl.dmp.converter.flow;

import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Optional;
import org.culturegraph.mf.framework.ObjectPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.stream.source.FileOpener;

import de.avgl.dmp.converter.DMPConverterException;
import de.avgl.dmp.converter.mf.stream.reader.CsvReader;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.utils.ConfigurationStatics;

public abstract class AbstractCSVResourceFlow<T> {

	private static final org.apache.log4j.Logger	LOG						= org.apache.log4j.Logger.getLogger(AbstractCSVResourceFlow.class);

	private final String							encoding;

	private final Character							escapeCharacter;

	private final Character							quoteCharacter;

	private final Character							columnDelimiter;

	private final String							rowDelimiter;

	private final int								ignoreLines;

	private final int								discardRows;

	Optional<Integer> 					atMost;


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

		final String text = jsonNode.asText();
		if (text.length() != 1) {
			if (text.matches("^\\\\t$")) {
				return Optional.of('\t');
			}

			throw new DMPConverterException(String.format("The field [%s] must be a single character only, got '%s' instead", key, text));
		}

		return Optional.of(text.charAt(0));
	}

	private Optional<Integer> getNumberParameter(final Configuration configuration, final String key) throws DMPConverterException {
		final JsonNode jsonNode = getParameterValue(configuration, key);
		if (jsonNode == null) {
			return Optional.absent();
		}

		if (!jsonNode.isNumber()) {
			return Optional.of(jsonNode.asInt());
		}

		final String text = jsonNode.asText();

		final int intValue;
		try {
			intValue = Integer.valueOf(text);
		} catch (final NumberFormatException e) {
			throw new DMPConverterException(String.format("The field [%s] must be numeric or a numeric string, got '%s' instead", key, text));
		}

		return Optional.of(intValue);
	}

	protected AbstractCSVResourceFlow() {
		this.encoding = ConfigurationStatics.DEFAULT_ENCODING;
		this.escapeCharacter = ConfigurationStatics.DEFAULT_ESCAPE_CHARACTER;
		this.quoteCharacter = ConfigurationStatics.DEFAULT_QUOTE_CHARACTER;
		this.columnDelimiter = ConfigurationStatics.DEFAULT_COLUMN_DELIMITER;
		this.rowDelimiter = ConfigurationStatics.DEFAULT_ROW_DELIMITER;
		this.ignoreLines = ConfigurationStatics.DEFAULT_IGNORE_LINES;
		this.discardRows = ConfigurationStatics.DEFAULT_DISCARD_ROWS;
		this.atMost = Optional.absent();
	}

	AbstractCSVResourceFlow(final String encoding, final Character escapeCharacter, final Character quoteCharacter,
	                        final Character columnDelimiter, final String rowDelimiter) {

		this.encoding = encoding;
		this.escapeCharacter = escapeCharacter;
		this.quoteCharacter = quoteCharacter;
		this.columnDelimiter = columnDelimiter;
		this.rowDelimiter = rowDelimiter;

		this.ignoreLines = ConfigurationStatics.DEFAULT_IGNORE_LINES;
		this.discardRows = ConfigurationStatics.DEFAULT_DISCARD_ROWS;
		this.atMost = Optional.absent();
	}

	AbstractCSVResourceFlow(final Configuration configuration) throws DMPConverterException {

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
		final Optional<Integer> ignoreLinesOptional = getNumberParameter(configuration, ConfigurationStatics.IGNORE_LINES);
		final Optional<Integer> discardRowsOptional = getNumberParameter(configuration, ConfigurationStatics.DISCARD_ROWS);
		final Optional<Integer> atMostOptional = getNumberParameter(configuration, ConfigurationStatics.AT_MOST);

		this.encoding = encodingOptional.or(ConfigurationStatics.DEFAULT_ENCODING);
		this.escapeCharacter = escapeCharacterOptional.or(ConfigurationStatics.DEFAULT_ESCAPE_CHARACTER);
		this.quoteCharacter = quoteCharacterOptional.or(ConfigurationStatics.DEFAULT_QUOTE_CHARACTER);
		this.columnDelimiter = columnDelimiterOptional.or(ConfigurationStatics.DEFAULT_COLUMN_DELIMITER);
		this.rowDelimiter = rowDelimiterOptional.or(ConfigurationStatics.DEFAULT_ROW_DELIMITER);
		this.ignoreLines = ignoreLinesOptional.or(ConfigurationStatics.DEFAULT_IGNORE_LINES);
		this.discardRows = discardRowsOptional.or(ConfigurationStatics.DEFAULT_DISCARD_ROWS);
		this.atMost = atMostOptional;

		try {
			Charset.forName(this.encoding);
		} catch (final UnsupportedCharsetException e) {
			throw new DMPConverterException(String.format("Unsupported Encoding - [%s]", e.getCharsetName()));
		}
	}

	public T applyFile(final String filePath) throws DMPConverterException {

		final FileOpener opener = new FileOpener();

		// set encoding
		final String finalEncoding = encoding != null ? encoding : ConfigurationStatics.DEFAULT_ENCODING;
		opener.setEncoding(finalEncoding);

		return apply(filePath, opener);
	}

	public T apply(final String obj, final ObjectPipe<String, ObjectReceiver<Reader>> opener) throws DMPConverterException {

		// set parsing attributes
		final CsvReader reader = new CsvReader(escapeCharacter, quoteCharacter, columnDelimiter, rowDelimiter,
				ignoreLines, discardRows, atMost);

		// TODO: process header from configuration
		reader.setHeader(true);

		final CsvReader pipe = opener.setReceiver(reader);

		try {

			return process(opener, obj, pipe);
		} catch (final RuntimeException e) {
			throw new DMPConverterException(e.getMessage());
		}

	}

	protected abstract T process(ObjectPipe<String, ObjectReceiver<Reader>> opener, String obj, CsvReader pipe);
}
