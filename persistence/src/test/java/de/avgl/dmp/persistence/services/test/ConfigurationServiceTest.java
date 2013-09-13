package de.avgl.dmp.persistence.services.test;

import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.test.BasicJPAServiceTest;
import de.avgl.dmp.persistence.services.ConfigurationService;


public class ConfigurationServiceTest extends BasicJPAServiceTest<Configuration, ConfigurationService> {
	
	private static final org.apache.log4j.Logger	LOG					= org.apache.log4j.Logger.getLogger(ConfigurationServiceTest.class);
	
	public ConfigurationServiceTest() {
		
		super("configuration", ConfigurationService.class);
	}
}
