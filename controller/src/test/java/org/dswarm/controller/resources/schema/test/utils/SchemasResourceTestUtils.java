package org.dswarm.controller.resources.schema.test.utils;

import org.dswarm.controller.resources.test.utils.BasicDMPResourceTestUtils;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.model.schema.proxy.ProxySchema;
import org.dswarm.persistence.service.schema.SchemaService;
import org.dswarm.persistence.service.schema.test.utils.SchemaServiceTestUtils;

public class SchemasResourceTestUtils extends BasicDMPResourceTestUtils<SchemaServiceTestUtils, SchemaService, ProxySchema, Schema> {

	public SchemasResourceTestUtils() {

		super("schemas", Schema.class, SchemaService.class, SchemaServiceTestUtils.class);
	}
}
