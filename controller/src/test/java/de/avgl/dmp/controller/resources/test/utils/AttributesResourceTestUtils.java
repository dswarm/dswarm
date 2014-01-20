package de.avgl.dmp.controller.resources.test.utils;

import de.avgl.dmp.persistence.model.schema.Attribute;
import de.avgl.dmp.persistence.service.schema.AttributeService;

public class AttributesResourceTestUtils extends BasicResourceTestUtils<AttributeService, Attribute, Long> {

	public AttributesResourceTestUtils() {

		super("attributes", Attribute.class, AttributeService.class);
	}

	@Override
	public void reset() {
		
	}
}
