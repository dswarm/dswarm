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
import java.io.StringWriter;
import java.net.URL;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.apache.commons.io.FileUtils;
import org.culturegraph.mf.stream.sink.ObjectJavaIoWriter;
import org.culturegraph.mf.stream.source.FileOpener;
import org.junit.Assert;
import org.junit.Test;

import org.dswarm.converter.GuicedTest;
import org.dswarm.converter.flow.CSVResourceFlowFactory;
import org.dswarm.converter.flow.CSVSourceResourceCSVPreviewFlow;
import org.dswarm.converter.mf.stream.reader.CsvReader;
import org.dswarm.converter.mf.stream.source.CSVEncoder;
import org.dswarm.persistence.util.DMPPersistenceUtil;

public class CSVSourceResourceCSVPreviewFlowTest extends GuicedTest {

	@Test
	public void testEndToEnd() throws Exception {

		final String expected = DMPPersistenceUtil.getResourceAsString("test_csv.csv");

		final CSVSourceResourceCSVPreviewFlow flow = injector
				.getInstance(CSVResourceFlowFactory.class)
				.csvPreview(Charsets.UTF_8.name(), '\\', '"', ';', "\n");

		final URL url = Resources.getResource("test_csv.csv");
		final File file = FileUtils.toFile(url);

		final String actual = flow.applyFile(file.getAbsolutePath());

		Assert.assertEquals(expected, actual);
	}

	@Test
	public void readCSVTest() throws Exception {

		final FileOpener opener = new FileOpener();

		// set encoding
		opener.setEncoding(Charsets.UTF_8.name());

		final URL url = Resources.getResource("test_csv.csv");
		final File file = FileUtils.toFile(url);

		// set column separator and line separator
		final CsvReader reader = new CsvReader('\\', '"', ';', "\n");

		// set number of header lines (if header lines = 1, then schema header line = 1)
		reader.setHeader(true);
		final CSVEncoder converter = new CSVEncoder();
		converter.withHeader();
		final StringWriter stringWriter = new StringWriter();
		final ObjectJavaIoWriter<String> writer = new ObjectJavaIoWriter<>(stringWriter);

		opener.setReceiver(reader).setReceiver(converter).setReceiver(writer);

		opener.process(file.getAbsolutePath());

		final String resultOutput = stringWriter.toString();

		Assert.assertNotNull("the result output shouldn't be null", resultOutput);

		final String expectedResult = DMPPersistenceUtil.getResourceAsString("test_csv.csv");

		Assert.assertEquals("the processing outputs are not equal", expectedResult, resultOutput);
	}
}
