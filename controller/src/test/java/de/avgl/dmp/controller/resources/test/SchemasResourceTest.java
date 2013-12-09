package de.avgl.dmp.controller.resources.test;

import de.avgl.dmp.controller.resources.test.utils.SchemasResourceTestUtils;

import de.avgl.dmp.persistence.model.schema.Schema;
import de.avgl.dmp.persistence.service.schema.SchemaService;

public class SchemasResourceTest extends BasicResourceTest<SchemasResourceTestUtils, SchemaService, Schema, Long> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(SchemasResourceTest.class);

	public SchemasResourceTest() {

		super(Schema.class, SchemaService.class, "schemas", "schema.json", new SchemasResourceTestUtils());
	}
}