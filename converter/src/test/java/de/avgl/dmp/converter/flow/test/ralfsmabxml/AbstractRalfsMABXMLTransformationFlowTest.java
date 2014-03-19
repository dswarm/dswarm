package de.avgl.dmp.converter.flow.test.ralfsmabxml;

import de.avgl.dmp.converter.flow.test.AbstractXMLTransformationFlowTest;


/**
 * @author tgaengler
 */
public abstract class AbstractRalfsMABXMLTransformationFlowTest extends AbstractXMLTransformationFlowTest {

	public AbstractRalfsMABXMLTransformationFlowTest(final String taskJSONFileName, final String taskResultJSONFileName) {

		super(taskJSONFileName, taskResultJSONFileName, "record", null, "dmpf_bsp1.xml");
	}
}
