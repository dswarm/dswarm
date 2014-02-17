package de.avgl.dmp.converter.flow.test;


/**
 * @author tgaengler
 */
public abstract class AbstractMABXMLTransformationFlowTest extends AbstractXMLTransformationFlowTest {

	public AbstractMABXMLTransformationFlowTest(final String taskJSONFileName, final String taskResultJSONFileName) {

		super(taskJSONFileName, taskResultJSONFileName, "datensatz", "http://www.ddb.de/professionell/mabxml/mabxml-1.xsd", "test-mabxml.xml");
	}
}
