package de.avgl.dmp.converter.flow.test.csv;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import de.avgl.dmp.converter.DMPConverterException;
import de.avgl.dmp.converter.flow.CSVResourceFlowFactory;
import de.avgl.dmp.converter.flow.CSVSourceResourceCSVJSONPreviewFlow;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.utils.ConfigurationStatics;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

/**
 * @author sreichert
 * @author tgaengler
 */
public class CSVSourceResourceCSVJSONPreviewFlowTest {

	private static final String baseDir = "csv_config" + System.getProperty("file.separator"); 
	
	
	@Test
	public void testEndToEnd() throws Exception {

		final String csvPath = "test_csv.csv";		
		final String expectedPath = "test_csv.preview.json";

		final CSVSourceResourceCSVJSONPreviewFlow flow = CSVResourceFlowFactory.fromConfigurationParameters(
				Charsets.UTF_8.name(), '\\', '"', ';', "\n", CSVSourceResourceCSVJSONPreviewFlow.class);
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

		final CSVSourceResourceCSVJSONPreviewFlow flow = CSVResourceFlowFactory.fromConfiguration(configuration, CSVSourceResourceCSVJSONPreviewFlow.class);
		flow.withLimit(50);
		
		runTest(csvPath, expectedPath, flow);
		
	}
	
	

	/**
	 * e.g.
	 * 1;2;3 
	 * a;b;"c;d"
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCoumnDelimiterInQuotes() throws Exception {

		final String csvPath = baseDir + "coumnDelimiterInQuotes.csv";		
		final String expectedPath = baseDir + "coumnDelimiterInQuotes.preview.json";
		final CSVSourceResourceCSVJSONPreviewFlow flow = CSVResourceFlowFactory.fromConfigurationParameters(
				Charsets.UTF_8.name(), '\\', '"', ';', "\n", CSVSourceResourceCSVJSONPreviewFlow.class);
		
		runTest(csvPath, expectedPath, flow);
	}
	
	
	/**
	 * e.g.
	 * 1;2;3 
	 * a;b;""
	 * 
	 * @throws Exception
	 */
	@Test
	public void testEmptyColumnQuoted() throws Exception {

		final String csvPath = baseDir + "emptyColumnQuoted.csv";		
		final String expectedPath = baseDir + "emptyColumnQuoted.preview.json";
		final CSVSourceResourceCSVJSONPreviewFlow flow = CSVResourceFlowFactory.fromConfigurationParameters(
				Charsets.UTF_8.name(), '\\', '"', ';', "\n", CSVSourceResourceCSVJSONPreviewFlow.class);
		
		runTest(csvPath, expectedPath, flow);	
	}
	
	
	/**
	 * e.g.
	 * 1;2;3 
	 * a;b;" "
	 * 
	 * @throws Exception
	 */
	@Test
	public void testWhitespaceQuoted() throws Exception {

		final String csvPath = baseDir + "whitespaceQuoted.csv";		
		final String expectedPath = baseDir + "whitespaceQuoted.preview.json";
		final CSVSourceResourceCSVJSONPreviewFlow flow = CSVResourceFlowFactory.fromConfigurationParameters(
				Charsets.UTF_8.name(), '\\', '"', ';', "\n", CSVSourceResourceCSVJSONPreviewFlow.class);
		
		runTest(csvPath, expectedPath, flow);	
	}
	
	
	/**
	 * e.g.
	 * "1"; "2";"3" 
	 * "a";"b";"c"
	 * 
	 * @throws Exception
	 */
	@Test
	public void testWhitespaceBeforeQuoteInHeader() throws Exception {

		final String csvPath = baseDir + "whitespaceBeforeQuoteInHeader.csv";		
		final String expectedPath = baseDir + "whitespaceBeforeQuoteInHeader.preview.json";
		final CSVSourceResourceCSVJSONPreviewFlow flow = CSVResourceFlowFactory.fromConfigurationParameters(
				Charsets.UTF_8.name(), '\\', '"', ';', "\n", CSVSourceResourceCSVJSONPreviewFlow.class);
		
		runTest(csvPath, expectedPath, flow);	
	}
	
	
	/**
	 * e.g.
	 * "1";"2" ;"3" 
	 * "a";"b";"c"
	 * 
	 * @throws Exception
	 */
	@Test
	public void testWhitespaceAfterQuoteInHeader() throws Exception {

		final String csvPath = baseDir + "whitespaceAfterQuoteInHeader.csv";		
		final String expectedPath = baseDir + "whitespaceAfterQuoteInHeader.preview.json";
		final CSVSourceResourceCSVJSONPreviewFlow flow = CSVResourceFlowFactory.fromConfigurationParameters(
				Charsets.UTF_8.name(), '\\', '"', ';', "\n", CSVSourceResourceCSVJSONPreviewFlow.class);
		
		runTest(csvPath, expectedPath, flow);	
	}
	
	
	
	/**
	 * e.g.
	 * 1;2;3 
	 * a;b;c
	 * <br /> 
	 *  
	 * @throws Exception
	 */
	@Test
	public void testTrailingEmptyRows() throws Exception {

		final String csvPath = baseDir + "trailingEmptyRows.csv";		
		final String expectedPath = baseDir + "trailingEmptyRows.preview.json";
		final CSVSourceResourceCSVJSONPreviewFlow flow = CSVResourceFlowFactory.fromConfigurationParameters(
				Charsets.UTF_8.name(), '\\', '"', ';', "\n", CSVSourceResourceCSVJSONPreviewFlow.class);
		
		runTest(csvPath, expectedPath, flow);
	}	
	
		
	/**
	 * e.g.
	 * 1;2;3
	 * <br />  
	 * a;b;c
	 *  
	 * @throws Exception
	 */
	@Test
	public void testIntermediateEmptyRows() throws Exception {

		final String csvPath = baseDir + "intermediateEmptyRows.csv";		
		final String expectedPath = baseDir + "intermediateEmptyRows.preview.json";
		final CSVSourceResourceCSVJSONPreviewFlow flow = CSVResourceFlowFactory.fromConfigurationParameters(
				Charsets.UTF_8.name(), '\\', '"', ';', "\n", CSVSourceResourceCSVJSONPreviewFlow.class);
		
		runTest(csvPath, expectedPath, flow);
	}	

	
	/**
	 * e.g.
	 * 1;2;3 
	 * a;b
	 *  
	 * @throws Exception
	 */
	@Test(expected = DMPConverterException.class)
	public void testMissingColumn() throws Exception {

		final String csvPath = baseDir + "missingColumn.csv";		
		final String expectedPath = baseDir + "missingColumn.preview.json";
		final CSVSourceResourceCSVJSONPreviewFlow flow = CSVResourceFlowFactory.fromConfigurationParameters(
				Charsets.UTF_8.name(), '\\', '"', ';', "\n", CSVSourceResourceCSVJSONPreviewFlow.class);
		
		runTest(csvPath, expectedPath, flow);
	}	

	
	/**
	 * e.g.
	 * 1;2;3 
	 * a;b;c;d
	 *  
	 * @throws Exception
	 */
	@Test(expected = DMPConverterException.class)
	public void testSuperfluousColumn() throws Exception {

		final String csvPath = baseDir + "superfluousColumn.csv";		
		final String expectedPath = baseDir + "superfluousColumn.preview.json";
		final CSVSourceResourceCSVJSONPreviewFlow flow = CSVResourceFlowFactory.fromConfigurationParameters(
				Charsets.UTF_8.name(), '\\', '"', ';', "\n", CSVSourceResourceCSVJSONPreviewFlow.class);
		
		runTest(csvPath, expectedPath, flow);
	}	
	
	
	/**
	 * e.g.
	 * 1;2;3 
	 * a;b;\"c1\" c2
	 *  
	 * @throws Exception
	 */
	@Test
	public void testEscapedQuote() throws Exception {

		final String csvPath = baseDir + "escapedQuote.csv";		
		final String expectedPath = baseDir + "escapedQuote.preview.json";
		final CSVSourceResourceCSVJSONPreviewFlow flow = CSVResourceFlowFactory.fromConfigurationParameters(
				Charsets.UTF_8.name(), '\\', '"', ';', "\n", CSVSourceResourceCSVJSONPreviewFlow.class);
		
		runTest(csvPath, expectedPath, flow);
	}
	
	
	/**
	 * e.g.
	 * 1;2;3 
	 * a;b;"c1 EOL 
	 * c2"
	 *  
	 * Sandro: this test may fail on unix if the csv file is converted to EOL=LF
	 *  
	 * @throws Exception
	 */
	@Test
	public void testNewlineCharInQuotes() throws Exception {

		final String csvPath = baseDir + "newlineSurroundedByQuotes_CRLF.csv";
		final String expectedPath = baseDir + "newlineSurroundedByQuotes.preview_CRLF.json";
		final CSVSourceResourceCSVJSONPreviewFlow flow = CSVResourceFlowFactory.fromConfigurationParameters(
				Charsets.UTF_8.name(), '\\', '"', ';', "\r\n", CSVSourceResourceCSVJSONPreviewFlow.class);
		
		runTest(csvPath, expectedPath, flow);
	}
	
	
	/**
	 * e.g.
	 * 1;2;3 
	 * a;b;c1 EOL 
	 * c2
	 *    
	 * @throws Exception
	 */
	@Test(expected = DMPConverterException.class)
	public void testNewlineCharWithoutQuotes() throws Exception {

		final String csvPath = baseDir + "newlineWithoutQuotes_CRLF.csv";		
		final String expectedPath = baseDir + "newlineWithoutQuotes.preview_CRLF.json";
		final CSVSourceResourceCSVJSONPreviewFlow flow = CSVResourceFlowFactory.fromConfigurationParameters(
				Charsets.UTF_8.name(), '\\', '"', ';', "\r\n", CSVSourceResourceCSVJSONPreviewFlow.class);
		
		runTest(csvPath, expectedPath, flow);
	}
	
	
	/**
	 * e.g.
	 * 1;2;3 
	 * a;b EOL
	 * ;c
	 *    
	 * @throws Exception
	 */
	@Test(expected = DMPConverterException.class)
	public void testNewlineCharWithoutQuotesEndOfColumn() throws Exception {

		final String csvPath = baseDir + "newlineWithoutQuotesEndOfColumn_CRLF.csv";		
		final String expectedPath = baseDir + "newlineWithoutQuotesEndOfColumn.preview_CRLF.json";
		final CSVSourceResourceCSVJSONPreviewFlow flow = CSVResourceFlowFactory.fromConfigurationParameters(
				Charsets.UTF_8.name(), '\\', '"', ';', "\r\n", CSVSourceResourceCSVJSONPreviewFlow.class);
		
		runTest(csvPath, expectedPath, flow);
	}
	
	
	/**
	 * e.g.
	 * 1;2;3 
	 * a;b;ʤ
	 *    
	 * @throws Exception
	 */
	@Test
	public void testUTF8Char() throws Exception {

		final String csvPath = baseDir + "UTF-8.csv";		
		final String expectedPath = baseDir + "UTF-8.preview.json";
		final CSVSourceResourceCSVJSONPreviewFlow flow = CSVResourceFlowFactory.fromConfigurationParameters(
				Charsets.UTF_8.name(), '\\', '"', ';', "\n", CSVSourceResourceCSVJSONPreviewFlow.class);
		
		runTest(csvPath, expectedPath, flow);
	}
	

	/**
	 * e.g.
	 * csv-comment line 1
	 * csv-comment line 2
	 * 1;2;3 
	 * a;b;c
	 *    
	 * @throws Exception
	 */
	@Test
	public void testIgnoreFirstTwoLines() throws Exception {
		
		final String csvPath = baseDir + "ignoreFirstTwoLines.csv";		
		final String expectedPath = baseDir + "ignoreFirstTwoLines.preview.json";

		final Configuration configuration = new Configuration();

		configuration.addParameter(ConfigurationStatics.COLUMN_DELIMITER, new TextNode(";"));
		configuration.addParameter(ConfigurationStatics.ENCODING, new TextNode("UTF-8"));
		configuration.addParameter(ConfigurationStatics.ESCAPE_CHARACTER, new TextNode("\\"));
		configuration.addParameter(ConfigurationStatics.QUOTE_CHARACTER, new TextNode("\""));
		configuration.addParameter(ConfigurationStatics.ROW_DELIMITER, new TextNode("\n"));
		configuration.addParameter(ConfigurationStatics.FIRST_ROW_IS_HEADINGS, BooleanNode.TRUE);
		configuration.addParameter(ConfigurationStatics.IGNORE_LINES, new IntNode(2));

		final CSVSourceResourceCSVJSONPreviewFlow flow = CSVResourceFlowFactory.fromConfiguration(configuration, CSVSourceResourceCSVJSONPreviewFlow.class);
				
		runTest(csvPath, expectedPath, flow);
		
	}
	

	/**
	 * e.g.
	 * 1;2;3 
	 * a;b;c
	 * d;e;f
	 * g;h;i
	 *    
	 * @throws Exception
	 */
	@Test
	public void testAtMostTwoRows() throws Exception {
		
		final String csvPath = baseDir + "atMostTwoRows.csv";		
		final String expectedPath = baseDir + "atMostTwoRows.preview.json";

		final Configuration configuration = new Configuration();

		configuration.addParameter(ConfigurationStatics.COLUMN_DELIMITER, new TextNode(";"));
		configuration.addParameter(ConfigurationStatics.ENCODING, new TextNode("UTF-8"));
		configuration.addParameter(ConfigurationStatics.ESCAPE_CHARACTER, new TextNode("\\"));
		configuration.addParameter(ConfigurationStatics.QUOTE_CHARACTER, new TextNode("\""));
		configuration.addParameter(ConfigurationStatics.ROW_DELIMITER, new TextNode("\n"));
		configuration.addParameter(ConfigurationStatics.FIRST_ROW_IS_HEADINGS, BooleanNode.TRUE);

		final CSVSourceResourceCSVJSONPreviewFlow flow = CSVResourceFlowFactory.fromConfiguration(configuration, CSVSourceResourceCSVJSONPreviewFlow.class);
		flow.withLimit(2);
		
		runTest(csvPath, expectedPath, flow);		
	}
	

	/**
	 * e.g.
	 * 1;2;3 
	 * a;b;c
	 * d;e;f
	 * g;h;i
	 *    
	 * @throws Exception
	 */
	@Test
	public void testDiscardInitialTwoRows() throws Exception {
		
		final String csvPath = baseDir + "discardInitialTwoRows.csv";		
		final String expectedPath = baseDir + "discardInitialTwoRows.preview.json";

		final Configuration configuration = new Configuration();

		configuration.addParameter(ConfigurationStatics.COLUMN_DELIMITER, new TextNode(";"));
		configuration.addParameter(ConfigurationStatics.ENCODING, new TextNode("UTF-8"));
		configuration.addParameter(ConfigurationStatics.ESCAPE_CHARACTER, new TextNode("\\"));
		configuration.addParameter(ConfigurationStatics.QUOTE_CHARACTER, new TextNode("\""));
		configuration.addParameter(ConfigurationStatics.ROW_DELIMITER, new TextNode("\n"));
		configuration.addParameter(ConfigurationStatics.FIRST_ROW_IS_HEADINGS, BooleanNode.TRUE);
		configuration.addParameter(ConfigurationStatics.DISCARD_ROWS, new IntNode(2));

		final CSVSourceResourceCSVJSONPreviewFlow flow = CSVResourceFlowFactory.fromConfiguration(configuration, CSVSourceResourceCSVJSONPreviewFlow.class);
		
		runTest(csvPath, expectedPath, flow);		
	}




	private void runTest(final String csvPath, final String expectedPath, CSVSourceResourceCSVJSONPreviewFlow flow)
			throws IOException, DMPConverterException {
		
		final String expected = DMPPersistenceUtil.getResourceAsString(expectedPath);

		final URL url = Resources.getResource(csvPath);
		final File file = FileUtils.toFile(url);

		final String actual = flow.applyFile(file.getAbsolutePath());

		Assert.assertEquals(expected.trim(), actual.trim());
	}
	
	
	

}
