package de.avgl.dmp.controller.resources.job.test.utils;

import de.avgl.dmp.controller.resources.test.utils.ExtendedBasicDMPResourceTestUtils;
import de.avgl.dmp.persistence.model.job.Component;
import de.avgl.dmp.persistence.model.job.proxy.ProxyComponent;
import de.avgl.dmp.persistence.service.job.ComponentService;
import de.avgl.dmp.persistence.service.job.test.utils.ComponentServiceTestUtils;

public class ComponentsResourceTestUtils extends
		ExtendedBasicDMPResourceTestUtils<ComponentServiceTestUtils, ComponentService, ProxyComponent, Component> {

	public ComponentsResourceTestUtils() {

		super("components", Component.class, ComponentService.class, ComponentServiceTestUtils.class);
	}
}
