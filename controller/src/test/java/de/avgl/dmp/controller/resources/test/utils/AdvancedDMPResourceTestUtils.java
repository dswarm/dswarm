package de.avgl.dmp.controller.resources.test.utils;

import de.avgl.dmp.persistence.model.AdvancedDMPJPAObject;
import de.avgl.dmp.persistence.model.proxy.ProxyAdvancedDMPJPAObject;
import de.avgl.dmp.persistence.service.AdvancedDMPJPAService;
import de.avgl.dmp.persistence.service.test.utils.AdvancedDMPJPAServiceTestUtils;

public abstract class AdvancedDMPResourceTestUtils<POJOCLASSPERSISTENCESERVICETESTUTILS extends AdvancedDMPJPAServiceTestUtils<POJOCLASSPERSISTENCESERVICE, PROXYPOJOCLASS, POJOCLASS>, POJOCLASSPERSISTENCESERVICE extends AdvancedDMPJPAService<PROXYPOJOCLASS, POJOCLASS>, PROXYPOJOCLASS extends ProxyAdvancedDMPJPAObject<POJOCLASS>, POJOCLASS extends AdvancedDMPJPAObject>
		extends BasicDMPResourceTestUtils<POJOCLASSPERSISTENCESERVICETESTUTILS, POJOCLASSPERSISTENCESERVICE, PROXYPOJOCLASS, POJOCLASS> {

	public AdvancedDMPResourceTestUtils(final String resourceIdentifier, final Class<POJOCLASS> pojoClassArg,
			final Class<POJOCLASSPERSISTENCESERVICE> persistenceServiceClassArg,
			final Class<POJOCLASSPERSISTENCESERVICETESTUTILS> persistenceServiceTestUtilsClassArg) {

		super(resourceIdentifier, pojoClassArg, persistenceServiceClassArg, persistenceServiceTestUtilsClassArg);
	}
}
