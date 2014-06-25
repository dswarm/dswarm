package org.dswarm.converter.flow.test.mabxml.edgecases;

import org.dswarm.converter.flow.test.mabxml.AbstractMABXMLTransformationFlowTest;

/**
 * @author tgaengler
 */
public class MABXMLTransformationFlowWOTransformationComponentFunctionAndParameterMappingsTest extends AbstractMABXMLTransformationFlowTest {

	public MABXMLTransformationFlowWOTransformationComponentFunctionAndParameterMappingsTest() {

		super("task.wo.transformationcomponentfunctionandparametermappings.json", "task-result.wo.transformationcomponent.json");
	}
}
