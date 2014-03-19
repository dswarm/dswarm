package de.avgl.dmp.converter.flow.test.mabxml.edgecases;

import de.avgl.dmp.converter.flow.test.mabxml.AbstractMABXMLTransformationFlowTest;


/**
 * @author tgaengler
 */
public class MABXMLTransformationFlowWOTransformationComponentFunctionAndParameterMappingsTest extends AbstractMABXMLTransformationFlowTest {

	public MABXMLTransformationFlowWOTransformationComponentFunctionAndParameterMappingsTest() {

		super("task.wo.transformationcomponentfunctionandparametermappings.json", "task-result.wo.transformationcomponent.json");
	}
}
