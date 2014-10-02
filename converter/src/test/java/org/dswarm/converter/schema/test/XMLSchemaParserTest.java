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

import java.io.IOException;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import junit.framework.Assert;
import org.junit.Test;

import org.dswarm.converter.GuicedTest;
import org.dswarm.converter.schema.XMLSchemaParser;
import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.internal.helper.AttributePathHelper;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.util.DMPPersistenceUtil;

/**
 * @author tgaengler
 */
public class XMLSchemaParserTest extends GuicedTest {

	@Test
	public void testAttributePathsParsing() throws IOException {

		final XMLSchemaParser xmlSchemaParser = GuicedTest.injector.getInstance(XMLSchemaParser.class);
		final Optional<Set<AttributePathHelper>> optionalAttributePaths = xmlSchemaParser.parseAttributePaths("mabxml-1.xsd", "datensatz");

		Assert.assertTrue(optionalAttributePaths.isPresent());

		final Set<AttributePathHelper> attributePaths = optionalAttributePaths.get();

		final StringBuilder sb = new StringBuilder();

		for(final AttributePathHelper attributePath : attributePaths) {

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
	//@Test
	public void testSchemaParsing() throws IOException, DMPPersistenceException {

		final XMLSchemaParser xmlSchemaParser = GuicedTest.injector.getInstance(XMLSchemaParser.class);
		final Optional<Schema> optionalSchema = xmlSchemaParser.parse("mabxml-1.xsd", "datensatz", "mabxml schema");

		Assert.assertTrue(optionalSchema.isPresent());

		final Schema schema = optionalSchema.get();

		final ObjectMapper mapper = GuicedTest.injector.getInstance(ObjectMapper.class);

		final String schemaJSONString = mapper.writeValueAsString(schema);

		System.out.println("'" + schemaJSONString + "'");
	}
}
