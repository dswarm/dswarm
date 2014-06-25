package org.dswarm.controller.resources.schema.test.utils;

import org.dswarm.controller.resources.test.utils.AdvancedDMPResourceTestUtils;
import org.dswarm.persistence.model.schema.Clasz;
import org.dswarm.persistence.model.schema.proxy.ProxyClasz;
import org.dswarm.persistence.service.schema.ClaszService;
import org.dswarm.persistence.service.schema.test.utils.ClaszServiceTestUtils;

public class ClaszesResourceTestUtils extends AdvancedDMPResourceTestUtils<ClaszServiceTestUtils, ClaszService, ProxyClasz, Clasz> {

	public ClaszesResourceTestUtils() {

		super("classes", Clasz.class, ClaszService.class, ClaszServiceTestUtils.class);
	}
}
