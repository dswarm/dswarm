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
package org.dswarm.converter.schema.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;

import javax.xml.stream.XMLStreamException;

import com.google.inject.Key;
import com.google.inject.name.Names;
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

		final SchemaService schemaService = GuicedTest.injector.getInstance(SchemaService.class);

		final Schema fincSolrSchema = schemaService.getObject(SchemaUtils.FINC_SOLR_SCHEMA_UUID);

		final String dswarmRoot = GuicedTest.injector.getInstance(Key.get(String.class, Names.named("dswarm.paths.root")));
		final String sep = File.separator;
		final String fileName = "data-config.xml";

		final StringBuilder sb = new StringBuilder();

		sb.append(dswarmRoot).append(sep);

		final String converterModule = "converter";

		if (!dswarmRoot.endsWith(converterModule)) {

			sb.append(converterModule).append(sep);
		}

		sb.append("target").append(sep).append("classes").append(sep).append(fileName);

		final String fullFileName = sb.toString();

		LOG.debug("Solr XML data source config file = '{}'", fullFileName);

		final File file = new File(fullFileName);
		final OutputStream fop = new FileOutputStream(file);

		SolrXMLDataSourceConfigGenerator
				.generateSolrXMLDataSourceConfig(fincSolrSchema, Optional.of("http://purl.org/ontology/bibo/Document"), Optional.<String>empty(),
						fop);

		final String expectedDataConfig = DMPPersistenceUtil.getResourceAsString("expected-data-config.xml");

		Assert.assertNotNull(expectedDataConfig);

		final String actualDataConfig = DMPPersistenceUtil.getResourceAsString(fileName);

		Assert.assertNotNull(actualDataConfig);

		// do comparison: check for XML similarity
		final Diff xmlDiff = DiffBuilder.compare(Input.fromString(expectedDataConfig))
				.withTest(Input.fromString(actualDataConfig)).ignoreWhitespace().checkForSimilar().build();

		Assert.assertFalse(xmlDiff.hasDifferences());
	}
}
