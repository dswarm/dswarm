package org.dswarm.converter.schema.test;

import org.junit.Test;

import org.dswarm.converter.GuicedTest;
import org.dswarm.converter.schema.XMLSchemaParser;

/**
 * @author tgaengler
 */
public class XMLSchemaParserTest extends GuicedTest {

	@Test
	public void testXMLSchemaParserTest() {

		final XMLSchemaParser xmlSchemaParser = GuicedTest.injector.getInstance(XMLSchemaParser.class);
		xmlSchemaParser.parse("mabxml-1.xsd", "datensatz");
	}
}
