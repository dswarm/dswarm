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

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.converter.GuicedTest;
import org.dswarm.init.util.CmdUtil;
import org.dswarm.persistence.DMPPersistenceError;
import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.resource.utils.DataModelUtils;
import org.dswarm.persistence.model.schema.Attribute;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.Clasz;
import org.dswarm.persistence.model.schema.ContentSchema;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.model.schema.SchemaAttributePathInstance;
import org.dswarm.persistence.model.schema.utils.SchemaUtils;
import org.dswarm.persistence.service.resource.DataModelService;
import org.dswarm.persistence.service.schema.AttributePathService;
import org.dswarm.persistence.service.schema.AttributeService;
import org.dswarm.persistence.service.schema.ClaszService;
import org.dswarm.persistence.service.schema.SchemaAttributePathInstanceService;
import org.dswarm.persistence.service.schema.SchemaService;
import org.dswarm.persistence.service.schema.test.internalmodel.BiboDocumentSchemaBuilder;
import org.dswarm.persistence.service.schema.test.internalmodel.BibrmContractItemSchemaBuilder;
import org.dswarm.persistence.service.schema.test.utils.AttributeServiceTestUtils;
import org.dswarm.persistence.util.DMPPersistenceUtil;

/**
 * Serves as a preliminary place for triggering the build of a script that populates the database with initially required internal
 * schemata. Note: Uncomment the test 'buildScript' to rebuild the script.
 *
 * @author tgaengler
 * @author polowins
 */
public class BuildInitInternalSchemaScriptTest extends GuicedTest {

	private static final Logger LOG = LoggerFactory.getLogger(BuildInitInternalSchemaScriptTest.class);

	private DataModelService dataModelService;
	private AttributeService attributeService;
	private ClaszService classService;
	private AttributePathService attributePathService;
	private SchemaAttributePathInstanceService schemaAttributePathInstanceService;
	private SchemaService schemaService;

	private final Map<String, String> attributes = new HashMap<>();
	private final Map<String, String> classes = new HashMap<>();
	private final Map<String, String> attributePaths = new HashMap<>();
	private final Map<String, String> sapis = new HashMap<>();
	private final Map<String, String> schemata = new HashMap<>();
	private final Map<String, String> subSchemata = new HashMap<>();

	private final Map<String, Map<String, String>> schemaAttributePathsSAPIUUIDs = new HashMap<>();
	private final Map<String, String> schemaContentSchemaMap = new HashMap<>();

	@Override
	public void prepare() throws Exception {
		init();

		// create inbuilt schemata from existing script
		maintainDBService.initDB();

		// retrieve all data that should be preserved
		preserveData();

		// clean tables
		tearDown3();
		init();

		// replay preserved data
		replayPreservedData();

		// create helpers
		createHelpers();

		//maintainDBService.createTables();
		//maintainDBService.truncateTables();
	}

	private void init() throws Exception {

		GuicedTest.tearDown();
		GuicedTest.startUp();
		initObjects();
	}

	@Override
	public void tearDown3() throws Exception {

		init();

		maintainDBService.truncateTables();
	}

	@Override
	protected void initObjects() {

		super.initObjects();
		injectPersistenceServices();
	}

	private void injectPersistenceServices() {

		dataModelService = GuicedTest.injector.getInstance(DataModelService.class);
		attributeService = GuicedTest.injector.getInstance(AttributeService.class);
		classService = GuicedTest.injector.getInstance(ClaszService.class);
		attributePathService = GuicedTest.injector.getInstance(AttributePathService.class);
		schemaAttributePathInstanceService = GuicedTest.injector.getInstance(SchemaAttributePathInstanceService.class);
		schemaService = GuicedTest.injector.getInstance(SchemaService.class);
	}

	//@Test
	public void buildScript() throws Exception {

		final Schema bibrmContractSchema = new BibrmContractItemSchemaBuilder(Optional.ofNullable(schemaAttributePathsSAPIUUIDs.get(SchemaUtils.BIBRM_CONTRACT_ITEM_SCHEMA_UUID))).buildSchema();
		final Schema biboDocumentSchema = new BiboDocumentSchemaBuilder(Optional.ofNullable(schemaAttributePathsSAPIUUIDs.get(SchemaUtils.BIBO_DOCUMENT_SCHEMA_UUID)), Optional.ofNullable(schemaAttributePathsSAPIUUIDs.get(SchemaUtils.FOAF_SCHEMA_UUID))).buildSchema();
		final Schema mabxmlSchema = XMLSchemaParserTest.parseMabxmlSchema(Optional.ofNullable(schemaAttributePathsSAPIUUIDs.get(SchemaUtils.MABXML_SCHEMA_UUID)), Optional.ofNullable(schemaContentSchemaMap.get(SchemaUtils.MABXML_SCHEMA_UUID)));
		final Schema pnxSchema = XMLSchemaParserTest.parsePNXSchema(Optional.ofNullable(schemaAttributePathsSAPIUUIDs.get(SchemaUtils.PNX_SCHEMA_UUID)), Optional.ofNullable(schemaContentSchemaMap.get(SchemaUtils.PNX_SCHEMA_UUID)));
		final Schema marcxmlSchema = XMLSchemaParserTest.parseMarcXmlSchema(Optional.ofNullable(schemaAttributePathsSAPIUUIDs.get(SchemaUtils.MARCXML_SCHEMA_UUID)), Optional.ofNullable(schemaContentSchemaMap.get(SchemaUtils.MARCXML_SCHEMA_UUID)));
		final Schema picaplusxmlSchema = XMLSchemaParserTest.parsePicaPlusXmlSchema(Optional.ofNullable(schemaAttributePathsSAPIUUIDs.get(SchemaUtils.PICAPLUSXML_SCHEMA_UUID)), Optional.ofNullable(schemaContentSchemaMap.get(SchemaUtils.PICAPLUSXML_SCHEMA_UUID)));

		// reutilize content schema from PICA+ XML schema
		schemaContentSchemaMap.put(SchemaUtils.PICAPLUSXML_GLOBAL_SCHEMA_UUID, picaplusxmlSchema.getContentSchema().getUuid());

		final Schema picaplusxmlGlobalSchema = XMLSchemaParserTest.parsePicaPlusXmlGlobalSchema(Optional.ofNullable(schemaAttributePathsSAPIUUIDs.get(SchemaUtils.PICAPLUSXML_GLOBAL_SCHEMA_UUID)), Optional.ofNullable(schemaContentSchemaMap.get(SchemaUtils.PICAPLUSXML_GLOBAL_SCHEMA_UUID)));
		final Schema fincSolrSchema = SolrSchemaParserTest.parseFincSolrSchema(Optional.ofNullable(schemaAttributePathsSAPIUUIDs.get(SchemaUtils.FINC_SOLR_SCHEMA_UUID)), Optional.ofNullable(schemaContentSchemaMap.get(SchemaUtils.FINC_SOLR_SCHEMA_UUID)));
		final Schema oaipmhDCElementsSchema = XMLSchemaParserTest.parseOAIPMHPlusDCElementsSchema(Optional.ofNullable(schemaAttributePathsSAPIUUIDs.get(SchemaUtils.OAI_PMH_DC_ELEMENTS_SCHEMA_UUID)), Optional.ofNullable(schemaContentSchemaMap.get(SchemaUtils.OAI_PMH_DC_ELEMENTS_SCHEMA_UUID)));
		final Schema oaipmhDCTermsSchema = XMLSchemaParserTest.parseOAIPMHPlusDCTermsSchema(Optional.ofNullable(schemaAttributePathsSAPIUUIDs.get(SchemaUtils.OAI_PMH_DC_TERMS_SCHEMA_UUID)), Optional.ofNullable(schemaContentSchemaMap.get(SchemaUtils.OAI_PMH_DC_TERMS_SCHEMA_UUID)));
		final Schema oaipmhMARCXMLSchema = XMLSchemaParserTest.parseOAIPMHPlusMARCXMLSchema(Optional.ofNullable(schemaAttributePathsSAPIUUIDs.get(SchemaUtils.OAI_PMH_MARCXML_SCHEMA_UUID)), Optional.ofNullable(schemaContentSchemaMap.get(SchemaUtils.OAI_PMH_MARCXML_SCHEMA_UUID)));
		final Schema sru11PICAPlusXMLGlobalSchema = XMLSchemaParserTest.parseSRU11PlusPICAPlusXMLSchema(Optional.ofNullable(schemaAttributePathsSAPIUUIDs.get(SchemaUtils.SRU_11_PICAPLUSXML_GLOBAL_SCHEMA_UUID)), Optional.ofNullable(schemaContentSchemaMap.get(SchemaUtils.SRU_11_PICAPLUSXML_GLOBAL_SCHEMA_UUID)));
		final Schema oaipmhDCElementsAndEDMSchema = XMLSchemaParserTest.parseOAIPMHPlusDCElementsAndEDMSchema(Optional.ofNullable(schemaAttributePathsSAPIUUIDs.get(SchemaUtils.OAI_PMH_DC_ELEMENTS_AND_EDM_SCHEMA_UUID)), Optional.ofNullable(schemaContentSchemaMap.get(SchemaUtils.OAI_PMH_DC_ELEMENTS_AND_EDM_SCHEMA_UUID)));
		final Schema ublIntermediateFormatSchema = JSONSchemaParserTest.parseUBLIntermediateFormatSchema(Optional.ofNullable(schemaAttributePathsSAPIUUIDs.get(SchemaUtils.UBL_INTERMEDIATE_FORMAT_SCHEMA_UUID)), Optional.ofNullable(schemaContentSchemaMap.get(SchemaUtils.UBL_INTERMEDIATE_FORMAT_SCHEMA_UUID)));
		final Schema springerJournalsSchema = XMLSchemaParserTest.parseSpringerJournalsSchema(Optional.ofNullable(schemaAttributePathsSAPIUUIDs.get(SchemaUtils.SPRINGER_JOURNALS_SCHEMA_UUID)));

		final String bibrmContractDM = "Internal Data Model ContractItem";
		final String biboDocumentDM = "Internal Data Model BiboDocument";
		final String mabxmlSchemaDM = "Internal Data Model mabxml";
		final String pnxSchemaDM = "Internal Data Model PNX";
		final String marcxmlSchemaDM = "Internal Data Model MARCXML";
		final String picaplusxmlSchemaDM = "Internal Data Model PICA+ XML";
		final String picaplusxmlGlobalSchemaDM = "Internal Data Model PICA+ XML 'global'";
		// [@tgaengler]: just prevention, but I guess that we also need a (default) data model for the foaf:Person schema (right now)
		final String foafPersonDM = "Internal Data Model foafPerson";
		final String fincSolrSchemaDM = "Internal Data Model finc Solr";
		final String oaipmhDCElementsSchemaDM = "Internal Data Model OAI-PMH + DC Elements";
		final String oaipmhDCTermsSchemaDM = "Internal Data Model OAI-PMH + DC Terms";
		final String oaipmhMARCXMLSchemaDM = "Internal Data Model OAI-PMH + MARCXML";
		final String sru11PICAPlusXMLGlobalSchemaDM = "Internal Data Model SRU 1.1 + PICA+ XML 'global'";
		final String oaipmhDCElementsAndEDMSchemaDM = "Internal Data Model OAI-PMH + DC Elements + EDM";
		final String ublIntermediateFormatSchemaDM = "Internal Data Model UBL Intermediate Format";
		final String springerJournalsSchemaDM = "Internal Data Model Springer Journals";

		final Schema foafPersonSchema = biboDocumentSchema.getAttributePathByURIPath(AttributeServiceTestUtils.DCTERMS_CREATOR).getSubSchema();

		createSchemaDataModel(DataModelUtils.BIBRM_CONTRACT_DATA_MODEL_UUID, bibrmContractDM, bibrmContractDM, bibrmContractSchema);
		createSchemaDataModel(DataModelUtils.BIBO_DOCUMENT_DATA_MODEL_UUID, biboDocumentDM, biboDocumentDM, biboDocumentSchema);
		createSchemaDataModel(DataModelUtils.MABXML_DATA_MODEL_UUID, mabxmlSchemaDM, mabxmlSchemaDM, mabxmlSchema);
		createSchemaDataModel(DataModelUtils.PNX_DATA_MODEL_UUID, pnxSchemaDM, pnxSchemaDM, pnxSchema);
		createSchemaDataModel(DataModelUtils.MARCXML_DATA_MODEL_UUID, marcxmlSchemaDM, marcxmlSchemaDM, marcxmlSchema);
		createSchemaDataModel(DataModelUtils.PICAPLUSXML_DATA_MODEL_UUID, picaplusxmlSchemaDM, picaplusxmlSchemaDM, picaplusxmlSchema);
		createSchemaDataModel(DataModelUtils.PICAPLUSXML_GLOBAL_DATA_MODEL_UUID, picaplusxmlGlobalSchemaDM, picaplusxmlGlobalSchemaDM, picaplusxmlGlobalSchema);
		createSchemaDataModel(DataModelUtils.FOAF_PERSON_DATA_MODEL_UUID, foafPersonDM, foafPersonDM, foafPersonSchema);
		createSchemaDataModel(DataModelUtils.FINC_SOLR_DATA_MODEL_UUID, fincSolrSchemaDM, fincSolrSchemaDM, fincSolrSchema);
		createSchemaDataModel(DataModelUtils.OAI_PMH_DC_ELEMENTS_DATA_MODEL_UUID, oaipmhDCElementsSchemaDM, oaipmhDCElementsSchemaDM,
				oaipmhDCElementsSchema);
		createSchemaDataModel(DataModelUtils.OAI_PMH_DC_TERMS_DATA_MODEL_UUID, oaipmhDCTermsSchemaDM, oaipmhDCTermsSchemaDM, oaipmhDCTermsSchema);
		createSchemaDataModel(DataModelUtils.OAI_PMH_MARCXML_DATA_MODEL_UUID, oaipmhMARCXMLSchemaDM, oaipmhMARCXMLSchemaDM, oaipmhMARCXMLSchema);
		createSchemaDataModel(DataModelUtils.SRU_11_PICAPLUSXML_GLOBAL_DATA_MODEL_UUID, sru11PICAPlusXMLGlobalSchemaDM, sru11PICAPlusXMLGlobalSchemaDM, sru11PICAPlusXMLGlobalSchema);
		createSchemaDataModel(DataModelUtils.OAI_PMH_DC_ELEMENTS_AND_EDM_DATA_MODEL_UUID, oaipmhDCElementsAndEDMSchemaDM, oaipmhDCElementsAndEDMSchemaDM,
				oaipmhDCElementsAndEDMSchema);
		createSchemaDataModel(DataModelUtils.UBL_INTERMEDIATE_FORMAT_DATA_MODEL_UUID, ublIntermediateFormatSchemaDM, ublIntermediateFormatSchemaDM, ublIntermediateFormatSchema);
		createSchemaDataModel(DataModelUtils.SPRINGER_JOURNALS_DATA_MODEL_UUID, springerJournalsSchemaDM, springerJournalsSchemaDM, springerJournalsSchema);

		// clean up DB from deprecated entities
		cleanUpDB();

		final String sep = File.separator;

		final String user = readManuallyFromTypeSafeConfig("dswarm.db.metadata.username");
		final String pass = readManuallyFromTypeSafeConfig("dswarm.db.metadata.password");
		final String db = readManuallyFromTypeSafeConfig("dswarm.db.metadata.schema");
		String outputFile = readManuallyFromTypeSafeConfig("dswarm.paths.root");

		//outputFile = outputFile.substring(0, outputFile.lastIndexOf(sep));
		outputFile = outputFile + sep + "persistence" + sep + "src" + sep + "main" + sep + "resources" + sep + "init_internal_schema.sql";

		final String output = outputFile;

		final StringBuilder sb = new StringBuilder();
		sb.append("mysqldump")
				.append(" -u")
				.append(user)
				.append(" -p")
				.append(pass)
				.append(" --no-create-info --no-create-db --skip-triggers --skip-create-options --skip-add-drop-table --skip-lock-tables --skip-add-locks -B ")
				.append(db);

		// didn't work in ubuntu right now
		CmdUtil.runCommand(sb.toString(), output);
	}

	private void createSchemaDataModel(final String uuid, final String name, final String description, final Schema schema)
			throws DMPPersistenceException {

		final DataModel dataModel = new DataModel(uuid);
		dataModel.setName(name);
		dataModel.setDescription(description);
		dataModel.setSchema(schema);

		dataModelService.createObjectTransactional(dataModel);
	}

	private void preserveData() {

		preserveAttributes();
		preserveClasses();
		preserveAttributePaths();
		preserveSAPIs();
		preserveSchemata();
	}

	private void preserveAttributes() {

		final List<Attribute> attributeList = attributeService.getObjects();

		attributeList.forEach(attribute -> {

			try {

				attributes.put(attribute.getUuid(), DMPPersistenceUtil.getJSONObjectMapper().writeValueAsString(attribute));
			} catch (final JsonProcessingException e) {

				DMPPersistenceError.wrap(new DMPPersistenceException("something went wrong", e));
			}
		});
	}

	private void preserveClasses() {

		final List<Clasz> claszList = classService.getObjects();

		claszList.forEach(clasz -> {

			try {

				classes.put(clasz.getUuid(), DMPPersistenceUtil.getJSONObjectMapper().writeValueAsString(clasz));
			} catch (final JsonProcessingException e) {

				DMPPersistenceError.wrap(new DMPPersistenceException("something went wrong", e));
			}
		});
	}

	private void preserveAttributePaths() {

		final List<AttributePath> attributePathList = attributePathService.getObjects();

		attributePathList.forEach(attributePath -> {

			try {

				attributePaths.put(attributePath.getUuid(), DMPPersistenceUtil.getJSONObjectMapper().writeValueAsString(attributePath));
			} catch (final JsonProcessingException e) {

				DMPPersistenceError.wrap(new DMPPersistenceException("something went wrong", e));
			}
		});
	}

	private void preserveSAPIs() {

		final List<SchemaAttributePathInstance> sapiList = schemaAttributePathInstanceService.getObjects();

		sapiList.forEach(sapi -> {

			try {

				final Schema subSchema = sapi.getSubSchema();

				if (subSchema != null) {

					subSchemata.put(subSchema.getUuid(), DMPPersistenceUtil.getJSONObjectMapper().writeValueAsString(subSchema));
				}

				sapis.put(sapi.getUuid(), DMPPersistenceUtil.getJSONObjectMapper().writeValueAsString(sapi));
			} catch (final JsonProcessingException e) {

				DMPPersistenceError.wrap(new DMPPersistenceException("something went wrong", e));
			}
		});
	}

	private void preserveSchemata() {

		final List<Schema> schemaList = schemaService.getObjects();

		schemaList.forEach(schema -> {

			try {

				schemata.put(schema.getUuid(), DMPPersistenceUtil.getJSONObjectMapper().writeValueAsString(schema));
			} catch (final JsonProcessingException e) {

				DMPPersistenceError.wrap(new DMPPersistenceException("something went wrong", e));
			}
		});
	}

	private void replayPreservedData() {

		replayAttributes();
		replayClasses();
		replayAttributePaths();
		replaySubSchemata();
		replaySAPIs();
	}

	private void replayAttributes() {

		attributes.forEach((uuid, attributeString) -> {

			try {

				final Attribute attribute = DMPPersistenceUtil.getJSONObjectMapper().readValue(attributeString, Attribute.class);

				attributeService.createObjectTransactional(attribute);
			} catch (final IOException | DMPPersistenceException e) {

				DMPPersistenceError.wrap(new DMPPersistenceException("something went wrong", e));
			}
		});
	}

	private void replayClasses() {

		classes.forEach((uuid, claszString) -> {

			try {

				final Clasz clasz = DMPPersistenceUtil.getJSONObjectMapper().readValue(claszString, Clasz.class);

				classService.createObjectTransactional(clasz);
			} catch (final IOException | DMPPersistenceException e) {

				DMPPersistenceError.wrap(new DMPPersistenceException("something went wrong", e));
			}
		});
	}

	private void replayAttributePaths() {

		attributePaths.forEach((uuid, attributePathString) -> {

			try {

				final AttributePath attributePath = DMPPersistenceUtil.getJSONObjectMapper().readValue(attributePathString, AttributePath.class);

				attributePathService.createObjectTransactional(attributePath);
			} catch (final IOException | DMPPersistenceException e) {

				DMPPersistenceError.wrap(new DMPPersistenceException("something went wrong", e));
			}
		});
	}

	private void replaySubSchemata() {

		subSchemata.forEach((uuid, subSchemaString) -> {

			try {

				final Schema subSchema = DMPPersistenceUtil.getJSONObjectMapper().readValue(subSchemaString, Schema.class);

				subSchema.setAttributePaths(null);
				subSchema.setRecordClass(null);

				schemaService.createObjectTransactional(subSchema);
			} catch (final IOException | DMPPersistenceException e) {

				DMPPersistenceError.wrap(new DMPPersistenceException("something went wrong", e));
			}
		});
	}

	private void replaySAPIs() {

		sapis.forEach((uuid, sapiString) -> {

			try {

				final SchemaAttributePathInstance sapi = DMPPersistenceUtil.getJSONObjectMapper().readValue(sapiString, SchemaAttributePathInstance.class);

				schemaAttributePathInstanceService.createObjectTransactional(sapi);
			} catch (final IOException | DMPPersistenceException e) {

				DMPPersistenceError.wrap(new DMPPersistenceException("something went wrong", e));
			}
		});
	}

	private void createHelpers() {

		schemata.forEach((uuid, schemaString) -> {

			try {

				final Schema schema = DMPPersistenceUtil.getJSONObjectMapper().readValue(schemaString, Schema.class);

				final String schemaUuid = schema.getUuid();

				final Map<String, String> attributePathsSAPIUUIDs = new HashMap<>();

				schema.getAttributePaths().forEach(sapi -> attributePathsSAPIUUIDs.put(sapi.getAttributePath().toAttributePath(), sapi.getUuid()));

				schemaAttributePathsSAPIUUIDs.put(schemaUuid, attributePathsSAPIUUIDs);

				final ContentSchema contentSchema = schema.getContentSchema();

				if (contentSchema != null) {

					schemaContentSchemaMap.put(schemaUuid, contentSchema.getUuid());
				}
			} catch (final IOException e) {

				DMPPersistenceError.wrap(new DMPPersistenceException("something went wrong", e));
			}
		});
	}

	private void cleanUpDB() {

		schemata.forEach((uuid, schemaString) -> {

			try {

				final Schema schema = DMPPersistenceUtil.getJSONObjectMapper().readValue(schemaString, Schema.class);

				cleanUpSAPIs(schema);
			} catch (final IOException e) {

				DMPPersistenceError.wrap(new DMPPersistenceException("something went wrong", e));
			}
		});
	}

	private void cleanUpSAPIs(final Schema oldSchema) {

		final Optional<Collection<SchemaAttributePathInstance>> optionalOldSAPIs = Optional.ofNullable(oldSchema.getAttributePaths());

		if (!optionalOldSAPIs.isPresent()) {

			return;
		}

		final Collection<SchemaAttributePathInstance> oldSAPIs = optionalOldSAPIs.get();

		final Optional<Schema> optionalNewSchema = Optional.ofNullable(schemaService.getObject(oldSchema.getUuid()));

		if (!optionalNewSchema.isPresent()) {

			// remove all SAPIs, since schema does not exist anymore
			oldSAPIs.forEach(oldSAPI -> cleanUpSAPI(oldSAPI));

			return;
		}

		final Schema newSchema = optionalNewSchema.get();

		oldSAPIs.forEach(oldSAPI -> {

			final String oldSAPIUuid = oldSAPI.getUuid();

			final Optional<SchemaAttributePathInstance> optionalNewSAPI = Optional.ofNullable(newSchema.getAttributePath(oldSAPIUuid));

			if (!optionalNewSAPI.isPresent()) {

				// remove old SAPI, since this one does not exist anymore + try to remove SAPI parts as well
				cleanUpSAPI(oldSAPI);
			}
		});
	}

	private void cleanUpSAPI(final SchemaAttributePathInstance oldSAPI) {

		schemaAttributePathInstanceService.deleteObject(oldSAPI.getUuid());

		cleanUpSAPIParts(oldSAPI);
	}

	private void cleanUpSAPIParts(final SchemaAttributePathInstance oldSAPI) {

		cleanUpSubSchema(oldSAPI);
		cleanUpSAPIAttributePath(oldSAPI);
	}

	private void cleanUpSubSchema(final SchemaAttributePathInstance oldSAPI) {

		final Optional<Schema> optionalOldSubSchema = Optional.ofNullable(oldSAPI.getSubSchema());

		try {

			cleanUpSchema(optionalOldSubSchema);
		} catch (final Exception e) {

			LOG.debug("could not remove SAPI sub schema (maybe because it is utilised somewhere else as well)", e);
		}
	}

	private void cleanUpSAPIAttributePath(final SchemaAttributePathInstance oldSAPI) {

		final Optional<AttributePath> optionalOldAttributePath = Optional.ofNullable(oldSAPI.getAttributePath());

		try {

			cleanUpAttributePath(optionalOldAttributePath);
		} catch (final Exception e) {

			LOG.debug("could not remove SAPI attribute path (maybe because it is utilised somewhere else as well)", e);
		}


	}

	private void cleanUpSchema(final Optional<Schema> optionalOldSchema) {

		if (!optionalOldSchema.isPresent()) {

			return;
		}

		final Schema oldSchema = optionalOldSchema.get();

		cleanUpSchema(oldSchema);
	}

	private void cleanUpAttributePath(final Optional<AttributePath> optionalOldAttributePath) {

		if (!optionalOldAttributePath.isPresent()) {

			return;
		}

		final AttributePath oldattributePath = optionalOldAttributePath.get();

		attributePathService.deleteObject(oldattributePath.getUuid());

		cleanUpAttributePathParts(oldattributePath);
	}

	private void cleanUpSchema(final Schema oldSchema) {

		schemaService.deleteObject(oldSchema.getUuid());

		cleanUpSchemaParts(oldSchema);
	}

	private void cleanUpSchemaParts(final Schema oldSchema) {

		cleanUPSAPIs(oldSchema);
		cleanUpRecordClass(oldSchema);
	}

	private void cleanUPSAPIs(final Schema oldSchema) {

		final Optional<Set<SchemaAttributePathInstance>> optionalOldSAPIs = Optional.ofNullable(oldSchema.getUniqueAttributePaths());

		if (!optionalOldSAPIs.isPresent()) {

			return;
		}

		final Set<SchemaAttributePathInstance> oldSAPIs = optionalOldSAPIs.get();

		oldSAPIs.forEach(oldSAPI -> cleanUpSAPI(oldSAPI));
	}

	private void cleanUpRecordClass(final Schema oldSchema) {

		final Optional<Clasz> optionalOldRecordClass = Optional.ofNullable(oldSchema.getRecordClass());

		try {

			cleanUpClass(optionalOldRecordClass);
		} catch (final Exception e) {

			LOG.debug("could not remove record class from schema (maybe it is utilised somewhere else as well)", e);
		}
	}

	private void cleanUpClass(final Optional<Clasz> optionalClass) {

		if (!optionalClass.isPresent()) {

			return;
		}

		final Clasz clasz = optionalClass.get();

		classService.deleteObject(clasz.getUuid());
	}

	private void cleanUpAttributePathParts(final AttributePath oldattributePath) {

		final Optional<Set<Attribute>> optionalOldAttributes = Optional.ofNullable(oldattributePath.getAttributes());

		if (!optionalOldAttributes.isPresent()) {

			return;
		}

		final Set<Attribute> oldAttributes = optionalOldAttributes.get();

		oldAttributes.forEach(oldAttribute -> {

			try {

				cleanUpAttribute(oldAttribute);
			} catch (final Exception e) {

				LOG.debug("could not remove attribute from attribute path (maybe it is utilised somewhere else as well)", e);
			}
		});
	}

	private void cleanUpAttribute(final Attribute oldAttribute) {

		attributeService.deleteObject(oldAttribute.getUuid());
	}
}
