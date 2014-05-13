package de.avgl.dmp.converter.flow.test.csv;

import java.io.File;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import de.avgl.dmp.converter.flow.CSVResourceFlowFactory;
import de.avgl.dmp.converter.flow.CSVSourceResourceCSVJSONPreviewFlow;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.utils.ConfigurationStatics;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

public class CSVSourceResourceCSVJSONPreviewFlowTest {

	@Test
	public void testEndToEnd() throws Exception {

		final String expected = DMPPersistenceUtil.getResourceAsString("test_csv.json");

		final CSVSourceResourceCSVJSONPreviewFlow flow = CSVResourceFlowFactory.fromConfigurationParameters(Charsets.UTF_8.name(), '\\', '"', ';',
				"\n", CSVSourceResourceCSVJSONPreviewFlow.class);

		flow.withLimit(50);

		final URL url = Resources.getResource("test_csv.csv");
		final File file = FileUtils.toFile(url);

		final String actual = flow.applyFile(file.getAbsolutePath());

		Assert.assertEquals(expected.trim(), actual.trim());
	}

	@Test
	public void testNoHeaders() throws Exception {

		final Configuration configuration = new Configuration();

		configuration.addParameter(ConfigurationStatics.COLUMN_DELIMITER, new TextNode(";"));
		configuration.addParameter(ConfigurationStatics.ENCODING, new TextNode("UTF-8"));
		configuration.addParameter(ConfigurationStatics.ESCAPE_CHARACTER, new TextNode("\\"));
		configuration.addParameter(ConfigurationStatics.QUOTE_CHARACTER, new TextNode("\""));
		configuration.addParameter(ConfigurationStatics.ROW_DELIMITER, new TextNode("\n"));
		configuration.addParameter(ConfigurationStatics.FIRST_ROW_IS_HEADINGS, BooleanNode.FALSE);

		final CSVSourceResourceCSVJSONPreviewFlow flow = CSVResourceFlowFactory.fromConfiguration(configuration, CSVSourceResourceCSVJSONPreviewFlow.class);
		flow.withLimit(50);

		final String expected = DMPPersistenceUtil.getResourceAsString("test_csv_no_headers.json");

		final URL url = Resources.getResource("test_csv.csv");
		final File file = FileUtils.toFile(url);

		final String actual = flow.applyFile(file.getAbsolutePath());

		Assert.assertEquals(expected.trim(), actual.trim());
	}
}
