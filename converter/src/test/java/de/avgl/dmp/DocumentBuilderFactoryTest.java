package org.dswarm;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.junit.Assert;
import org.junit.Test;

public class DocumentBuilderFactoryTest {

	@Test
	public void testDocumentBuilderFactory() throws Exception {
		final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		final Schema schema = schemaFactory.newSchema();

		System.out.println(builderFactory.getClass().getName());
		try {
			builderFactory.setSchema(schema);
		} catch (final UnsupportedOperationException e) {
			Assert.fail();
		}
		Assert.assertNotNull(builderFactory.getSchema());
		Assert.assertEquals(builderFactory.getSchema(), schema);
	}
}
