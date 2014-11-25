package org.dswarm.controller.adapt;

import org.junit.Test;
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

	//@Test
	public void rewriteProjectJSONs() throws Exception {

		rewriteProjectJSON("project_to_remove_mapping_from_with_dummy_IDs.json");
		rewriteProjectJSON("project.w.new.entities.onepersistentmappingattributepathinstance.json");
		rewriteProjectJSON("project.w.new.entities.json");
	}

	@Test
	public void rewriteTaskJSONs() throws Exception {

		rewriteTaskJSON("task.csv.json");
	}
}
