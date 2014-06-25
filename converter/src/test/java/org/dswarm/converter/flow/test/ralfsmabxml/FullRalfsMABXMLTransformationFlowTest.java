package org.dswarm.converter.flow.test.ralfsmabxml;

import org.junit.Ignore;

/**
 * @author tgaengler
 */
public class FullRalfsMABXMLTransformationFlowTest extends AbstractRalfsMABXMLTransformationFlowTest {

	public FullRalfsMABXMLTransformationFlowTest() {

		super("dmpf-task.json", "dmpf-task-result.json");
	}

	@Ignore
	@Override
	public void testXMLDataResourceEndToEnd() throws Exception {

		// TODO: enable when the time has come
	}
}
