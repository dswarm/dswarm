package de.avgl.dmp.persistence.model.resource.test;

import de.avgl.dmp.persistence.model.resource.Resource;
import de.avgl.dmp.persistence.model.test.BasicJPAServiceTest;
import de.avgl.dmp.persistence.services.ResourceService;


public class ResourceServiceTest extends BasicJPAServiceTest<Resource, ResourceService> {
	
	private static final org.apache.log4j.Logger	LOG					= org.apache.log4j.Logger.getLogger(ResourceServiceTest.class);
	
	public ResourceServiceTest() {
		
		super("resource", ResourceService.class);
	}
}
