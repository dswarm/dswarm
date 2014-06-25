package org.dswarm.controller.resources.test.utils;

import org.dswarm.persistence.model.AdvancedDMPJPAObject;
import org.dswarm.persistence.model.proxy.ProxyAdvancedDMPJPAObject;
import org.dswarm.persistence.service.AdvancedDMPJPAService;
import org.dswarm.persistence.service.test.utils.AdvancedDMPJPAServiceTestUtils;

public abstract class AdvancedDMPResourceTestUtils<POJOCLASSPERSISTENCESERVICETESTUTILS extends AdvancedDMPJPAServiceTestUtils<POJOCLASSPERSISTENCESERVICE, PROXYPOJOCLASS, POJOCLASS>, POJOCLASSPERSISTENCESERVICE extends AdvancedDMPJPAService<PROXYPOJOCLASS, POJOCLASS>, PROXYPOJOCLASS extends ProxyAdvancedDMPJPAObject<POJOCLASS>, POJOCLASS extends AdvancedDMPJPAObject>
		extends BasicDMPResourceTestUtils<POJOCLASSPERSISTENCESERVICETESTUTILS, POJOCLASSPERSISTENCESERVICE, PROXYPOJOCLASS, POJOCLASS> {

	public AdvancedDMPResourceTestUtils(final String resourceIdentifier, final Class<POJOCLASS> pojoClassArg,
			final Class<POJOCLASSPERSISTENCESERVICE> persistenceServiceClassArg,
			final Class<POJOCLASSPERSISTENCESERVICETESTUTILS> persistenceServiceTestUtilsClassArg) {

		super(resourceIdentifier, pojoClassArg, persistenceServiceClassArg, persistenceServiceTestUtilsClassArg);
	}
}
