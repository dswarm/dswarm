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
package org.dswarm.converter.schema.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import javax.xml.stream.XMLStreamException;

import com.google.common.io.Files;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;

import org.dswarm.converter.GuicedTest;
import org.dswarm.converter.schema.SolrXMLDataSourceConfigGenerator;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.model.schema.utils.SchemaUtils;
import org.dswarm.persistence.service.schema.SchemaService;
import org.dswarm.persistence.util.DMPPersistenceUtil;

/**
 * @author tgaengler
 */
public class SolrXMLDataSourceConfigGeneratorTest extends GuicedTest {

	private static final Logger LOG = LoggerFactory.getLogger(SolrXMLDataSourceConfigGeneratorTest.class);

	@Test
	public void testSolrXMLDataSourceConfigGenerator() throws IOException, XMLStreamException {

		internalTestSolrXMLDataSourceConfigGenerator("data-config.xml", Optional.of("http://purl.org/ontology/bibo/Document"), Optional.empty(),
				"expected-data-config.xml");
	}

	@Test
	public void testSolrXMLDataSourceConfigGenerator2() throws IOException, XMLStreamException {

		internalTestSolrXMLDataSourceConfigGenerator("data-config2.xml", Optional.empty(),
				Optional.of("http://data.slub-dresden.de/schemas/Schema-5664ba0e-ccb3-4b71-8823-13281490de30/RecordTypes"),
				"expected-data-config2.xml");
	}

	private void internalTestSolrXMLDataSourceConfigGenerator(final String tempDataConfigFileName, final Optional<String> optionalRecordTag,
			final Optional<String> optionalRootAttributePath,
			final String exectedDataConfigFileName) throws IOException, XMLStreamException {

		final SchemaService schemaService = GuicedTest.injector.getInstance(SchemaService.class);

		final Schema fincSolrSchema = schemaService.getObject(SchemaUtils.FINC_SOLR_SCHEMA_UUID);

		final String[] fileNameParts = tempDataConfigFileName.split("\\.");
		final File file = File.createTempFile(fileNameParts[0], fileNameParts[1]);
		final OutputStream fop = new FileOutputStream(file);

		SolrXMLDataSourceConfigGenerator
				.generateSolrXMLDataSourceConfig(fincSolrSchema, optionalRecordTag, optionalRootAttributePath,
						fop);

		final String expectedDataConfig = DMPPersistenceUtil.getResourceAsString(exectedDataConfigFileName);

		Assert.assertNotNull(expectedDataConfig);

		final String actualDataConfig = Files.toString(file, StandardCharsets.UTF_8);

		Assert.assertNotNull(actualDataConfig);

		// do comparison: check for XML similarity
		final Diff xmlDiff = DiffBuilder.compare(Input.fromString(expectedDataConfig))
				.withTest(Input.fromString(actualDataConfig)).ignoreWhitespace().checkForSimilar().build();

		Assert.assertFalse(xmlDiff.hasDifferences());
	}
}
