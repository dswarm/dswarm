package de.avgl.dmp.controller.services;

import de.avgl.dmp.persistence.services.ConfigurationService;
import de.avgl.dmp.persistence.services.ResourceService;


public class PersistenceServices {
	
	private static PersistenceServices instance;
	
	private static ResourceService resourceService;
	
	private static ConfigurationService configurationService;
	
	private PersistenceServices() {
		
		
	}
	
	public static PersistenceServices getInstance() {
		
		if(instance == null) {
			
			instance = new PersistenceServices();
		}
		
		return instance;
	}
	
	public ResourceService getResourceService() {
		
		if(resourceService == null) {
			
			resourceService = new ResourceService();
		}
		
		return resourceService;
	}
	
	public ConfigurationService getConfigurationService() {
		
		if(configurationService == null) {
			
			configurationService = new ConfigurationService();
		}
		
		return configurationService;
	}

}
