package org.dswarm.controller.resources.schema.test.utils;

import org.dswarm.controller.resources.test.utils.BasicDMPResourceTestUtils;
import org.dswarm.persistence.model.schema.ContentSchema;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.model.schema.proxy.ProxyContentSchema;
import org.dswarm.persistence.model.schema.proxy.ProxySchema;
import org.dswarm.persistence.service.schema.ContentSchemaService;
import org.dswarm.persistence.service.schema.SchemaService;
import org.dswarm.persistence.service.schema.test.utils.ContentSchemaServiceTestUtils;
import org.dswarm.persistence.service.schema.test.utils.SchemaServiceTestUtils;

public class ContentSchemasResourceTestUtils extends
		BasicDMPResourceTestUtils<ContentSchemaServiceTestUtils, ContentSchemaService, ProxyContentSchema, ContentSchema> {

	public ContentSchemasResourceTestUtils() {

		super("contentschemas", ContentSchema.class, ContentSchemaService.class, ContentSchemaServiceTestUtils.class);
	}
}
