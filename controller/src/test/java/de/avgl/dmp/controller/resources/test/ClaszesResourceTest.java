package de.avgl.dmp.controller.resources.test;

import de.avgl.dmp.controller.resources.test.utils.ClaszesResourceTestUtils;
import de.avgl.dmp.persistence.model.schema.Clasz;
import de.avgl.dmp.persistence.service.schema.ClaszService;

public class ClaszesResourceTest extends BasicResourceTest<ClaszesResourceTestUtils, ClaszService, Clasz, Long> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(ClaszesResourceTest.class);

	public ClaszesResourceTest() {

		super(Clasz.class, ClaszService.class, "classes", "clasz.json", new ClaszesResourceTestUtils());
	}
}
