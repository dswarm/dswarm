/**
 * Copyright (C) 2013, 2014 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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
package org.dswarm.converter.flow.test.csv;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import org.dswarm.converter.DMPConverterException;
import org.dswarm.converter.flow.CSVResourceFlowFactory;
import org.dswarm.converter.flow.CSVSourceResourceCSVJSONPreviewFlow;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.utils.ConfigurationStatics;
import org.dswarm.persistence.util.DMPPersistenceUtil;

/**
 * @author sreichert
 * @author tgaengler
 */
public class CSVSourceResourceCSVJSONPreviewFlowTest {

	private static final String	baseDir	= "csv_config" + System.getProperty("file.separator");

	@Test
	public void testEndToEnd() throws Exception {

		final String csvPath = "test_csv.csv";
		final String expectedPath = "test_csv.preview.json";

		final CSVSourceResourceCSVJSONPreviewFlow flow = CSVResourceFlowFactory.fromConfigurationParameters(Charsets.UTF_8.name(), '\\', '"', ';',
				"\n", CSVSourceResourceCSVJSONPreviewFlow.class);
		flow.withLimit(50);

		runTest(csvPath, expectedPath, flow);
	}

	@Test
	public void testNoHeaders() throws Exception {

		final String csvPath = "test_csv.csv";
		final String expectedPath = "test_csv_no_headers.json";

		final Configuration configuration = new Configuration();

		configuration.addParameter(ConfigurationStatics.COLUMN_DELIMITER, new TextNode(";"));
		configuration.addParameter(ConfigurationStatics.ENCODING, new TextNode("UTF-8"));
		configuration.addParameter(ConfigurationStatics.ESCAPE_CHARACTER, new TextNode("\\"));
		configuration.addParameter(ConfigurationStatics.QUOTE_CHARACTER, new TextNode("\""));
		configuration.addParameter(ConfigurationStatics.ROW_DELIMITER, new TextNode("\n"));
		configuration.addParameter(ConfigurationStatics.FIRST_ROW_IS_HEADINGS, BooleanNode.FALSE);

		final CSVSourceResourceCSVJSONPreviewFlow flow = CSVResourceFlowFactory.fromConfiguration(configuration,
				CSVSourceResourceCSVJSONPreviewFlow.class);
		flow.withLimit(50);

		runTest(csvPath, expectedPath, flow);

	}

	/**
	 * e.g.<br />
	 * 1;2;3 <br />
	 * a;b;"c;d"<br />
	 * 
	 * @throws Exception
	 */
	@Test
	public void testColumnDelimiterInQuotes() throws Exception {

		final String csvPath = CSVSourceResourceCSVJSONPreviewFlowTest.baseDir + "columnDelimiterInQuotes.csv";
		final String expectedPath = CSVSourceResourceCSVJSONPreviewFlowTest.baseDir + "columnDelimiterInQuotes.preview.json";
		final CSVSourceResourceCSVJSONPreviewFlow flow = CSVResourceFlowFactory.fromConfigurationParameters(Charsets.UTF_8.name(), '\\', '"', ';',
				"\n", CSVSourceResourceCSVJSONPreviewFlow.class);

		runTest(csvPath, expectedPath, flow);
	}

	/**
	 * e.g.<br />
	 * 1;2;3<br />
	 * a;b;""<br />
	 * 
	 * @throws Exception
	 */
	@Test
	public void testEmptyColumnQuoted() throws Exception {

		final String csvPath = CSVSourceResourceCSVJSONPreviewFlowTest.baseDir + "emptyColumnQuoted.csv";
		final String expectedPath = CSVSourceResourceCSVJSONPreviewFlowTest.baseDir + "emptyColumnQuoted.preview.json";
		final CSVSourceResourceCSVJSONPreviewFlow flow = CSVResourceFlowFactory.fromConfigurationParameters(Charsets.UTF_8.name(), '\\', '"', ';',
				"\n", CSVSourceResourceCSVJSONPreviewFlow.class);

		runTest(csvPath, expectedPath, flow);
	}

	/**
	 * e.g.<br />
	 * 1;2;3<br />
	 * a;b;" "<br />
	 * 
	 * @throws Exception
	 */
	@Test
	public void testWhitespaceQuoted() throws Exception {

		final String csvPath = CSVSourceResourceCSVJSONPreviewFlowTest.baseDir + "whitespaceQuoted.csv";
		final String expectedPath = CSVSourceResourceCSVJSONPreviewFlowTest.baseDir + "whitespaceQuoted.preview.json";
		final CSVSourceResourceCSVJSONPreviewFlow flow = CSVResourceFlowFactory.fromConfigurationParameters(Charsets.UTF_8.name(), '\\', '"', ';',
				"\n", CSVSourceResourceCSVJSONPreviewFlow.class);

		runTest(csvPath, expectedPath, flow);
	}

	/**
	 * e.g.<br />
	 * "1"; "2";"3"<br />
	 * "a";"b";"c"<br />
	 * 
	 * @throws Exception
	 */
	@Test
	public void testWhitespaceBeforeQuoteInHeader() throws Exception {

		final String csvPath = CSVSourceResourceCSVJSONPreviewFlowTest.baseDir + "whitespaceBeforeQuoteInHeader.csv";
		final String expectedPath = CSVSourceResourceCSVJSONPreviewFlowTest.baseDir + "whitespaceBeforeQuoteInHeader.preview.json";
		final CSVSourceResourceCSVJSONPreviewFlow flow = CSVResourceFlowFactory.fromConfigurationParameters(Charsets.UTF_8.name(), '\\', '"', ';',
				"\n", CSVSourceResourceCSVJSONPreviewFlow.class);

		runTest(csvPath, expectedPath, flow);
	}

	/**
	 * e.g.<br />
	 * "1";"2" ;"3"<br />
	 * "a";"b";"c"<br />
	 * 
	 * @throws Exception
	 */
	@Test
	public void testWhitespaceAfterQuoteInHeader() throws Exception {

		final String csvPath = CSVSourceResourceCSVJSONPreviewFlowTest.baseDir + "whitespaceAfterQuoteInHeader.csv";
		final String expectedPath = CSVSourceResourceCSVJSONPreviewFlowTest.baseDir + "whitespaceAfterQuoteInHeader.preview.json";
		final CSVSourceResourceCSVJSONPreviewFlow flow = CSVResourceFlowFactory.fromConfigurationParameters(Charsets.UTF_8.name(), '\\', '"', ';',
				"\n", CSVSourceResourceCSVJSONPreviewFlow.class);

		runTest(csvPath, expectedPath, flow);
	}

	/**
	 * e.g.<br />
	 * 1;2;3<br />
	 * a;b;c<br />
	 * <br />
	 * 
	 * @throws Exception
	 */
	@Test
	public void testTrailingEmptyRows() throws Exception {

		final String csvPath = CSVSourceResourceCSVJSONPreviewFlowTest.baseDir + "trailingEmptyRows.csv";
		final String expectedPath = CSVSourceResourceCSVJSONPreviewFlowTest.baseDir + "trailingEmptyRows.preview.json";
		final CSVSourceResourceCSVJSONPreviewFlow flow = CSVResourceFlowFactory.fromConfigurationParameters(Charsets.UTF_8.name(), '\\', '"', ';',
				"\n", CSVSourceResourceCSVJSONPreviewFlow.class);

		runTest(csvPath, expectedPath, flow);
	}

	/**
	 * e.g.<br />
	 * 1;2;3<br />
	 * <br />
	 * a;b;c<br />
	 * 
	 * @throws Exception
	 */
	@Test
	public void testIntermediateEmptyRows() throws Exception {

		final String csvPath = CSVSourceResourceCSVJSONPreviewFlowTest.baseDir + "intermediateEmptyRows.csv";
		final String expectedPath = CSVSourceResourceCSVJSONPreviewFlowTest.baseDir + "intermediateEmptyRows.preview.json";
		final CSVSourceResourceCSVJSONPreviewFlow flow = CSVResourceFlowFactory.fromConfigurationParameters(Charsets.UTF_8.name(), '\\', '"', ';',
				"\n", CSVSourceResourceCSVJSONPreviewFlow.class);

		runTest(csvPath, expectedPath, flow);
	}

	/**
	 * e.g.<br />
	 * 1;2;3<br />
	 * a;b<br />
	 * 
	 * @throws Exception
	 */
	@Test(expected = DMPConverterException.class)
	public void testMissingColumn() throws Exception {

		final String csvPath = CSVSourceResourceCSVJSONPreviewFlowTest.baseDir + "missingColumn.csv";
		final String expectedPath = CSVSourceResourceCSVJSONPreviewFlowTest.baseDir + "missingColumn.preview.json";
		final CSVSourceResourceCSVJSONPreviewFlow flow = CSVResourceFlowFactory.fromConfigurationParameters(Charsets.UTF_8.name(), '\\', '"', ';',
				"\n", CSVSourceResourceCSVJSONPreviewFlow.class);

		runTest(csvPath, expectedPath, flow);
	}

	/**
	 * e.g.<br />
	 * 1;2;3 <br />
	 * a;b;c;d<br />
	 * 
	 * @throws Exception
	 */
	@Test(expected = DMPConverterException.class)
	public void testSuperfluousColumn() throws Exception {

		final String csvPath = CSVSourceResourceCSVJSONPreviewFlowTest.baseDir + "superfluousColumn.csv";
		final String expectedPath = CSVSourceResourceCSVJSONPreviewFlowTest.baseDir + "superfluousColumn.preview.json";
		final CSVSourceResourceCSVJSONPreviewFlow flow = CSVResourceFlowFactory.fromConfigurationParameters(Charsets.UTF_8.name(), '\\', '"', ';',
				"\n", CSVSourceResourceCSVJSONPreviewFlow.class);

		runTest(csvPath, expectedPath, flow);
	}

	/**
	 * e.g.<br />
	 * 1;2;3<br />
	 * a;b;\"c1\" c2<br />
	 * 
	 * @throws Exception
	 */
	@Test
	public void testEscapedQuote() throws Exception {

		final String csvPath = CSVSourceResourceCSVJSONPreviewFlowTest.baseDir + "escapedQuote.csv";
		final String expectedPath = CSVSourceResourceCSVJSONPreviewFlowTest.baseDir + "escapedQuote.preview.json";
		final CSVSourceResourceCSVJSONPreviewFlow flow = CSVResourceFlowFactory.fromConfigurationParameters(Charsets.UTF_8.name(), '\\', '"', ';',
				"\n", CSVSourceResourceCSVJSONPreviewFlow.class);

		runTest(csvPath, expectedPath, flow);
	}

	/**
	 * e.g.<br />
	 * 1;2;3 <br />
	 * a;b;"c1 EOL <br />
	 * c2" <br />
	 * <br />
	 * In case this test fails, check whether .gitattributes contains the following line:<br />
	 * *_CRLF.csv text eol=crlf
	 * 
	 * @throws Exception
	 */
	@Test
	public void testNewlineCharInQuotes() throws Exception {

		final String csvPath = CSVSourceResourceCSVJSONPreviewFlowTest.baseDir + "newlineCharInQuotes_CRLF.csv";
		final String expectedPath = CSVSourceResourceCSVJSONPreviewFlowTest.baseDir + "newlineCharInQuotes.preview_CRLF.json";
		final CSVSourceResourceCSVJSONPreviewFlow flow = CSVResourceFlowFactory.fromConfigurationParameters(Charsets.UTF_8.name(), '\\', '"', ';',
				"\r\n", CSVSourceResourceCSVJSONPreviewFlow.class);

		runTest(csvPath, expectedPath, flow);
	}

	/**
	 * e.g.<br />
	 * 1;2;3<br />
	 * a;b;c1 EOL<br />
	 * c2<br />
	 * 
	 * @throws Exception
	 */
	@Test(expected = DMPConverterException.class)
	public void testNewlineCharWithoutQuotes() throws Exception {

		final String csvPath = CSVSourceResourceCSVJSONPreviewFlowTest.baseDir + "newlineWithoutQuotes_CRLF.csv";
		final String expectedPath = CSVSourceResourceCSVJSONPreviewFlowTest.baseDir + "newlineWithoutQuotes.preview_CRLF.json";
		final CSVSourceResourceCSVJSONPreviewFlow flow = CSVResourceFlowFactory.fromConfigurationParameters(Charsets.UTF_8.name(), '\\', '"', ';',
				"\r\n", CSVSourceResourceCSVJSONPreviewFlow.class);

		runTest(csvPath, expectedPath, flow);
	}

	/**
	 * e.g.<br />
	 * 1;2;3<br />
	 * a;b EOL<br />
	 * ;c<br />
	 * 
	 * @throws Exception
	 */
	@Test(expected = DMPConverterException.class)
	public void testNewlineCharWithoutQuotesEndOfColumn() throws Exception {

		final String csvPath = CSVSourceResourceCSVJSONPreviewFlowTest.baseDir + "newlineWithoutQuotesEndOfColumn_CRLF.csv";
		final String expectedPath = CSVSourceResourceCSVJSONPreviewFlowTest.baseDir + "newlineWithoutQuotesEndOfColumn.preview_CRLF.json";
		final CSVSourceResourceCSVJSONPreviewFlow flow = CSVResourceFlowFactory.fromConfigurationParameters(Charsets.UTF_8.name(), '\\', '"', ';',
				"\r\n", CSVSourceResourceCSVJSONPreviewFlow.class);

		runTest(csvPath, expectedPath, flow);
	}

	/**
	 * e.g.<br />
	 * 1;2;3<br />
	 * a;b;ʤ<br />
	 * 
	 * @throws Exception
	 */
	@Test
	public void testUTF8Char() throws Exception {

		final String csvPath = CSVSourceResourceCSVJSONPreviewFlowTest.baseDir + "UTF-8.csv";
		final String expectedPath = CSVSourceResourceCSVJSONPreviewFlowTest.baseDir + "UTF-8.preview.json";
		final CSVSourceResourceCSVJSONPreviewFlow flow = CSVResourceFlowFactory.fromConfigurationParameters(Charsets.UTF_8.name(), '\\', '"', ';',
				"\n", CSVSourceResourceCSVJSONPreviewFlow.class);

		runTest(csvPath, expectedPath, flow);
	}

	/**
	 * e.g.<br />
	 * csv-comment line 1<br />
	 * csv-comment line 2<br />
	 * 1;2;3<br />
	 * a;b;c<br />
	 * 
	 * @throws Exception
	 */
	@Test
	public void testIgnoreFirstTwoLines() throws Exception {

		final String csvPath = CSVSourceResourceCSVJSONPreviewFlowTest.baseDir + "ignoreFirstTwoLines.csv";
		final String expectedPath = CSVSourceResourceCSVJSONPreviewFlowTest.baseDir + "ignoreFirstTwoLines.preview.json";

		final Configuration configuration = new Configuration();

		configuration.addParameter(ConfigurationStatics.COLUMN_DELIMITER, new TextNode(";"));
		configuration.addParameter(ConfigurationStatics.ENCODING, new TextNode("UTF-8"));
		configuration.addParameter(ConfigurationStatics.ESCAPE_CHARACTER, new TextNode("\\"));
		configuration.addParameter(ConfigurationStatics.QUOTE_CHARACTER, new TextNode("\""));
		configuration.addParameter(ConfigurationStatics.ROW_DELIMITER, new TextNode("\n"));
		configuration.addParameter(ConfigurationStatics.FIRST_ROW_IS_HEADINGS, BooleanNode.TRUE);
		configuration.addParameter(ConfigurationStatics.IGNORE_LINES, new IntNode(2));

		final CSVSourceResourceCSVJSONPreviewFlow flow = CSVResourceFlowFactory.fromConfiguration(configuration,
				CSVSourceResourceCSVJSONPreviewFlow.class);

		runTest(csvPath, expectedPath, flow);
	}

	/**
	 * e.g.<br />
	 * 1;2;3<br />
	 * a;b;c<br />
	 * d;e;f<br />
	 * g;h;i<br />
	 * 
	 * @throws Exception
	 */
	@Test
	public void testAtMostTwoRows() throws Exception {

		final String csvPath = CSVSourceResourceCSVJSONPreviewFlowTest.baseDir + "atMostTwoRows.csv";
		final String expectedPath = CSVSourceResourceCSVJSONPreviewFlowTest.baseDir + "atMostTwoRows.preview.json";

		final Configuration configuration = new Configuration();

		configuration.addParameter(ConfigurationStatics.COLUMN_DELIMITER, new TextNode(";"));
		configuration.addParameter(ConfigurationStatics.ENCODING, new TextNode("UTF-8"));
		configuration.addParameter(ConfigurationStatics.ESCAPE_CHARACTER, new TextNode("\\"));
		configuration.addParameter(ConfigurationStatics.QUOTE_CHARACTER, new TextNode("\""));
		configuration.addParameter(ConfigurationStatics.ROW_DELIMITER, new TextNode("\n"));
		configuration.addParameter(ConfigurationStatics.FIRST_ROW_IS_HEADINGS, BooleanNode.TRUE);

		final CSVSourceResourceCSVJSONPreviewFlow flow = CSVResourceFlowFactory.fromConfiguration(configuration,
				CSVSourceResourceCSVJSONPreviewFlow.class);
		flow.withLimit(2);

		runTest(csvPath, expectedPath, flow);
	}

	/**
	 * e.g.<br />
	 * 1;2;3<br />
	 * a;b;c<br />
	 * d;e;f<br />
	 * g;h;i<br />
	 * 
	 * @throws Exception
	 */
	@Test
	public void testDiscardInitialTwoRows() throws Exception {

		final String csvPath = CSVSourceResourceCSVJSONPreviewFlowTest.baseDir + "discardInitialTwoRows.csv";
		final String expectedPath = CSVSourceResourceCSVJSONPreviewFlowTest.baseDir + "discardInitialTwoRows.preview.json";

		final Configuration configuration = new Configuration();

		configuration.addParameter(ConfigurationStatics.COLUMN_DELIMITER, new TextNode(";"));
		configuration.addParameter(ConfigurationStatics.ENCODING, new TextNode("UTF-8"));
		configuration.addParameter(ConfigurationStatics.ESCAPE_CHARACTER, new TextNode("\\"));
		configuration.addParameter(ConfigurationStatics.QUOTE_CHARACTER, new TextNode("\""));
		configuration.addParameter(ConfigurationStatics.ROW_DELIMITER, new TextNode("\n"));
		configuration.addParameter(ConfigurationStatics.FIRST_ROW_IS_HEADINGS, BooleanNode.TRUE);
		configuration.addParameter(ConfigurationStatics.DISCARD_ROWS, new IntNode(2));

		final CSVSourceResourceCSVJSONPreviewFlow flow = CSVResourceFlowFactory.fromConfiguration(configuration,
				CSVSourceResourceCSVJSONPreviewFlow.class);

		runTest(csvPath, expectedPath, flow);
	}

	private void runTest(final String csvPath, final String expectedPath, final CSVSourceResourceCSVJSONPreviewFlow flow) throws IOException,
			DMPConverterException {

		final String expected = DMPPersistenceUtil.getResourceAsString(expectedPath);

		final URL url = Resources.getResource(csvPath);
		final File file = FileUtils.toFile(url);

		final String actual = flow.applyFile(file.getAbsolutePath());

		Assert.assertEquals(expected.trim(), actual.trim());
	}

}
