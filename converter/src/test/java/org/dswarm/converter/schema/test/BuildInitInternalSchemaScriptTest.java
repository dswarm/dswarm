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

import com.google.inject.Key;
import com.google.inject.name.Names;

import org.dswarm.converter.GuicedTest;
import org.dswarm.init.util.CmdUtil;
import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.service.resource.DataModelService;
import org.dswarm.persistence.service.schema.test.internalmodel.BiboDocumentSchemaBuilder;
import org.dswarm.persistence.service.schema.test.internalmodel.BibrmContractItemSchemaBuilder;
import org.dswarm.persistence.service.schema.test.utils.AttributeServiceTestUtils;

/**
 * Serves as a preliminary place for triggering the build of a script that populates the database with initially required internal
 * schemata. Note: Uncomment the test 'buildScript' to rebuild the script.
 *
 * @author tgaengler
 * @author polowins
 */
public class BuildInitInternalSchemaScriptTest extends GuicedTest {

	public static final String BIBRM_CONTRACT_DATA_MODEL_UUID = "DataModel-7e170c22-1371-4836-9a09-515524a1a8d5";
	public static final String BIBO_DOCUMENT_DATA_MODEL_UUID  = "DataModel-cf998267-392a-4d87-a33a-88dd1bffb016";
	public static final String MABXML_DATA_MODEL_UUID         = "DataModel-4f399d11-81ae-45af-b2f4-645aa177ab85";
	public static final String FOAF_PERSON_DATA_MODEL_UUID    = "DataModel-23451d9d-adf6-4352-90f8-4f17cccf5d36";

	private DataModelService dataModelService;

	@Override
	public void prepare() throws Exception {
		GuicedTest.tearDown();
		GuicedTest.startUp();
		initObjects();
		maintainDBService.createTables();
		maintainDBService.truncateTables();
	}

	@Override public void tearDown3() throws Exception {

		GuicedTest.tearDown();
		GuicedTest.startUp();
		initObjects();
		maintainDBService.truncateTables();
	}

	@Override
	protected void initObjects() {

		super.initObjects();
		dataModelService = GuicedTest.injector.getInstance(DataModelService.class);
	}

	//@Test
	public void buildScript() throws Exception {

		final Schema bibrmContractSchema = new BibrmContractItemSchemaBuilder().buildSchema();
		final Schema biboDocumentSchema = new BiboDocumentSchemaBuilder().buildSchema();
		final Schema mabxmlSchema = XMLSchemaParserTest.testSchemaParsing2();

		final String bibrmContractDM = "Internal Data Model ContractItem";
		final String biboDocumentDM = "Internal Data Model BiboDocument";
		final String mabxmlSchemaDM = "Internal Data Model mabxml";
		// [@tgaengler]: just prevention, but I guess that we also need a (default) data model for the foaf:Person schema (right now)
		final String foafPersonDM = "Internal Data Model foafPerson";

		final Schema foafPersonSchema = biboDocumentSchema.getAttributePathByURIPath(AttributeServiceTestUtils.DCTERMS_CREATOR).getSubSchema();

		createSchemaDataModel(BIBRM_CONTRACT_DATA_MODEL_UUID, bibrmContractDM, bibrmContractDM, bibrmContractSchema);
		createSchemaDataModel(BIBO_DOCUMENT_DATA_MODEL_UUID, biboDocumentDM, biboDocumentDM, biboDocumentSchema);
		createSchemaDataModel(MABXML_DATA_MODEL_UUID, mabxmlSchemaDM, mabxmlSchemaDM, mabxmlSchema);
		createSchemaDataModel(FOAF_PERSON_DATA_MODEL_UUID, foafPersonDM, foafPersonDM, foafPersonSchema);

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

	private String readManuallyFromTypeSafeConfig(final String key) {
		return GuicedTest.injector.getInstance(Key.get(String.class, Names.named(key)));
	}

	private void createSchemaDataModel(final String uuid, final String name, final String description, final Schema schema)
			throws DMPPersistenceException {

		final DataModel dataModel = new DataModel(uuid);
		dataModel.setName(name);
		dataModel.setDescription(description);
		dataModel.setSchema(schema);

		dataModelService.createObjectTransactional(dataModel);
	}

}
