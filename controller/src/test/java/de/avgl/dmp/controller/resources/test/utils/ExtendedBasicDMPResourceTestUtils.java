package de.avgl.dmp.controller.resources.test.utils;

import de.avgl.dmp.persistence.model.ExtendedBasicDMPJPAObject;
import de.avgl.dmp.persistence.model.proxy.ProxyExtendedBasicDMPJPAObject;
import de.avgl.dmp.persistence.service.ExtendedBasicDMPJPAService;
import de.avgl.dmp.persistence.service.test.utils.ExtendedBasicDMPJPAServiceTestUtils;

public abstract class ExtendedBasicDMPResourceTestUtils<POJOCLASSPERSISTENCESERVICETESTUTILS extends ExtendedBasicDMPJPAServiceTestUtils<POJOCLASSPERSISTENCESERVICE, PROXYPOJOCLASS, POJOCLASS>, POJOCLASSPERSISTENCESERVICE extends ExtendedBasicDMPJPAService<PROXYPOJOCLASS, POJOCLASS>, PROXYPOJOCLASS extends ProxyExtendedBasicDMPJPAObject<POJOCLASS>, POJOCLASS extends ExtendedBasicDMPJPAObject>
		extends BasicDMPResourceTestUtils<POJOCLASSPERSISTENCESERVICETESTUTILS, POJOCLASSPERSISTENCESERVICE, PROXYPOJOCLASS, POJOCLASS> {

	public ExtendedBasicDMPResourceTestUtils(final String resourceIdentifier, final Class<POJOCLASS> pojoClassArg,
			final Class<POJOCLASSPERSISTENCESERVICE> persistenceServiceClassArg,
			final Class<POJOCLASSPERSISTENCESERVICETESTUTILS> persistenceServiceTestUtilsClassArg) {

		super(resourceIdentifier, pojoClassArg, persistenceServiceClassArg, persistenceServiceTestUtilsClassArg);
	}
}
