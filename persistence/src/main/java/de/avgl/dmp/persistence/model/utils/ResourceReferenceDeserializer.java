package de.avgl.dmp.persistence.model.utils;

import de.avgl.dmp.init.DMPException;
import de.avgl.dmp.persistence.model.resource.Resource;
import de.avgl.dmp.persistence.services.BasicJPAService;
import de.avgl.dmp.persistence.services.ResourceService;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;


public class ResourceReferenceDeserializer extends ReferenceDeserializer<Resource> {


	public ResourceReferenceDeserializer() {
		super();
	}

	@Override
	BasicJPAService<Resource> getJpaService() throws DMPException {
		return DMPPersistenceUtil.getInjector().getInstance(ResourceService.class);
	}
}
