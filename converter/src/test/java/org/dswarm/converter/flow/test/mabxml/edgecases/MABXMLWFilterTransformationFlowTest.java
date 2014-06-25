package org.dswarm.converter.flow.test.mabxml.edgecases;

import org.dswarm.converter.flow.test.mabxml.AbstractMABXMLTransformationFlowTest;

/**
 * @author tgaengler
 */
public class MABXMLWFilterTransformationFlowTest extends AbstractMABXMLTransformationFlowTest {

	public MABXMLWFilterTransformationFlowTest() {

		super("pauls_task.json", "pauls-task-result.json");
	}
}
