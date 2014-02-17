package de.avgl.dmp.controller.resources.test.utils;

import de.avgl.dmp.persistence.model.BasicDMPJPAObject;
import de.avgl.dmp.persistence.model.proxy.ProxyBasicDMPJPAObject;
import de.avgl.dmp.persistence.service.BasicDMPJPAService;
import de.avgl.dmp.persistence.service.test.utils.BasicDMPJPAServiceTestUtils;

public abstract class BasicDMPResourceTestUtils<POJOCLASSPERSISTENCESERVICETESTUTILS extends BasicDMPJPAServiceTestUtils<POJOCLASSPERSISTENCESERVICE, PROXYPOJOCLASS, POJOCLASS>, POJOCLASSPERSISTENCESERVICE extends BasicDMPJPAService<PROXYPOJOCLASS, POJOCLASS>, PROXYPOJOCLASS extends ProxyBasicDMPJPAObject<POJOCLASS>, POJOCLASS extends BasicDMPJPAObject>
		extends BasicResourceTestUtils<POJOCLASSPERSISTENCESERVICETESTUTILS, POJOCLASSPERSISTENCESERVICE, PROXYPOJOCLASS, POJOCLASS, Long> {

	public BasicDMPResourceTestUtils(final String resourceIdentifier, final Class<POJOCLASS> pojoClassArg,
			final Class<POJOCLASSPERSISTENCESERVICE> persistenceServiceClassArg,
			final Class<POJOCLASSPERSISTENCESERVICETESTUTILS> persistenceServiceTestUtilsClassArg) {

		super(resourceIdentifier, pojoClassArg, persistenceServiceClassArg, persistenceServiceTestUtilsClassArg);
	}
}
