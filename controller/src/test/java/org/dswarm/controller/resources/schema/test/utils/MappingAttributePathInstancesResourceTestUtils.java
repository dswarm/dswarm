package org.dswarm.controller.resources.schema.test.utils;

import org.dswarm.persistence.model.schema.MappingAttributePathInstance;
import org.dswarm.persistence.model.schema.proxy.ProxyMappingAttributePathInstance;
import org.dswarm.persistence.service.schema.MappingAttributePathInstanceService;
import org.dswarm.persistence.service.schema.test.utils.MappingAttributePathInstanceServiceTestUtils;

public class MappingAttributePathInstancesResourceTestUtils
		extends
		AttributePathInstancesResourceTestUtils<MappingAttributePathInstanceServiceTestUtils, MappingAttributePathInstanceService, ProxyMappingAttributePathInstance, MappingAttributePathInstance> {

	public MappingAttributePathInstancesResourceTestUtils() {

		super("mappingattributepathinstances", MappingAttributePathInstance.class, MappingAttributePathInstanceService.class,
				MappingAttributePathInstanceServiceTestUtils.class);
	}
}
