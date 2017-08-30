/**
 * Copyright (C) 2013 – 2017 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dswarm.converter.flow;

import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Optional;
import org.culturegraph.mf.framework.ObjectPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.stream.source.FileOpener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.converter.DMPConverterException;
import org.dswarm.converter.mf.stream.reader.CsvReader;
import org.dswarm.converter.mf.stream.source.BOMResourceOpener;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.resource.utils.ConfigurationStatics;
import org.dswarm.persistence.model.resource.utils.DataModelUtils;

/**
 * @author phorn
 * @param <T>
 */
public abstract class AbstractCSVResourceFlow<T> {

	private static final Logger	LOG	= LoggerFactory.getLogger(AbstractCSVResourceFlow.class);

	private final String		encoding;

	private final Character		escapeCharacter;

	private final Character		quoteCharacter;

	private final Character		columnDelimiter;

	private final String		rowDelimiter;

	private final int			ignoreLines;

	private final int			discardRows;

	private final boolean		firstRowIsHeaders;

	protected Optional<Integer>	atMost;

	protected final String dataModelBaseURI;
	protected final String dataModelSchemaBaseURI;

	public AbstractCSVResourceFlow(final DataModel dataModel) throws DMPConverterException {

		if (dataModel == null) {

			throw new DMPConverterException("the data model shouldn't be null");
		}

		final Configuration configuration = dataModel.getConfiguration();

		if (configuration == null) {

			throw new DMPConverterException("the data model configuration shouldn't be null");
		}

		if (configuration.getParameters() == null) {

			throw new DMPConverterException("the data model configuration parameters shouldn't be null");
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
		this.firstRowIsHeaders = getBooleanParameter(configuration, ConfigurationStatics.FIRST_ROW_IS_HEADINGS,
				ConfigurationStatics.DEFAULT_FIRST_ROW_IS_HEADINGS);

		try {
			Charset.forName(this.encoding);
		} catch (final UnsupportedCharsetException e) {
			throw new DMPConverterException(String.format("Unsupported Encoding - [%s]", e.getCharsetName()));
		}

		dataModelBaseURI = DataModelUtils.determineDataModelBaseURI(dataModel);
		dataModelSchemaBaseURI = DataModelUtils.determineDataModelSchemaBaseURI(dataModel);
	}

	public AbstractCSVResourceFlow(final String encoding, final Character escapeCharacter, final Character quoteCharacter,
			final Character columnDelimiter, final String rowDelimiter) {

		this.encoding = encoding;
		this.escapeCharacter = escapeCharacter;
		this.quoteCharacter = quoteCharacter;
		this.columnDelimiter = columnDelimiter;
		this.rowDelimiter = rowDelimiter;

		this.ignoreLines = ConfigurationStatics.DEFAULT_IGNORE_LINES;
		this.discardRows = ConfigurationStatics.DEFAULT_DISCARD_ROWS;
		this.atMost = Optional.absent();
		this.firstRowIsHeaders = true;

		this.dataModelBaseURI = null;
		this.dataModelSchemaBaseURI = null;
	}

	public AbstractCSVResourceFlow(final Configuration configuration) throws DMPConverterException {

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
		this.firstRowIsHeaders = getBooleanParameter(configuration, ConfigurationStatics.FIRST_ROW_IS_HEADINGS,
				ConfigurationStatics.DEFAULT_FIRST_ROW_IS_HEADINGS);

		try {
			Charset.forName(this.encoding);
		} catch (final UnsupportedCharsetException e) {
			throw new DMPConverterException(String.format("Unsupported Encoding - [%s]", e.getCharsetName()));
		}

		this.dataModelBaseURI = null;
		this.dataModelSchemaBaseURI = null;
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
		this.firstRowIsHeaders = ConfigurationStatics.DEFAULT_FIRST_ROW_IS_HEADINGS;

		this.dataModelBaseURI = null;
		this.dataModelSchemaBaseURI = null;
	}

	private static JsonNode getParameterValue(final Configuration configuration, final String key) throws DMPConverterException {

		if (key == null) {

			throw new DMPConverterException("the parameter key shouldn't be null");
		}

		final JsonNode valueNode = configuration.getParameter(key);

		if (valueNode == null) {

			AbstractCSVResourceFlow.LOG.debug("couldn't find value for parameter '{}'; try to utilise default value for this parameter", key);
		}

		return valueNode;
	}

	private static Optional<String> getStringParameter(final Configuration configuration, final String key) throws DMPConverterException {
		final JsonNode jsonNode = getParameterValue(configuration, key);
		if (jsonNode == null) {
			return Optional.absent();
		}

		return Optional.of(jsonNode.asText());
	}

	private static Optional<Character> getCharParameter(final Configuration configuration, final String key) throws DMPConverterException {
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

	private static Optional<Integer> getNumberParameter(final Configuration configuration, final String key) throws DMPConverterException {
		final JsonNode jsonNode = getParameterValue(configuration, key);
		if (jsonNode == null) {
			return Optional.absent();
		}

		if (jsonNode.isNumber()) {
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

	private static boolean getBooleanParameter(final Configuration configuration, final String key, final boolean defaultValue) throws DMPConverterException {
		final JsonNode jsonNode = getParameterValue(configuration, key);
		if (jsonNode == null) {
			return defaultValue;
		}

		if (jsonNode.isBoolean()) {
			return jsonNode.booleanValue();
		}

		final String text = jsonNode.asText();

		return Boolean.valueOf(text);
	}

	public T applyFile(final String filePath) throws DMPConverterException {

		final FileOpener opener = new FileOpener();

		// set encoding
		final String finalEncoding = encoding != null ? encoding : ConfigurationStatics.DEFAULT_ENCODING;
		opener.setEncoding(finalEncoding);

		return apply(filePath, opener);
	}

	public T applyResource(final String resourcePath) throws DMPConverterException {

		final BOMResourceOpener opener = new BOMResourceOpener();
		opener.setEncoding(encoding);

		return apply(resourcePath, opener);
	}

	public T apply(final String obj, final ObjectPipe<String, ObjectReceiver<Reader>> opener) throws DMPConverterException {

		// set parsing attributes
		final CsvReader reader = new CsvReader(escapeCharacter, quoteCharacter, columnDelimiter, rowDelimiter, ignoreLines, discardRows, atMost);

		reader.setHeader(firstRowIsHeaders);
		reader.setDataResourceSchemaBaseURI(dataModelSchemaBaseURI);

		final CsvReader pipe = opener.setReceiver(reader);

		try {

			return process(opener, obj, pipe);
		} catch (final RuntimeException e) {
			throw new DMPConverterException(e.getMessage());
		}

	}

	protected abstract T process(ObjectPipe<String, ObjectReceiver<Reader>> opener, String obj, CsvReader pipe);
}
