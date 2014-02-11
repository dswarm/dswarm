package de.avgl.dmp.controller.resources.schema.test.utils;

import de.avgl.dmp.controller.resources.test.utils.AdvancedDMPResourceTestUtils;
import de.avgl.dmp.persistence.model.schema.Attribute;
import de.avgl.dmp.persistence.model.schema.proxy.ProxyAttribute;
import de.avgl.dmp.persistence.service.schema.AttributeService;

public class AttributesResourceTestUtils extends AdvancedDMPResourceTestUtils<AttributeService, ProxyAttribute, Attribute> {

	public AttributesResourceTestUtils() {

		super("attributes", Attribute.class, AttributeService.class);
	}

	@Override
	public void reset() {
		
	}
}
