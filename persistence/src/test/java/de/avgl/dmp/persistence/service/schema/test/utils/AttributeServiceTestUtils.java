package de.avgl.dmp.persistence.service.schema.test.utils;

import de.avgl.dmp.persistence.model.schema.Attribute;
import de.avgl.dmp.persistence.model.schema.proxy.ProxyAttribute;
import de.avgl.dmp.persistence.service.schema.AttributeService;
import de.avgl.dmp.persistence.service.test.utils.AdvancedDMPJPAServiceTestUtils;

public class AttributeServiceTestUtils extends AdvancedDMPJPAServiceTestUtils<AttributeService, ProxyAttribute, Attribute> {

	public AttributeServiceTestUtils() {

		super(Attribute.class, AttributeService.class);
	}
	
	public Attribute createAttribute(final String id, final String name) throws Exception {

		final Attribute attribute = new Attribute(id, name);
		final Attribute updatedAttribute = createObject(attribute, attribute);

		return updatedAttribute;
	}

	@Override
	public void reset() {

	}
}
