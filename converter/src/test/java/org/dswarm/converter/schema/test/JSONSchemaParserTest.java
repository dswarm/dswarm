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

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;

import org.dswarm.converter.GuicedTest;
import org.dswarm.converter.schema.AbstractJSONSchemaParser;
import org.dswarm.converter.schema.JSONSchemaParser;
import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.ContentSchema;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.model.schema.utils.SchemaUtils;

/**
 * Created by tgaengler on 26.09.16.
 */
public class JSONSchemaParserTest extends AbstractJSONSchemaParserTest {

	private static final AbstractJSONSchemaParser JSON_SCHEMA_PARSER = GuicedTest.injector.getInstance(JSONSchemaParser.class);

	@Test
	public void testAttributePathsParsingForUBLIntermediateFormat() throws IOException {

		testAttributePathsParsing("is-0.9.json", null, "is-0.9.attribute_paths.txt", false, JSON_SCHEMA_PARSER);
	}

	/**
	 * creates the UBL Intermediate Format schema from the given JSON schema file from scratch (by optionally reutilising existing SAPIs)
	 *
	 * @throws IOException
	 * @throws org.dswarm.persistence.DMPPersistenceException
	 */
	public static Schema parseUBLIntermediateFormatSchema(final Optional<Map<String, String>> optionalAttributePathsSAPIUUIDs,
	                                                      final Optional<String> optionalContentSchemaIdentifier) throws IOException, DMPPersistenceException {

		final JSONSchemaParser schemaParser = GuicedTest.injector.getInstance(JSONSchemaParser.class);
		final Schema schema = parseSchema("is-0.9.json", null, SchemaUtils.UBL_INTERMEDIATE_FORMAT_SCHEMA_UUID, "UBL Intermediate Format schema", optionalAttributePathsSAPIUUIDs, schemaParser);

		final Map<String, AttributePath> aps = SchemaUtils.generateAttributePathMap(schema);

		final String uuid = getOrCreateContentSchemaIdentifier(optionalContentSchemaIdentifier);

		final ContentSchema contentSchema = new ContentSchema(uuid);
		contentSchema.setName("UBL Intermediate Format content schema");

		final AttributePath id = aps
				.get("http://data.slub-dresden.de/schemas/Schema-d06726be-a8e2-412a-b5e7-76cba340108b/finc.record_id");

		return fillContentSchemaAndUpdateSchema(contentSchema, id, null, schema);
	}

	/**
	 * creates the UBL Intermediate Format schema from the given JSON schema file from scratch
	 *
	 * @throws IOException
	 * @throws DMPPersistenceException
	 */
	@Test
	public void parseUBLIntermediateFormatSchema() throws IOException, DMPPersistenceException {

		parseUBLIntermediateFormatSchema(Optional.empty(), Optional.empty());
	}
}
