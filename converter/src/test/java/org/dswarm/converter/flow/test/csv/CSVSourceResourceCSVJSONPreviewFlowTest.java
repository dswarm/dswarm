/**
 * Copyright (C) 2013 – 2015 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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
import org.dswarm.converter.GuicedTest;
import org.dswarm.converter.flow.CSVResourceFlowFactory;
import org.dswarm.converter.flow.CSVSourceResourceCSVJSONPreviewFlow;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.utils.ConfigurationStatics;
import org.dswarm.persistence.service.UUIDService;
import org.dswarm.persistence.util.DMPPersistenceUtil;

/**
 * @author sreichert
 * @author tgaengler
 */
public class CSVSourceResourceCSVJSONPreviewFlowTest extends GuicedTest {

	private static final String BASE_DIR = "csv_config" + System.getProperty("file.separator");

	@Test
	public void testEndToEnd() throws Exception {

		final String csvPath = "test_csv.csv";
		final String expectedPath = "test_csv.preview.json";

		final CSVSourceResourceCSVJSONPreviewFlow flow = injector
				.getInstance(CSVResourceFlowFactory.class)
				.jsonPreview(Charsets.UTF_8.name(), '\\', '"', ';', "\n")
				.withLimit(50);

		runTest(csvPath, expectedPath, flow);
	}

	@Test
	public void testNoHeaders() throws Exception {

		final String csvPath = "test_csv.csv";
		final String expectedPath = "test_csv_no_headers.json";

		final String uuid = UUIDService.getUUID(Configuration.class.getSimpleName());

		final Configuration configuration = new Configuration(uuid);

		configuration.addParameter(ConfigurationStatics.COLUMN_DELIMITER, new TextNode(";"));
		configuration.addParameter(ConfigurationStatics.ENCODING, new TextNode("UTF-8"));
		configuration.addParameter(ConfigurationStatics.ESCAPE_CHARACTER, new TextNode("\\"));
		configuration.addParameter(ConfigurationStatics.QUOTE_CHARACTER, new TextNode("\""));
		configuration.addParameter(ConfigurationStatics.ROW_DELIMITER, new TextNode("\n"));
		configuration.addParameter(ConfigurationStatics.FIRST_ROW_IS_HEADINGS, BooleanNode.FALSE);


		final CSVSourceResourceCSVJSONPreviewFlow flow = injector
				.getInstance(CSVResourceFlowFactory.class)
				.jsonPreview(configuration)
				.withLimit(50);

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

		final String csvPath = CSVSourceResourceCSVJSONPreviewFlowTest.BASE_DIR + "columnDelimiterInQuotes.csv";
		final String expectedPath = CSVSourceResourceCSVJSONPreviewFlowTest.BASE_DIR + "columnDelimiterInQuotes.preview.json";

		final CSVSourceResourceCSVJSONPreviewFlow flow = injector
				.getInstance(CSVResourceFlowFactory.class)
				.jsonPreview(Charsets.UTF_8.name(), '\\', '"', ';', "\n");

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

		final String csvPath = CSVSourceResourceCSVJSONPreviewFlowTest.BASE_DIR + "emptyColumnQuoted.csv";
		final String expectedPath = CSVSourceResourceCSVJSONPreviewFlowTest.BASE_DIR + "emptyColumnQuoted.preview.json";


		final CSVSourceResourceCSVJSONPreviewFlow flow = injector
				.getInstance(CSVResourceFlowFactory.class)
				.jsonPreview(Charsets.UTF_8.name(), '\\', '"', ';', "\n");

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

		final String csvPath = CSVSourceResourceCSVJSONPreviewFlowTest.BASE_DIR + "whitespaceQuoted.csv";
		final String expectedPath = CSVSourceResourceCSVJSONPreviewFlowTest.BASE_DIR + "whitespaceQuoted.preview.json";

		final CSVSourceResourceCSVJSONPreviewFlow flow = injector
				.getInstance(CSVResourceFlowFactory.class)
				.jsonPreview(Charsets.UTF_8.name(), '\\', '"', ';', "\n");

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

		final String csvPath = CSVSourceResourceCSVJSONPreviewFlowTest.BASE_DIR + "whitespaceBeforeQuoteInHeader.csv";
		final String expectedPath = CSVSourceResourceCSVJSONPreviewFlowTest.BASE_DIR + "whitespaceBeforeQuoteInHeader.preview.json";

		final CSVSourceResourceCSVJSONPreviewFlow flow = injector
				.getInstance(CSVResourceFlowFactory.class)
				.jsonPreview(Charsets.UTF_8.name(), '\\', '"', ';', "\n");

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

		final String csvPath = CSVSourceResourceCSVJSONPreviewFlowTest.BASE_DIR + "whitespaceAfterQuoteInHeader.csv";
		final String expectedPath = CSVSourceResourceCSVJSONPreviewFlowTest.BASE_DIR + "whitespaceAfterQuoteInHeader.preview.json";

		final CSVSourceResourceCSVJSONPreviewFlow flow = injector
				.getInstance(CSVResourceFlowFactory.class)
				.jsonPreview(Charsets.UTF_8.name(), '\\', '"', ';', "\n");

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

		final String csvPath = CSVSourceResourceCSVJSONPreviewFlowTest.BASE_DIR + "trailingEmptyRows.csv";
		final String expectedPath = CSVSourceResourceCSVJSONPreviewFlowTest.BASE_DIR + "trailingEmptyRows.preview.json";

		final CSVSourceResourceCSVJSONPreviewFlow flow = injector
				.getInstance(CSVResourceFlowFactory.class)
				.jsonPreview(Charsets.UTF_8.name(), '\\', '"', ';', "\n");

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

		final String csvPath = CSVSourceResourceCSVJSONPreviewFlowTest.BASE_DIR + "intermediateEmptyRows.csv";
		final String expectedPath = CSVSourceResourceCSVJSONPreviewFlowTest.BASE_DIR + "intermediateEmptyRows.preview.json";
		final CSVSourceResourceCSVJSONPreviewFlow flow = injector
				.getInstance(CSVResourceFlowFactory.class)
				.jsonPreview(Charsets.UTF_8.name(), '\\', '"', ';', "\n");

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

		final String csvPath = CSVSourceResourceCSVJSONPreviewFlowTest.BASE_DIR + "missingColumn.csv";
		final String expectedPath = CSVSourceResourceCSVJSONPreviewFlowTest.BASE_DIR + "missingColumn.preview.json";

		final CSVSourceResourceCSVJSONPreviewFlow flow = injector
				.getInstance(CSVResourceFlowFactory.class)
				.jsonPreview(Charsets.UTF_8.name(), '\\', '"', ';', "\n");

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

		final String csvPath = CSVSourceResourceCSVJSONPreviewFlowTest.BASE_DIR + "superfluousColumn.csv";
		final String expectedPath = CSVSourceResourceCSVJSONPreviewFlowTest.BASE_DIR + "superfluousColumn.preview.json";

		final CSVSourceResourceCSVJSONPreviewFlow flow = injector
				.getInstance(CSVResourceFlowFactory.class)
				.jsonPreview(Charsets.UTF_8.name(), '\\', '"', ';', "\n");

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

		final String csvPath = CSVSourceResourceCSVJSONPreviewFlowTest.BASE_DIR + "escapedQuote.csv";
		final String expectedPath = CSVSourceResourceCSVJSONPreviewFlowTest.BASE_DIR + "escapedQuote.preview.json";

		final CSVSourceResourceCSVJSONPreviewFlow flow = injector
				.getInstance(CSVResourceFlowFactory.class)
				.jsonPreview(Charsets.UTF_8.name(), '\\', '"', ';', "\n");

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

		final String csvPath = CSVSourceResourceCSVJSONPreviewFlowTest.BASE_DIR + "newlineCharInQuotes_CRLF.csv";
		final String expectedPath = CSVSourceResourceCSVJSONPreviewFlowTest.BASE_DIR + "newlineCharInQuotes.preview_CRLF.json";

		final CSVSourceResourceCSVJSONPreviewFlow flow = injector
				.getInstance(CSVResourceFlowFactory.class)
				.jsonPreview(Charsets.UTF_8.name(), '\\', '"', ';', "\r\n");

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

		final String csvPath = CSVSourceResourceCSVJSONPreviewFlowTest.BASE_DIR + "newlineWithoutQuotes_CRLF.csv";
		final String expectedPath = CSVSourceResourceCSVJSONPreviewFlowTest.BASE_DIR + "newlineWithoutQuotes.preview_CRLF.json";

		final CSVSourceResourceCSVJSONPreviewFlow flow = injector
				.getInstance(CSVResourceFlowFactory.class)
				.jsonPreview(Charsets.UTF_8.name(), '\\', '"', ';', "\r\n");

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

		final String csvPath = CSVSourceResourceCSVJSONPreviewFlowTest.BASE_DIR + "newlineWithoutQuotesEndOfColumn_CRLF.csv";
		final String expectedPath = CSVSourceResourceCSVJSONPreviewFlowTest.BASE_DIR + "newlineWithoutQuotesEndOfColumn.preview_CRLF.json";

		final CSVSourceResourceCSVJSONPreviewFlow flow = injector
				.getInstance(CSVResourceFlowFactory.class)
				.jsonPreview(Charsets.UTF_8.name(), '\\', '"', ';', "\r\n");

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

		final String csvPath = CSVSourceResourceCSVJSONPreviewFlowTest.BASE_DIR + "UTF-8.csv";
		final String expectedPath = CSVSourceResourceCSVJSONPreviewFlowTest.BASE_DIR + "UTF-8.preview.json";

		final CSVSourceResourceCSVJSONPreviewFlow flow = injector
				.getInstance(CSVResourceFlowFactory.class)
				.jsonPreview(Charsets.UTF_8.name(), '\\', '"', ';', "\n");

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

		final String csvPath = CSVSourceResourceCSVJSONPreviewFlowTest.BASE_DIR + "ignoreFirstTwoLines.csv";
		final String expectedPath = CSVSourceResourceCSVJSONPreviewFlowTest.BASE_DIR + "ignoreFirstTwoLines.preview.json";

		final String uuid = UUIDService.getUUID(Configuration.class.getSimpleName());

		final Configuration configuration = new Configuration(uuid);

		configuration.addParameter(ConfigurationStatics.COLUMN_DELIMITER, new TextNode(";"));
		configuration.addParameter(ConfigurationStatics.ENCODING, new TextNode("UTF-8"));
		configuration.addParameter(ConfigurationStatics.ESCAPE_CHARACTER, new TextNode("\\"));
		configuration.addParameter(ConfigurationStatics.QUOTE_CHARACTER, new TextNode("\""));
		configuration.addParameter(ConfigurationStatics.ROW_DELIMITER, new TextNode("\n"));
		configuration.addParameter(ConfigurationStatics.FIRST_ROW_IS_HEADINGS, BooleanNode.TRUE);
		configuration.addParameter(ConfigurationStatics.IGNORE_LINES, new IntNode(2));

		final CSVSourceResourceCSVJSONPreviewFlow flow = injector
				.getInstance(CSVResourceFlowFactory.class)
				.jsonPreview(configuration);

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

		final String csvPath = CSVSourceResourceCSVJSONPreviewFlowTest.BASE_DIR + "atMostTwoRows.csv";
		final String expectedPath = CSVSourceResourceCSVJSONPreviewFlowTest.BASE_DIR + "atMostTwoRows.preview.json";

		final String uuid = UUIDService.getUUID(Configuration.class.getSimpleName());

		final Configuration configuration = new Configuration(uuid);

		configuration.addParameter(ConfigurationStatics.COLUMN_DELIMITER, new TextNode(";"));
		configuration.addParameter(ConfigurationStatics.ENCODING, new TextNode("UTF-8"));
		configuration.addParameter(ConfigurationStatics.ESCAPE_CHARACTER, new TextNode("\\"));
		configuration.addParameter(ConfigurationStatics.QUOTE_CHARACTER, new TextNode("\""));
		configuration.addParameter(ConfigurationStatics.ROW_DELIMITER, new TextNode("\n"));
		configuration.addParameter(ConfigurationStatics.FIRST_ROW_IS_HEADINGS, BooleanNode.TRUE);

		final CSVSourceResourceCSVJSONPreviewFlow flow = injector
				.getInstance(CSVResourceFlowFactory.class)
				.jsonPreview(configuration)
				.withLimit(2);

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

		final String csvPath = CSVSourceResourceCSVJSONPreviewFlowTest.BASE_DIR + "discardInitialTwoRows.csv";
		final String expectedPath = CSVSourceResourceCSVJSONPreviewFlowTest.BASE_DIR + "discardInitialTwoRows.preview.json";

		final String uuid = UUIDService.getUUID(Configuration.class.getSimpleName());

		final Configuration configuration = new Configuration(uuid);

		configuration.addParameter(ConfigurationStatics.COLUMN_DELIMITER, new TextNode(";"));
		configuration.addParameter(ConfigurationStatics.ENCODING, new TextNode("UTF-8"));
		configuration.addParameter(ConfigurationStatics.ESCAPE_CHARACTER, new TextNode("\\"));
		configuration.addParameter(ConfigurationStatics.QUOTE_CHARACTER, new TextNode("\""));
		configuration.addParameter(ConfigurationStatics.ROW_DELIMITER, new TextNode("\n"));
		configuration.addParameter(ConfigurationStatics.FIRST_ROW_IS_HEADINGS, BooleanNode.TRUE);
		configuration.addParameter(ConfigurationStatics.DISCARD_ROWS, new IntNode(2));

		final CSVSourceResourceCSVJSONPreviewFlow flow = injector
				.getInstance(CSVResourceFlowFactory.class)
				.jsonPreview(configuration);

		runTest(csvPath, expectedPath, flow);
	}

	private static void runTest(final String csvPath, final String expectedPath, final CSVSourceResourceCSVJSONPreviewFlow flow) throws IOException,
			DMPConverterException {

		final String expected = DMPPersistenceUtil.getResourceAsString(expectedPath);

		final URL url = Resources.getResource(csvPath);
		final File file = FileUtils.toFile(url);

		final String actual = flow.applyFile(file.getAbsolutePath());

		Assert.assertEquals(expected.trim(), actual.trim());
	}

}
