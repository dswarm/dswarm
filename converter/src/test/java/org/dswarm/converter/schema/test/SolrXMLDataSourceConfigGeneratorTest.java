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

	@Test
	public void testSolrXMLDataSourceConfigGenerator() throws IOException, XMLStreamException {

		final SchemaService schemaService = GuicedTest.injector.getInstance(SchemaService.class);

		final Schema fincSolrSchema = schemaService.getObject(SchemaUtils.FINC_SOLR_SCHEMA_UUID);

		final String dswarmRoot = GuicedTest.injector.getInstance(Key.get(String.class, Names.named("dswarm.paths.root")));
		final String sep = File.separator;
		final String fileName = "data-config.xml";
		final String fullFileName = dswarmRoot + sep + "converter" + sep + "target" + sep + "classes" + sep + fileName;

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
