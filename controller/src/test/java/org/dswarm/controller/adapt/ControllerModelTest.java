package org.dswarm.controller.adapt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.converter.adapt.ModelTest;

/**
 * @author tgaengler
 */
public class ControllerModelTest extends ModelTest {

	private static final Logger log = LoggerFactory.getLogger(ControllerModelTest.class);

	//@Test
	public void rewriteSchemaJSON() throws Exception {

		rewriteSchemaJSON("schema.json");
	}
}
