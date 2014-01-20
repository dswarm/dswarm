package de.avgl.dmp.controller.resources.test;

import org.junit.Ignore;

import de.avgl.dmp.controller.resources.test.utils.ClaszesResourceTestUtils;
import de.avgl.dmp.persistence.model.schema.Clasz;
import de.avgl.dmp.persistence.service.schema.ClaszService;

public class ClaszesResourceTest extends BasicResourceTest<ClaszesResourceTestUtils, ClaszService, Clasz, String> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(ClaszesResourceTest.class);

	public ClaszesResourceTest() {

		super(Clasz.class, ClaszService.class, "classes", "clasz.json", new ClaszesResourceTestUtils());
	}

	/**
	 * note: this operation is not supported right now
	 */
	@Ignore
	@Override
	public void testGETObject() throws Exception {

		// super.testGETObject();
	}
	
	@Ignore
	@Override
	public void testDELETEObject() throws Exception {

		//super.testDELETEObject();
	}
}
