package org.dswarm.converter.schema.test;

import java.io.IOException;
import java.util.Set;

import com.google.common.base.Optional;
import junit.framework.Assert;
import org.junit.Test;

import org.dswarm.converter.GuicedTest;
import org.dswarm.converter.schema.XMLSchemaParser;
import org.dswarm.persistence.model.internal.helper.AttributePathHelper;
import org.dswarm.persistence.util.DMPPersistenceUtil;

/**
 * @author tgaengler
 */
public class XMLSchemaParserTest extends GuicedTest {

	@Test
	public void testAttributePathParsing() throws IOException {

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
}
