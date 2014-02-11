package de.avgl.dmp.controller.resources.schema.test.utils;

import de.avgl.dmp.controller.resources.test.utils.BasicDMPResourceTestUtils;
import de.avgl.dmp.persistence.model.schema.Schema;
import de.avgl.dmp.persistence.model.schema.proxy.ProxySchema;
import de.avgl.dmp.persistence.service.schema.SchemaService;
import de.avgl.dmp.persistence.service.schema.test.utils.SchemaServiceTestUtils;

public class SchemasResourceTestUtils extends BasicDMPResourceTestUtils<SchemaServiceTestUtils, SchemaService, ProxySchema, Schema> {

	public SchemasResourceTestUtils() {

		super("schemas", Schema.class, SchemaService.class, SchemaServiceTestUtils.class);
	}
}
