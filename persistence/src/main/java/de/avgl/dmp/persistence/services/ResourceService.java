package de.avgl.dmp.persistence.services;

import de.avgl.dmp.persistence.model.resource.Resource;


public class ResourceService extends BasicJPAService<Resource> {

	public ResourceService() {
		
		super(Resource.class);
	}

	@Override
	protected void prepareObjectForRemoval(final Resource object) {
		// TODO Auto-generated method stub
		
	}	
}
