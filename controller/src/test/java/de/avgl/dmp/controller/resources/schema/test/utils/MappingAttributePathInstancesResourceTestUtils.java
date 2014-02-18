package de.avgl.dmp.controller.resources.schema.test.utils;

import de.avgl.dmp.persistence.model.schema.MappingAttributePathInstance;
import de.avgl.dmp.persistence.model.schema.proxy.ProxyMappingAttributePathInstance;
import de.avgl.dmp.persistence.service.schema.MappingAttributePathInstanceService;
import de.avgl.dmp.persistence.service.schema.test.utils.MappingAttributePathInstanceServiceTestUtils;

public class MappingAttributePathInstancesResourceTestUtils
		extends
		AttributePathInstancesResourceTestUtils<MappingAttributePathInstanceServiceTestUtils, MappingAttributePathInstanceService, ProxyMappingAttributePathInstance, MappingAttributePathInstance> {

	public MappingAttributePathInstancesResourceTestUtils() {

		super("mappingattributepathinstances", MappingAttributePathInstance.class, MappingAttributePathInstanceService.class,
				MappingAttributePathInstanceServiceTestUtils.class);
	}
}
