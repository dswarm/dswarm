package de.avgl.dmp.converter.flow.test.csv;

import java.io.File;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import de.avgl.dmp.converter.flow.CSVResourceFlowFactory;
import de.avgl.dmp.converter.flow.CSVSourceResourceCSVJSONPreviewFlow;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

public class CSVSourceResourceCSVJSONPreviewFlowTest {

	@Test
	public void testEndToEnd() throws Exception {

		final String expected = DMPPersistenceUtil.getResourceAsString("test_csv.preview.json");

		final CSVSourceResourceCSVJSONPreviewFlow flow = CSVResourceFlowFactory.fromConfigurationParameters(Charsets.UTF_8.name(), '\\', '"', ';',
				"\n", CSVSourceResourceCSVJSONPreviewFlow.class);

		flow.withLimit(50);

		final URL url = Resources.getResource("test_csv.csv");
		final File file = FileUtils.toFile(url);

		final String actual = flow.applyFile(file.getAbsolutePath());

		Assert.assertEquals(expected.trim(), actual.trim());
	}
}
