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
package org.dswarm.converter.schema.test;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.dswarm.converter.GuicedTest;
import org.dswarm.converter.schema.XMLSchemaParser;
import org.dswarm.init.util.CmdUtil;
import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.internal.helper.AttributePathHelper;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.ContentSchema;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.model.schema.SchemaAttributePathInstance;
import org.dswarm.persistence.service.MaintainDBService;
import org.dswarm.persistence.service.schema.SchemaService;
import org.dswarm.persistence.service.schema.test.internalmodel.BiboDocumentSchemaBuilder;
import org.dswarm.persistence.service.schema.test.internalmodel.BibrmContractItemSchemaBuilder;
import org.dswarm.persistence.util.DMPPersistenceUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.google.inject.Key;
import com.google.inject.name.Names;

/**
 * @author tgaengler
 */
public class XMLSchemaParserTest extends GuicedTest {

	protected MaintainDBService maintainDBService;


	@Before
	public void prepare() throws Exception {
		GuicedTest.tearDown();
		GuicedTest.startUp();
		initObjects();
		// maintainDBService.initDB();
		maintainDBService.truncateTables();
	}


	@After
	public void tearDown2() throws Exception {
		GuicedTest.tearDown();
		GuicedTest.startUp();
		initObjects();
		// maintainDBService.initDB();
		 maintainDBService.truncateTables();
	}


	protected void initObjects() {
		maintainDBService = GuicedTest.injector.getInstance(MaintainDBService.class);
	}


	@Test
	public void testAttributePathsParsing() throws IOException {

		final XMLSchemaParser xmlSchemaParser = GuicedTest.injector.getInstance(XMLSchemaParser.class);
		final Optional<Set<AttributePathHelper>> optionalAttributePaths = xmlSchemaParser.parseAttributePaths("mabxml-1.xsd", "datensatz");

		Assert.assertTrue(optionalAttributePaths.isPresent());

		final Set<AttributePathHelper> attributePaths = optionalAttributePaths.get();

		final StringBuilder sb = new StringBuilder();

		for (final AttributePathHelper attributePath : attributePaths) {

			sb.append(attributePath.toString()).append("\n");
		}

		final String expectedAttributePaths = DMPPersistenceUtil.getResourceAsString("mabxml-1.attribute_paths.txt");
		final String actualAttributePaths = sb.toString();

		Assert.assertEquals(expectedAttributePaths, actualAttributePaths);
	}


	/**
	 * note: creates the mabxml from the given xml schema file from scratch
	 *
	 * @throws IOException
	 * @throws DMPPersistenceException
	 */
	@Test
	public void testSchemaParsing() throws IOException, DMPPersistenceException {

		final XMLSchemaParser xmlSchemaParser = GuicedTest.injector.getInstance(XMLSchemaParser.class);
		final Optional<Schema> optionalSchema = xmlSchemaParser.parse("mabxml-1.xsd", "datensatz", "mabxml schema");

		Assert.assertTrue(optionalSchema.isPresent());

		final Schema schema = optionalSchema.get();

		final ObjectMapper mapper = GuicedTest.injector.getInstance(ObjectMapper.class);

		final String schemaJSONString = mapper.writeValueAsString(schema);

		System.out.println("'" + schemaJSONString + "'");
	}


	/**
	 * note: creates the mabxml from the given xml schema file from scratch
	 *
	 * @throws IOException
	 * @throws DMPPersistenceException
	 */
	public void testSchemaParsing2() throws IOException, DMPPersistenceException {

		final XMLSchemaParser xmlSchemaParser = GuicedTest.injector.getInstance(XMLSchemaParser.class);
		final Optional<Schema> optionalSchema = xmlSchemaParser.parse("mabxml-1.xsd", "datensatz", "mabxml schema");

		Assert.assertTrue(optionalSchema.isPresent());

		final Schema schema = optionalSchema.get();

		final Map<String, AttributePath> aps = Maps.newHashMap();

		for (final SchemaAttributePathInstance schemaAttributePathInstance : schema.getAttributePaths()) {

			final AttributePath attributePath = schemaAttributePathInstance.getAttributePath();
			aps.put(attributePath.toAttributePath(), attributePath);
		}

		final ContentSchema contentSchema = new ContentSchema();
		contentSchema.setName("mab content schema");

		final AttributePath feldNr = aps
				.get("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#feldhttp://www.ddb.de/professionell/mabxml/mabxml-1.xsd#nr");
		final AttributePath feldInd = aps
				.get("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#feldhttp://www.ddb.de/professionell/mabxml/mabxml-1.xsd#ind");
		final AttributePath id = aps.get("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#id");
		final AttributePath feldValue = aps
				.get("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd#feldhttp://www.w3.org/1999/02/22-rdf-syntax-ns#value");

		contentSchema.addKeyAttributePath(feldNr);
		contentSchema.addKeyAttributePath(feldInd);
		contentSchema.setRecordIdentifierAttributePath(id);
		contentSchema.setValueAttributePath(feldValue);

		schema.setContentSchema(contentSchema);

		final SchemaService schemaService = GuicedTest.injector.getInstance(SchemaService.class);

		schemaService.updateObjectTransactional(schema);
	}


	
//	@Test
	public void buildInitCompleteScript() throws Exception {
		new BibrmContractItemSchemaBuilder().buildSchema();
		new BiboDocumentSchemaBuilder().buildSchema();
		testSchemaParsing2();

		String sep = File.separator;

		String user = readManuallyFromTypeSafeConfig("dswarm.db.metadata.username");
		String pass = readManuallyFromTypeSafeConfig("dswarm.db.metadata.password");
		String db = readManuallyFromTypeSafeConfig("dswarm.db.metadata.schema");
		String outputFile = readManuallyFromTypeSafeConfig("dswarm.paths.root");

		outputFile = outputFile.substring(0, outputFile.lastIndexOf(sep));
		outputFile = outputFile + sep + "persistence" + sep + "src" + sep + "main" + sep + "resources" + sep + "init_internal_schema.sql";

		final String output = outputFile;


		StringBuilder sb = new StringBuilder();
		sb.append( "mysqldump" )
				.append(" -u")
				.append(user)
				.append(" -p")
				.append(pass)
				.append(
						" --no-create-info --no-create-db --skip-triggers --skip-create-options --skip-add-drop-table --skip-lock-tables --skip-add-locks -B ")
				.append(db);
		// .append(" > ")
		// .append( outputFile );

		CmdUtil.runCommand( sb.toString(), output );
	}


	private String readManuallyFromTypeSafeConfig( String key ) {
		return GuicedTest.injector.getInstance(Key.get(String.class, Names.named(key)));
	}

}
