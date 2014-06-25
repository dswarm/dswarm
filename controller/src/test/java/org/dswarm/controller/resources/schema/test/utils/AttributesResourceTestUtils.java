package org.dswarm.controller.resources.schema.test.utils;

import org.dswarm.controller.resources.test.utils.AdvancedDMPResourceTestUtils;
import org.dswarm.persistence.model.schema.Attribute;
import org.dswarm.persistence.model.schema.proxy.ProxyAttribute;
import org.dswarm.persistence.service.schema.AttributeService;
import org.dswarm.persistence.service.schema.test.utils.AttributeServiceTestUtils;

public class AttributesResourceTestUtils extends AdvancedDMPResourceTestUtils<AttributeServiceTestUtils, AttributeService, ProxyAttribute, Attribute> {

	public AttributesResourceTestUtils() {

		super("attributes", Attribute.class, AttributeService.class, AttributeServiceTestUtils.class);
	}
}
