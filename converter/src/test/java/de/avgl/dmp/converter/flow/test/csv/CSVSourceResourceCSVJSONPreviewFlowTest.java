package de.avgl.dmp.converter.flow.test.csv;

import java.io.File;
import java.net.URL;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import de.avgl.dmp.converter.flow.CSVResourceFlowFactory;
import de.avgl.dmp.converter.flow.CSVSourceResourceCSVJSONPreviewFlow;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

import static org.junit.Assert.assertEquals;

public class CSVSourceResourceCSVJSONPreviewFlowTest {

	@Test
	public void testEndToEnd() throws Exception {

		final String expected = DMPPersistenceUtil.getResourceAsString("test_csv.json");

		final CSVSourceResourceCSVJSONPreviewFlow flow = CSVResourceFlowFactory.fromConfigurationParameters(Charsets.UTF_8.name(), '\\', '"', ';', "\n", CSVSourceResourceCSVJSONPreviewFlow.class);

		flow.withLimit(50);

		final URL url = Resources.getResource("test_csv.csv");
		final File file = FileUtils.toFile(url);

		final String actual = flow.applyFile(file.getAbsolutePath());

		assertEquals(expected.trim(), actual.trim());
	}
}
