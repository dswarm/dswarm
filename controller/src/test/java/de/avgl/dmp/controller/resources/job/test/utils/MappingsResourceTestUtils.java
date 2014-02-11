package de.avgl.dmp.controller.resources.job.test.utils;

import de.avgl.dmp.controller.resources.test.utils.BasicDMPResourceTestUtils;
import de.avgl.dmp.persistence.model.job.Mapping;
import de.avgl.dmp.persistence.model.job.proxy.ProxyMapping;
import de.avgl.dmp.persistence.service.job.MappingService;
import de.avgl.dmp.persistence.service.job.test.utils.MappingServiceTestUtils;

public class MappingsResourceTestUtils extends BasicDMPResourceTestUtils<MappingServiceTestUtils, MappingService, ProxyMapping, Mapping> {

	public MappingsResourceTestUtils() {

		super("mappings", Mapping.class, MappingService.class, MappingServiceTestUtils.class);
	}
}
