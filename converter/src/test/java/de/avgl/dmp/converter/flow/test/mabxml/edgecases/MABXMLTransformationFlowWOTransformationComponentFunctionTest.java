package de.avgl.dmp.converter.flow.test.mabxml.edgecases;

import de.avgl.dmp.converter.flow.test.mabxml.AbstractMABXMLTransformationFlowTest;


/**
 * @author tgaengler
 */
public class MABXMLTransformationFlowWOTransformationComponentFunctionTest extends AbstractMABXMLTransformationFlowTest {

	public MABXMLTransformationFlowWOTransformationComponentFunctionTest() {

		super("task.wo.transformationcomponentfunction.json", "task-result.wo.transformationcomponent.json");
	}
}
