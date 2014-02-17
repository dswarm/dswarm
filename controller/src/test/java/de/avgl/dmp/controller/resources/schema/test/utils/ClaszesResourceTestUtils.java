package de.avgl.dmp.controller.resources.schema.test.utils;

import de.avgl.dmp.controller.resources.test.utils.AdvancedDMPResourceTestUtils;
import de.avgl.dmp.persistence.model.schema.Clasz;
import de.avgl.dmp.persistence.model.schema.proxy.ProxyClasz;
import de.avgl.dmp.persistence.service.schema.ClaszService;
import de.avgl.dmp.persistence.service.schema.test.utils.ClaszServiceTestUtils;

public class ClaszesResourceTestUtils extends AdvancedDMPResourceTestUtils<ClaszServiceTestUtils, ClaszService, ProxyClasz, Clasz> {

	public ClaszesResourceTestUtils() {

		super("classes", Clasz.class, ClaszService.class, ClaszServiceTestUtils.class);
	}
}
