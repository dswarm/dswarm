package de.avgl.dmp.controller.resources.utils;

import javax.inject.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.avgl.dmp.persistence.model.BasicDMPJPAObject;
import de.avgl.dmp.persistence.model.proxy.ProxyBasicDMPJPAObject;
import de.avgl.dmp.persistence.service.BasicDMPJPAService;

/**
 * @author tgaengler
 * @param <POJOCLASSPERSISTENCESERVICE>
 * @param <POJOCLASS>
 * @param <POJOCLASSIDTYPE>
 */
public abstract class BasicDMPResourceUtils<POJOCLASSPERSISTENCESERVICE extends BasicDMPJPAService<PROXYPOJOCLASS, POJOCLASS>, PROXYPOJOCLASS extends ProxyBasicDMPJPAObject<POJOCLASS>, POJOCLASS extends BasicDMPJPAObject>
		extends BasicIDResourceUtils<POJOCLASSPERSISTENCESERVICE, PROXYPOJOCLASS, POJOCLASS> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(BasicDMPResourceUtils.class);

	public BasicDMPResourceUtils(final Class<POJOCLASS> pojoClassArg, final Provider<POJOCLASSPERSISTENCESERVICE> persistenceServiceProviderArg,
			final Provider<ObjectMapper> objectMapperProviderArg, final ResourceUtilsFactory utilsFactory) {

		super(pojoClassArg, persistenceServiceProviderArg, objectMapperProviderArg, utilsFactory);
	}
}
