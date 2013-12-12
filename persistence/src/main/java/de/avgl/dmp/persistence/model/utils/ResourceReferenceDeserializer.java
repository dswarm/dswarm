package de.avgl.dmp.persistence.model.utils;

import de.avgl.dmp.init.DMPException;
import de.avgl.dmp.persistence.model.resource.Resource;
import de.avgl.dmp.persistence.service.BasicJPAService;
import de.avgl.dmp.persistence.service.resource.ResourceService;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

public class ResourceReferenceDeserializer extends ReferenceDeserializer<Resource> {

	/**
	 * TODO: @tgaengler the injector is not available at this point here; however, why do we need to utilise the injector here? -
	 * can't we retrieve the service directly?
	 */
	@Override
	BasicJPAService<Resource, Long> getJpaService() throws DMPException {
		return DMPPersistenceUtil.getInjector().getInstance(ResourceService.class);
	}
}
