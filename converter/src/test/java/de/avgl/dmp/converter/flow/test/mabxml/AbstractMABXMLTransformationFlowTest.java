package de.avgl.dmp.converter.flow.test.mabxml;

import de.avgl.dmp.converter.flow.test.xml.AbstractXMLTransformationFlowTest;

/**
 * @author tgaengler
 */
public abstract class AbstractMABXMLTransformationFlowTest extends AbstractXMLTransformationFlowTest {

	public AbstractMABXMLTransformationFlowTest(final String taskJSONFileName, final String taskResultJSONFileName) {

		super(taskJSONFileName, taskResultJSONFileName, "datensatz", "http://www.ddb.de/professionell/mabxml/mabxml-1.xsd", "test-mabxml.xml");
	}
}
