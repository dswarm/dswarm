package org.dswarm.controller.resources.job.test.utils;

import org.dswarm.controller.resources.test.utils.ExtendedBasicDMPResourceTestUtils;
import org.dswarm.persistence.model.job.Component;
import org.dswarm.persistence.model.job.proxy.ProxyComponent;
import org.dswarm.persistence.service.job.ComponentService;
import org.dswarm.persistence.service.job.test.utils.ComponentServiceTestUtils;

public class ComponentsResourceTestUtils extends
		ExtendedBasicDMPResourceTestUtils<ComponentServiceTestUtils, ComponentService, ProxyComponent, Component> {

	public ComponentsResourceTestUtils() {

		super("components", Component.class, ComponentService.class, ComponentServiceTestUtils.class);
	}
}
