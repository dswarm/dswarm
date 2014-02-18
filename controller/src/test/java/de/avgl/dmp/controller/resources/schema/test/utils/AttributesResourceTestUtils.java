package de.avgl.dmp.controller.resources.schema.test.utils;

import de.avgl.dmp.controller.resources.test.utils.AdvancedDMPResourceTestUtils;
import de.avgl.dmp.persistence.model.schema.Attribute;
import de.avgl.dmp.persistence.model.schema.proxy.ProxyAttribute;
import de.avgl.dmp.persistence.service.schema.AttributeService;
import de.avgl.dmp.persistence.service.schema.test.utils.AttributeServiceTestUtils;

public class AttributesResourceTestUtils extends AdvancedDMPResourceTestUtils<AttributeServiceTestUtils, AttributeService, ProxyAttribute, Attribute> {

	public AttributesResourceTestUtils() {

		super("attributes", Attribute.class, AttributeService.class, AttributeServiceTestUtils.class);
	}
}
