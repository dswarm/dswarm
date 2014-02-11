package de.avgl.dmp.controller.resources.schema.test.utils;

import de.avgl.dmp.controller.resources.test.utils.BasicResourceTestUtils;
import de.avgl.dmp.persistence.model.schema.AttributePath;
import de.avgl.dmp.persistence.model.schema.proxy.ProxyAttributePath;
import de.avgl.dmp.persistence.service.schema.AttributePathService;
import de.avgl.dmp.persistence.service.schema.test.utils.AttributePathServiceTestUtils;

public class AttributePathsResourceTestUtils extends
		BasicResourceTestUtils<AttributePathServiceTestUtils, AttributePathService, ProxyAttributePath, AttributePath, Long> {

	public AttributePathsResourceTestUtils() {

		super("attributepaths", AttributePath.class, AttributePathService.class, AttributePathServiceTestUtils.class);
	}
}
