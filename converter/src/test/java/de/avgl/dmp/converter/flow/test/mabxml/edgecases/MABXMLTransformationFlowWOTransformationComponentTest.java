package de.avgl.dmp.converter.flow.test.mabxml.edgecases;

import de.avgl.dmp.converter.flow.test.mabxml.AbstractMABXMLTransformationFlowTest;


/**
 * @author tgaengler
 */
public class MABXMLTransformationFlowWOTransformationComponentTest extends AbstractMABXMLTransformationFlowTest {

	public MABXMLTransformationFlowWOTransformationComponentTest() {

		super("task.wo.transformationcomponent.json", "task-result.wo.transformationcomponent.json");
	}
}
