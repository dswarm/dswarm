package org.dswarm.controller.resources.job.test.utils;

import org.dswarm.controller.resources.test.utils.BasicDMPResourceTestUtils;
import org.dswarm.persistence.model.job.Mapping;
import org.dswarm.persistence.model.job.proxy.ProxyMapping;
import org.dswarm.persistence.service.job.MappingService;
import org.dswarm.persistence.service.job.test.utils.MappingServiceTestUtils;

public class MappingsResourceTestUtils extends BasicDMPResourceTestUtils<MappingServiceTestUtils, MappingService, ProxyMapping, Mapping> {

	public MappingsResourceTestUtils() {

		super("mappings", Mapping.class, MappingService.class, MappingServiceTestUtils.class);
	}
}
