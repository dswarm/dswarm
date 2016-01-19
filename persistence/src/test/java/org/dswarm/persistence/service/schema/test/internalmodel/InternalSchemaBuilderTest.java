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
package org.dswarm.persistence.service.schema.test.internalmodel;

import java.util.Iterator;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import org.dswarm.persistence.GuicedTest;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.model.schema.SchemaAttributePathInstance;

public class InternalSchemaBuilderTest extends GuicedTest {

	// private static final Logger LOG = LoggerFactory.getLogger(InternalSchemaBuilderTest.class);

	private final ObjectMapper objectMapper = GuicedTest.injector.getInstance(ObjectMapper.class);

	// private static final String NL = System.lineSeparator();

	@Override
	public void prepare() throws Exception {

		super.prepare();
		maintainDBService.truncateTables();
	}

	//@Ignore
	@Test
	public void buildInternalSchema() throws Exception {
		buildSchema(new BiboDocumentSchemaBuilder());
	}

	//@Ignore
	@Test
	public void buildERMSchema() throws Exception {
		buildSchema(new BibrmContractItemSchemaBuilder());
	}

	private void buildSchema(final SchemaBuilder schemaBuilder) throws Exception {

		final Schema schema = schemaBuilder.buildSchema();

		printSchemaAsJSON(schema);
		printSchemaAsText(schema);
		printSchemaAsPrefixPaths(schemaBuilder);
	}

	private void printSchemaAsPrefixPaths(final SchemaBuilder schemaBuilder) {

		System.out.println("***************************************************************");
		System.out.println("Schema as prefix paths (does currently not print sub-schemata!)");
		System.out.println("***************************************************************");
		System.out.println(schemaBuilder.getPrefixPaths());
		System.out.println("****************************************************");
	}

	public void printSchemaAsJSON(final Schema schema) {

		String json = null;

		try {

			json = objectMapper.writeValueAsString(schema);
		} catch (final JsonProcessingException e) {

			e.printStackTrace();
		}

		System.out.println("****************************************************");
		System.out.println("Schema as json: " + json);
		System.out.println("****************************************************");

	}

	public static void printSchemaAsText(final Schema schema) {

		System.out.println("****************************************************");
		System.out.println("Schema for " + schema.getRecordClass().getUri());
		System.out.println("****************************************************");

		final Set<SchemaAttributePathInstance> pathSet = schema.getUniqueAttributePaths();

		for (final Iterator<SchemaAttributePathInstance> iterator = pathSet.iterator(); iterator.hasNext(); ) {

			final SchemaAttributePathInstance attributePathInstance = iterator.next();
			InternalSchemaBuilderTest.printAttributePathAsText(attributePathInstance);
		}

		System.out.println("****************************************************");

	}

	public static void printAttributePathAsText(final SchemaAttributePathInstance path) {

		System.out.println(path.getAttributePath().toAttributePath().replace("", " :: "));

		if (null != path.getSubSchema()) {
			System.out.println("****************************************************");
			System.out.println("*with sub-schema:***********************************");
			printSchemaAsText(path.getSubSchema());
		}
	}

}
