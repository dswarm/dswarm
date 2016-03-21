/**
 * Copyright (C) 2013 – 2016 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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

import com.google.inject.Provider;
import org.dswarm.common.types.Tuple;
import org.dswarm.converter.GuicedTest;
import org.dswarm.converter.schema.XMLSchemaParser;
import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.internal.helper.AttributePathHelper;
import org.dswarm.persistence.model.internal.helper.AttributePathHelperHelper;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.ContentSchema;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.model.schema.utils.SchemaUtils;
import org.dswarm.persistence.service.UUIDService;
import org.dswarm.persistence.service.schema.AttributePathService;
import org.dswarm.persistence.service.schema.AttributeService;
import org.dswarm.persistence.service.schema.SchemaAttributePathInstanceService;
import org.dswarm.persistence.service.schema.SchemaService;
import org.dswarm.persistence.util.DMPPersistenceUtil;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

/**
 * @author tgaengler
 */
public class XMLSchemaParserTest extends GuicedTest {

	@Override
	public void prepare() throws Exception {
		GuicedTest.tearDown();
		GuicedTest.startUp();
		initObjects();
		maintainDBService.createTables();
		maintainDBService.truncateTables();
	}

	@Override
	public void tearDown3() throws Exception {
		GuicedTest.tearDown();
		GuicedTest.startUp();
		initObjects();
		maintainDBService.truncateTables();
	}

	@Test
	public void testAttributePathsParsingForMabxml() throws IOException {

		testAttributePathsParsing("mabxml-1.xsd", "datensatz", "mabxml-1.attribute_paths.txt", false);
	}

	@Test
	public void testAttributePathsParsingForMarc21() throws IOException {

		testAttributePathsParsing("MARC21slim.xsd", "record", "marc21_schema_attribute_paths.txt", false);
	}

	@Test
	public void testAttributePathsParsingForPNX() throws IOException {

		testAttributePathsParsing("pnx.xsd", "record", "pnx_schema_-_attribute_paths.txt", false);
	}

	@Test
	public void testAttributePathsParsingForDCElements() throws IOException {

		testAttributePathsParsing("dc.xsd", null, "dc_elements_schema_-_attribute_paths.txt", false);
	}

	@Test
	public void testAttributePathsParsingForOAIDCElements() throws IOException {

		testAttributePathsParsing("oai_dc.xsd", "dc", "oai_dc_elements_schema_-_attribute_paths.txt", true);
	}

	@Test
	public void testAttributePathsParsingForDCTerms() throws IOException {

		testAttributePathsParsing("dcterms.xsd", null, "dc_terms_schema_-_attribute_paths.txt", false);
	}

	@Test
	public void testAttributePathsParsingForOAIPMH() throws IOException {

		testAttributePathsParsing("OAI-PMH.xsd", "record", "oai-pmh_schema_-_attribute_paths.txt", false);
	}

	/**
	 * note: http://europeana.eu/terms is not the original EDM namespace URI; http://www.europeana.eu/schemas/edm/ is the original namespace URI
	 *
	 * @throws IOException
	 */
	@Test
	public void testAttributePathsParsingForEDM() throws IOException {

		testAttributePathsParsing("dd-1168/EDM-COMMON-MAIN.xsd", null, "dd-1168/edm_schema_-_attribute_paths.txt", false);
	}

	@Test
	public void testAttributePathsParsingForOAIPMHPlusOAIDCElements() throws IOException {

		final Map<String, AttributePathHelper> rootAttributePaths = parseAttributePaths("OAI-PMH.xsd", "record", false);

		final String rootAttributePathIdentifier = "http://www.openarchives.org/OAI/2.0/metadata";
		final AttributePathHelper rootAttributePath = rootAttributePaths.get(rootAttributePathIdentifier);

		final Map<String, AttributePathHelper> childAttributePaths = parseAttributePaths("oai_dc.xsd", "dc", true);

		final Map<String, AttributePathHelper> newAttributePaths = composeAttributePaths(rootAttributePaths, rootAttributePath, childAttributePaths);

		compareAttributePaths("oai-pmh_plus_oai_dc_elements_schema_-_attribute_paths.txt", newAttributePaths);
	}

	@Test
	public void testAttributePathsParsingForOAIPMHPlusOAIDCElementsAndEDM() throws IOException {

		final Map<String, AttributePathHelper> rootAttributePaths = parseAttributePaths("OAI-PMH.xsd", "record", false);

		final Map<String, AttributePathHelper> newAttributePaths = buildOAIPMHPlusDCElementsAndEDMAttributePaths(rootAttributePaths);

		compareAttributePaths("oai-pmh_plus_oai_dc_elements_and_edm_schema_-_attribute_paths.txt", newAttributePaths);
	}

	private static Map<String, AttributePathHelper> buildOAIPMHPlusDCElementsAndEDMAttributePaths(final Map<String, AttributePathHelper> rootAttributePaths) {
		final String rootAttributePathIdentifier = "http://www.openarchives.org/OAI/2.0/metadata";
		final AttributePathHelper rootAttributePath = rootAttributePaths.get(rootAttributePathIdentifier);

		final Map<String, AttributePathHelper> childAttributePaths = parseAttributePaths("oai_dc.xsd", "dc", true);

		final String childAttributePathIdentifier = "http://www.openarchives.org/OAI/2.0/oai_dc/dc";
		final AttributePathHelper childAttributePath = childAttributePaths.get(childAttributePathIdentifier);

		final Map<String, AttributePathHelper> childAttributePaths3 = new LinkedHashMap<>();
		childAttributePaths3.put(childAttributePathIdentifier, childAttributePath);

		// insert OAI-PMH DC attribute before EDM, because it should be the root attribute path for EDM
		final Map<String, AttributePathHelper> rootAttributePaths3 = composeAttributePaths(rootAttributePaths, rootAttributePath,
				childAttributePaths3);

		final String rootAttributePathIdentifier2 = "http://www.openarchives.org/OAI/2.0/metadata\u001Ehttp://www.openarchives.org/OAI/2.0/oai_dc/dc";
		final AttributePathHelper rootAttributePath2 = rootAttributePaths3.get(rootAttributePathIdentifier2);

		final Map<String, AttributePathHelper> childAttributePaths2 = parseAttributePaths("dd-1168/EDM-COMMON-MAIN.xsd", null, false);

		// insert EDM schema before, to guarantee the order DCE before EDM
		final Map<String, AttributePathHelper> newAttributePaths2 = composeAttributePaths(rootAttributePaths3, rootAttributePath2,
				childAttributePaths2);

		return composeAttributePaths(newAttributePaths2, rootAttributePath, childAttributePaths);
	}

	@Test
	public void testAttributePathsParsingForOAIPMHPlusDCTerms() throws IOException {

		final Map<String, AttributePathHelper> rootAttributePaths = parseAttributePaths("OAI-PMH.xsd", "record", false);

		final String rootAttributePathIdentifier = "http://www.openarchives.org/OAI/2.0/metadata";
		final AttributePathHelper rootAttributePath = rootAttributePaths.get(rootAttributePathIdentifier);

		final Map<String, AttributePathHelper> childAttributePaths = parseAttributePaths("dcterms.xsd", null, false);

		final Map<String, AttributePathHelper> newAttributePaths = composeAttributePaths(rootAttributePaths, rootAttributePath, childAttributePaths);

		compareAttributePaths("oai-pmh_plus_dc_terms_schema_-_attribute_paths.txt", newAttributePaths);
	}

	@Test
	public void testAttributePathsParsingForOAIPMHPlusMARCXML() throws IOException {

		final Map<String, AttributePathHelper> rootAttributePaths = parseAttributePaths("OAI-PMH.xsd", "record", false);

		final String rootAttributePathIdentifier = "http://www.openarchives.org/OAI/2.0/metadata";
		final AttributePathHelper rootAttributePath = rootAttributePaths.get(rootAttributePathIdentifier);

		// collection - to include "record" attribute into the attribute paths
		final Map<String, AttributePathHelper> childAttributePaths = parseAttributePaths("MARC21slim.xsd", "collection", false);

		final Map<String, AttributePathHelper> newAttributePaths = composeAttributePaths(rootAttributePaths, rootAttributePath, childAttributePaths);

		compareAttributePaths("oai-pmh_plus_marcxml_schema_-_attribute_paths.txt", newAttributePaths);
	}

	public static Schema parseOAIPMHPlusDCElementsSchema(final Optional<Map<String, String>> optionalAttributePathsSAPIUUIDs,
	                                                     final Optional<String> optionalContentSchemaIdentifier) throws DMPPersistenceException {

		final Schema schema = parseOAIPMHPlusXSchema("oai_dc.xsd", "DC Elements", SchemaUtils.OAI_PMH_DC_ELEMENTS_SCHEMA_UUID, "dc", true, optionalAttributePathsSAPIUUIDs);

		final Map<String, AttributePath> aps = SchemaUtils.generateAttributePathMap(schema);

		final String uuid = getOrCreateContentSchemaIdentifier(optionalContentSchemaIdentifier);

		final ContentSchema contentSchema = new ContentSchema(uuid);
		contentSchema.setName("OAI-PMH + DC Elements content schema");

		final AttributePath id = aps
				.get("http://www.openarchives.org/OAI/2.0/header\u001Ehttp://www.openarchives.org/OAI/2.0/identifier\u001Ehttp://www.w3.org/1999/02/22-rdf-syntax-ns#value");

		return fillContentSchemaAndUpdateSchema(contentSchema, id, null, schema);
	}

	public static Schema parseOAIPMHPlusDCElementsSchema() throws DMPPersistenceException {

		return parseOAIPMHPlusDCElementsSchema(Optional.empty(), Optional.empty());
	}

	public static Schema parseOAIPMHPlusDCElementsAndEDMSchema(final Optional<Map<String, String>> optionalAttributePathsSAPIUUIDs,
	                                                           final Optional<String> optionalContentSchemaIdentifier) throws DMPPersistenceException {

		final String childSchemaName = "DC Elements + EDM";
		final Tuple<Schema, Map<String, AttributePathHelper>> result = parseSchemaSeparately("OAI-PMH.xsd", "record",
				SchemaUtils.OAI_PMH_DC_ELEMENTS_AND_EDM_SCHEMA_UUID, "OAI-PMH + " + childSchemaName + " schema");

		final Map<String, AttributePathHelper> rootAttributePaths = result.v2();

		final Map<String, AttributePathHelper> newAttributePaths = buildOAIPMHPlusDCElementsAndEDMAttributePaths(rootAttributePaths);

		final Schema schema = result.v1();
		final Schema updatedSchema = addChildSchemataAttributePathsToSchema(newAttributePaths, schema, optionalAttributePathsSAPIUUIDs);

		final Map<String, AttributePath> aps = SchemaUtils.generateAttributePathMap(updatedSchema);

		final String uuid = getOrCreateContentSchemaIdentifier(optionalContentSchemaIdentifier);

		final ContentSchema contentSchema = new ContentSchema(uuid);
		contentSchema.setName("OAI-PMH + DC Elements + EDM content schema");

		final AttributePath id = aps
				.get("http://www.openarchives.org/OAI/2.0/header\u001Ehttp://www.openarchives.org/OAI/2.0/identifier\u001Ehttp://www.w3.org/1999/02/22-rdf-syntax-ns#value");

		return fillContentSchemaAndUpdateSchema(contentSchema, id, null, updatedSchema);
	}

	public static Schema parseOAIPMHPlusDCElementsAndEDMSchema() throws DMPPersistenceException {

		return parseOAIPMHPlusDCElementsAndEDMSchema(Optional.empty(), Optional.empty());
	}

	public static Schema parseOAIPMHPlusDCTermsSchema(final Optional<Map<String, String>> optionalAttributePathsSAPIUUIDs,
	                                                  final Optional<String> optionalContentSchemaIdentifier) throws DMPPersistenceException {

		final Schema schema = parseOAIPMHPlusXSchema("dcterms.xsd", "DC Terms", SchemaUtils.OAI_PMH_DC_TERMS_SCHEMA_UUID, null, false, optionalAttributePathsSAPIUUIDs);

		final Map<String, AttributePath> aps = SchemaUtils.generateAttributePathMap(schema);

		final String uuid = getOrCreateContentSchemaIdentifier(optionalContentSchemaIdentifier);

		final ContentSchema contentSchema = new ContentSchema(uuid);
		contentSchema.setName("OAI-PMH + DC Terms content schema");

		final AttributePath id = aps
				.get("http://www.openarchives.org/OAI/2.0/header\u001Ehttp://www.openarchives.org/OAI/2.0/identifier\u001Ehttp://www.w3.org/1999/02/22-rdf-syntax-ns#value");

		return fillContentSchemaAndUpdateSchema(contentSchema, id, null, schema);
	}

	public static Schema parseOAIPMHPlusDCTermsSchema() throws DMPPersistenceException {

		return parseOAIPMHPlusDCTermsSchema(Optional.empty(), Optional.empty());
	}

	public static Schema parseOAIPMHPlusMARCXMLSchema(final Optional<Map<String, String>> optionalAttributePathsSAPIUUIDs,
	                                                  final Optional<String> optionalContentSchemaIdentifier) throws DMPPersistenceException {

		// collection - to include "record" attribute into the attribute paths
		final Schema schema = parseOAIPMHPlusXSchema("MARC21slim.xsd", "MARCXML", SchemaUtils.OAI_PMH_MARCXML_SCHEMA_UUID, "collection", false, optionalAttributePathsSAPIUUIDs);

		final Map<String, AttributePath> aps = SchemaUtils.generateAttributePathMap(schema);

		final String uuid = getOrCreateContentSchemaIdentifier(optionalContentSchemaIdentifier);

		final ContentSchema contentSchema = new ContentSchema(uuid);
		contentSchema.setName("OAI-PMH + MARCXML content schema");

		final AttributePath datafieldTag = aps
				.get("http://www.openarchives.org/OAI/2.0/metadata\u001Ehttp://www.loc.gov/MARC21/slim#record\u001Ehttp://www.loc.gov/MARC21/slim#datafield\u001Ehttp://www.loc.gov/MARC21/slim#tag");
		final AttributePath datafieldInd1 = aps
				.get("http://www.openarchives.org/OAI/2.0/metadata\u001Ehttp://www.loc.gov/MARC21/slim#record\u001Ehttp://www.loc.gov/MARC21/slim#datafield\u001Ehttp://www.loc.gov/MARC21/slim#ind1");
		final AttributePath datafieldInd2 = aps
				.get("http://www.openarchives.org/OAI/2.0/metadata\u001Ehttp://www.loc.gov/MARC21/slim#record\u001Ehttp://www.loc.gov/MARC21/slim#datafield\u001Ehttp://www.loc.gov/MARC21/slim#ind2");
		final AttributePath datafieldSubfieldCode = aps
				.get("http://www.openarchives.org/OAI/2.0/metadata\u001Ehttp://www.loc.gov/MARC21/slim#record\u001Ehttp://www.loc.gov/MARC21/slim#datafield\u001Ehttp://www.loc.gov/MARC21/slim#subfield\u001Ehttp://www.loc.gov/MARC21/slim#code");
		final AttributePath id = aps
				.get("http://www.openarchives.org/OAI/2.0/header\u001Ehttp://www.openarchives.org/OAI/2.0/identifier\u001Ehttp://www.w3.org/1999/02/22-rdf-syntax-ns#value");
		final AttributePath datafieldSubfieldValue = aps
				.get("http://www.openarchives.org/OAI/2.0/metadata\u001Ehttp://www.loc.gov/MARC21/slim#record\u001Ehttp://www.loc.gov/MARC21/slim#datafield\u001Ehttp://www.loc.gov/MARC21/slim#subfield\u001Ehttp://www.w3.org/1999/02/22-rdf-syntax-ns#value");

		contentSchema.addKeyAttributePath(datafieldTag);
		contentSchema.addKeyAttributePath(datafieldInd1);
		contentSchema.addKeyAttributePath(datafieldInd2);
		contentSchema.addKeyAttributePath(datafieldSubfieldCode);

		return fillContentSchemaAndUpdateSchema(contentSchema, id, datafieldSubfieldValue, schema);
	}

	public static Schema parseOAIPMHPlusMARCXMLSchema() throws DMPPersistenceException {

		return parseOAIPMHPlusMARCXMLSchema(Optional.empty(), Optional.empty());
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

		final String schemaUUID = UUIDService.getUUID(Schema.class.getSimpleName());

		final java.util.Optional<Schema> optionalSchema = xmlSchemaParser.parse("mabxml-1.xsd", "datensatz", schemaUUID, "mabxml schema");

		Assert.assertTrue(optionalSchema.isPresent());
	}

	/**
	 * note: creates the mabxml from the given xml schema file from scratch (by optionally reutilising existing SAPIs) + adds content schema programmatically
	 *
	 * @throws IOException
	 * @throws DMPPersistenceException
	 */
	public static Schema parseMabxmlSchema(final Optional<Map<String, String>> optionalAttributePathsSAPIUUIDs,
	                                       final Optional<String> optionalContentSchemaIdentifier) throws IOException, DMPPersistenceException {

		final Schema schema = parseSchema("mabxml-1.xsd", "datensatz", SchemaUtils.MABXML_SCHEMA_UUID, "mabxml schema", optionalAttributePathsSAPIUUIDs);

		final Map<String, AttributePath> aps = SchemaUtils.generateAttributePathMap(schema);

		final String uuid = getOrCreateContentSchemaIdentifier(optionalContentSchemaIdentifier);

		final ContentSchema contentSchema = new ContentSchema(uuid);
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

		return fillContentSchemaAndUpdateSchema(contentSchema, id, feldValue, schema);
	}

	/**
	 * note: creates the mabxml from the given xml schema file from scratch + adds content schema programmatically
	 *
	 * @throws IOException
	 * @throws DMPPersistenceException
	 */
	public static Schema parseMabxmlSchema() throws IOException, DMPPersistenceException {

		return parseMabxmlSchema(Optional.empty(), Optional.empty());
	}

	/**
	 * creates the PNX schema from the given XML schema file from scratch (by optionally reutilising existing SAPIs)
	 *
	 * @throws IOException
	 * @throws DMPPersistenceException
	 */
	public static Schema parsePNXSchema(final Optional<Map<String, String>> optionalAttributePathsSAPIUUIDs,
	                                    final Optional<String> optionalContentSchemaIdentifier) throws IOException, DMPPersistenceException {

		final Schema schema = parseSchema("pnx.xsd", "record", SchemaUtils.PNX_SCHEMA_UUID, "pnx schema", optionalAttributePathsSAPIUUIDs);

		final Map<String, AttributePath> aps = SchemaUtils.generateAttributePathMap(schema);

		final String uuid = getOrCreateContentSchemaIdentifier(optionalContentSchemaIdentifier);

		final ContentSchema contentSchema = new ContentSchema(uuid);
		contentSchema.setName("PNX content schema");

		final AttributePath id = aps
				.get("http://www.exlibrisgroup.com/xsd/primo/primo_nm_bib#control\u001Ehttp://www.exlibrisgroup.com/xsd/primo/primo_nm_bib#sourcerecordid\u001Ehttp://www.w3.org/1999/02/22-rdf-syntax-ns#value");

		return fillContentSchemaAndUpdateSchema(contentSchema, id, null, schema);
	}

	/**
	 * creates the PNX schema from the given XML schema file from scratch
	 *
	 * @throws IOException
	 * @throws DMPPersistenceException
	 */
	public static Schema parsePNXSchema() throws IOException, DMPPersistenceException {

		return parsePNXSchema(Optional.empty(), Optional.empty());
	}

	/**
	 * creates the Marc21 schema from the given XML schema file from scratch (by optionally reutilising existing SAPIs)
	 *
	 * @throws IOException
	 * @throws DMPPersistenceException
	 */
	public static Schema parseMarc21Schema(final Optional<Map<String, String>> optionalAttributePathsSAPIUUIDs,
	                                       final Optional<String> optionalContentSchemaIdentifier) throws IOException, DMPPersistenceException {

		final Schema schema = parseSchema("MARC21slim.xsd", "record", SchemaUtils.MARC21_SCHEMA_UUID, "marc21 schema", optionalAttributePathsSAPIUUIDs);

		final Map<String, AttributePath> aps = SchemaUtils.generateAttributePathMap(schema);

		final String uuid = getOrCreateContentSchemaIdentifier(optionalContentSchemaIdentifier);

		final ContentSchema contentSchema = new ContentSchema(uuid);
		contentSchema.setName("marc21 content schema");

		final AttributePath datafieldTag = aps
				.get("http://www.loc.gov/MARC21/slim#datafield\u001Ehttp://www.loc.gov/MARC21/slim#tag");
		final AttributePath datafieldInd1 = aps
				.get("http://www.loc.gov/MARC21/slim#datafield\u001Ehttp://www.loc.gov/MARC21/slim#ind1");
		final AttributePath datafieldInd2 = aps
				.get("http://www.loc.gov/MARC21/slim#datafield\u001Ehttp://www.loc.gov/MARC21/slim#ind2");
		final AttributePath datafieldSubfieldCode = aps
				.get("http://www.loc.gov/MARC21/slim#datafield\u001Ehttp://www.loc.gov/MARC21/slim#subfield\u001Ehttp://www.loc.gov/MARC21/slim#code");
		final AttributePath id = aps.get("http://www.loc.gov/MARC21/slim#id");
		final AttributePath datafieldSubfieldValue = aps
				.get("http://www.loc.gov/MARC21/slim#datafield\u001Ehttp://www.loc.gov/MARC21/slim#subfield\u001Ehttp://www.w3.org/1999/02/22-rdf-syntax-ns#value");

		contentSchema.addKeyAttributePath(datafieldTag);
		contentSchema.addKeyAttributePath(datafieldInd1);
		contentSchema.addKeyAttributePath(datafieldInd2);
		contentSchema.addKeyAttributePath(datafieldSubfieldCode);

		return fillContentSchemaAndUpdateSchema(contentSchema, id, datafieldSubfieldValue, schema);
	}

	/**
	 * creates the Marc21 schema from the given XML schema file from scratch
	 *
	 * @throws IOException
	 * @throws DMPPersistenceException
	 */
	public static Schema parseMarc21Schema() throws IOException, DMPPersistenceException {

		return parseMarc21Schema(Optional.empty(), Optional.empty());
	}

	private static Schema parseSchema(final String xsdFileName,
	                                  final String recordIdentifier,
	                                  final String schemaUUID,
	                                  final String schemaName,
	                                  final Optional<Map<String, String>> optionalAttributePathsSAPIUUIDs) throws DMPPersistenceException {

		final XMLSchemaParser xmlSchemaParser = GuicedTest.injector.getInstance(XMLSchemaParser.class);
		final java.util.Optional<Schema> optionalSchema = xmlSchemaParser.parse(xsdFileName, recordIdentifier, schemaUUID, schemaName, optionalAttributePathsSAPIUUIDs);

		Assert.assertTrue(optionalSchema.isPresent());

		return optionalSchema.get();
	}

	private static Schema parseSchema(final String xsdFileName,
	                                  final String recordIdentifier,
	                                  final String schemaUUID,
	                                  final String schemaName) throws DMPPersistenceException {


		return parseSchema(xsdFileName, recordIdentifier, schemaUUID, schemaName, Optional.empty());
	}

	private static Tuple<Schema, Map<String, AttributePathHelper>> parseSchemaSeparately(final String xsdFileName,
	                                                                                     final String recordIdentifier,
	                                                                                     final String schemaUUID,
	                                                                                     final String schemaName) throws DMPPersistenceException {

		final XMLSchemaParser xmlSchemaParser = GuicedTest.injector.getInstance(XMLSchemaParser.class);
		final java.util.Optional<Tuple<Schema, Map<String, AttributePathHelper>>> optionalResult = xmlSchemaParser.parseSeparately(xsdFileName,
				recordIdentifier, schemaUUID, schemaName);

		Assert.assertTrue(optionalResult.isPresent());

		return optionalResult.get();
	}

	private void testAttributePathsParsing(final String xsdFileName, final String recordIdentifier, final String resultFileName,
	                                       final boolean includeRecordTag) throws IOException {

		final Map<String, AttributePathHelper> attributePaths = parseAttributePaths(xsdFileName, recordIdentifier, includeRecordTag);

		compareAttributePaths(resultFileName, attributePaths);
	}

	private void compareAttributePaths(final String resultFileName, final Map<String, AttributePathHelper> attributePaths) throws IOException {

		final StringBuilder sb = new StringBuilder();

		for (final AttributePathHelper attributePath : attributePaths.values()) {

			sb.append(attributePath.toString()).append("\n");
		}

		final String expectedAttributePaths = DMPPersistenceUtil.getResourceAsString(resultFileName);
		final String actualAttributePaths = sb.toString();

		Assert.assertEquals(expectedAttributePaths, actualAttributePaths);
	}

	private static Map<String, AttributePathHelper> parseAttributePaths(final String xsdFileName, final String recordIdentifier,
	                                                                    final boolean includeRecordTag) {

		final XMLSchemaParser xmlSchemaParser = GuicedTest.injector.getInstance(XMLSchemaParser.class);
		xmlSchemaParser.setIncludeRecordTag(includeRecordTag);
		final Optional<Map<String, AttributePathHelper>> optionalAttributePaths = xmlSchemaParser.parseAttributePathsMap(xsdFileName,
				Optional.ofNullable(recordIdentifier));

		Assert.assertTrue(optionalAttributePaths.isPresent());

		return optionalAttributePaths.get();
	}

	private static Schema fillContentSchemaAndUpdateSchema(final ContentSchema contentSchema, final AttributePath recordIdentifierAP,
	                                                       final AttributePath valueAP, final Schema schema)
			throws DMPPersistenceException {

		contentSchema.setRecordIdentifierAttributePath(recordIdentifierAP);

		if (valueAP != null) {

			contentSchema.setValueAttributePath(valueAP);
		}

		schema.setContentSchema(contentSchema);

		final SchemaService schemaService = GuicedTest.injector.getInstance(SchemaService.class);

		schemaService.updateObjectTransactional(schema);

		return schema;
	}

	private static Map<String, AttributePathHelper> composeAttributePaths(final Map<String, AttributePathHelper> rootAttributePaths,
	                                                                      final AttributePathHelper rootAttributePath,
	                                                                      final Map<String, AttributePathHelper> childAttributePaths) {

		final Set<AttributePathHelper> compoundAttributePaths = new LinkedHashSet<>();

		for (final AttributePathHelper attributePath : childAttributePaths.values()) {

			AttributePathHelperHelper.addAttributePath(attributePath, compoundAttributePaths, rootAttributePath);
		}

		final Map<String, AttributePathHelper> newAttributePaths = new LinkedHashMap<>();
		final String rootAttributePathIdentifier = rootAttributePath.toString();

		for (final Map.Entry<String, AttributePathHelper> rootAttributePathEntry : rootAttributePaths.entrySet()) {

			newAttributePaths.put(rootAttributePathEntry.getKey(), rootAttributePathEntry.getValue());

			if (rootAttributePathEntry.getKey().equals(rootAttributePathIdentifier)) {

				for (final AttributePathHelper attributePath : compoundAttributePaths) {

					newAttributePaths.put(attributePath.toString(), attributePath);
				}
			}
		}

		return newAttributePaths;
	}

	private static Set<AttributePathHelper> convertToSet(final Map<String, AttributePathHelper> attributePathsMap) {

		final Set<AttributePathHelper> attributePaths = new LinkedHashSet<>();

		for (final AttributePathHelper attributePath : attributePathsMap.values()) {

			attributePaths.add(attributePath);
		}

		return attributePaths;
	}

	private static Schema parseOAIPMHPlusXSchema(final String childSchemaFileName,
	                                             final String childSchemaName,
	                                             final String schemaUUID,
	                                             final String childRecordIdentifier,
	                                             final boolean includeRecordTag,
	                                             final Optional<Map<String, String>> optionalAttributePathsSAPIUUIDs) throws DMPPersistenceException {

		final Tuple<Schema, Map<String, AttributePathHelper>> result = parseSchemaSeparately("OAI-PMH.xsd", "record", schemaUUID,
				"OAI-PMH + " + childSchemaName + " schema");

		final Map<String, AttributePathHelper> rootAttributePaths = result.v2();

		final String rootAttributePathIdentifier = "http://www.openarchives.org/OAI/2.0/metadata";
		final AttributePathHelper rootAttributePath = rootAttributePaths.get(rootAttributePathIdentifier);

		final Map<String, AttributePathHelper> childAttributePaths = parseAttributePaths(childSchemaFileName, childRecordIdentifier,
				includeRecordTag);

		final Map<String, AttributePathHelper> newAttributePaths = composeAttributePaths(rootAttributePaths, rootAttributePath, childAttributePaths);

		final Schema schema = result.v1();

		return addChildSchemataAttributePathsToSchema(newAttributePaths, schema, optionalAttributePathsSAPIUUIDs);
	}

	private static Schema parseOAIPMHPlusXSchema(final String childSchemaFileName,
	                                             final String childSchemaName,
	                                             final String schemaUUID,
	                                             final String childRecordIdentifier,
	                                             final boolean includeRecordTag) throws DMPPersistenceException {

		return parseOAIPMHPlusXSchema(childSchemaFileName, childSchemaName, schemaUUID, childRecordIdentifier, includeRecordTag, Optional.empty());
	}

	private static Schema addChildSchemataAttributePathsToSchema(final Map<String, AttributePathHelper> newAttributePaths,
	                                                             final Schema schema,
	                                                             final Optional<Map<String, String>> optionalAttributePathsSAPIUUIDs) throws DMPPersistenceException {

		final Set<AttributePathHelper> attributePaths = convertToSet(newAttributePaths);
		final Provider<AttributePathService> attributePathServiceProvider = GuicedTest.injector.getProvider(AttributePathService.class);
		final Provider<SchemaAttributePathInstanceService> schemaAttributePathInstanceServiceProvider = GuicedTest.injector.getProvider(
				SchemaAttributePathInstanceService.class);
		final Provider<AttributeService> attributeServiceProvider = GuicedTest.injector.getProvider(AttributeService.class);
		final Provider<SchemaService> schemaServiceProvider = GuicedTest.injector.getProvider(SchemaService.class);

		SchemaUtils.addAttributePaths(schema, attributePaths, attributePathServiceProvider, schemaAttributePathInstanceServiceProvider,
				attributeServiceProvider, optionalAttributePathsSAPIUUIDs);

		return SchemaUtils.updateSchema(schema, schemaServiceProvider);
	}

	private static String getOrCreateContentSchemaIdentifier(final Optional<String> optionalContentSchemaIdentifier) {

		return getOrCreateIdentifier(optionalContentSchemaIdentifier, ContentSchema.class.getSimpleName());
	}

	private static String getOrCreateIdentifier(final Optional<String> optionalIdentifier,
	                                            final String entityPrefix) {

		final String uuid;

		if (optionalIdentifier.isPresent()) {

			uuid = optionalIdentifier.get();
		} else {

			uuid = UUIDService.getUUID(entityPrefix);
		}

		return uuid;
	}
}
