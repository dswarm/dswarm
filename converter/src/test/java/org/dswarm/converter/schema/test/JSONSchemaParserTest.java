package org.dswarm.converter.schema.test;

import java.io.IOException;

import org.junit.Test;

import org.dswarm.converter.GuicedTest;
import org.dswarm.converter.schema.AbstractJSONSchemaParser;
import org.dswarm.converter.schema.JSONSchemaParser;

/**
 * Created by tgaengler on 26.09.16.
 */
public class JSONSchemaParserTest extends AbstractJSONSchemaParserTest {

	private static final AbstractJSONSchemaParser JSON_SCHEMA_PARSER = GuicedTest.injector.getInstance(JSONSchemaParser.class);

	@Test
	public void testAttributePathsParsingForUBLIntermediateFormat() throws IOException {

		testAttributePathsParsing("is-0.9.json", null, "is-0.9.attribute_paths.txt", false, JSON_SCHEMA_PARSER);
	}
}
