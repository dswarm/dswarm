package de.avgl.dmp.controller.resources.schema.test.utils;

import de.avgl.dmp.controller.resources.test.utils.BasicDMPResourceTestUtils;
import de.avgl.dmp.persistence.model.schema.AttributePathInstance;
import de.avgl.dmp.persistence.model.schema.proxy.ProxyAttributePathInstance;
import de.avgl.dmp.persistence.service.schema.AttributePathInstanceService;
import de.avgl.dmp.persistence.service.schema.test.utils.AttributePathInstanceServiceTestUtils;

public abstract class AttributePathInstancesResourceTestUtils<POJOCLASSPERSISTENCESERVICETESTUTILS extends AttributePathInstanceServiceTestUtils<POJOCLASSPERSISTENCESERVICE, PROXYPOJOCLASS, POJOCLASS>, POJOCLASSPERSISTENCESERVICE extends AttributePathInstanceService<PROXYPOJOCLASS, POJOCLASS>, PROXYPOJOCLASS extends ProxyAttributePathInstance<POJOCLASS>, POJOCLASS extends AttributePathInstance>
		extends BasicDMPResourceTestUtils<POJOCLASSPERSISTENCESERVICETESTUTILS, POJOCLASSPERSISTENCESERVICE, PROXYPOJOCLASS, POJOCLASS> {

	public AttributePathInstancesResourceTestUtils(final String resourceIdentifier, final Class<POJOCLASS> pojoClassArg,
			final Class<POJOCLASSPERSISTENCESERVICE> persistenceServiceClassArg,
			final Class<POJOCLASSPERSISTENCESERVICETESTUTILS> persistenceServiceTestUtilsClassArg) {

		super(resourceIdentifier, pojoClassArg, persistenceServiceClassArg, persistenceServiceTestUtilsClassArg);
	}
}
