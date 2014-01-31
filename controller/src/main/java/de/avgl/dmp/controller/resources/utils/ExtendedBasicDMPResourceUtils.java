package de.avgl.dmp.controller.resources.utils;

import javax.inject.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.avgl.dmp.persistence.model.ExtendedBasicDMPJPAObject;
import de.avgl.dmp.persistence.model.proxy.ProxyExtendedBasicDMPJPAObject;
import de.avgl.dmp.persistence.service.ExtendedBasicDMPJPAService;

/**
 * @author tgaengler
 * @param <POJOCLASSPERSISTENCESERVICE>
 * @param <POJOCLASS>
 * @param <POJOCLASSIDTYPE>
 */
public abstract class ExtendedBasicDMPResourceUtils<POJOCLASSPERSISTENCESERVICE extends ExtendedBasicDMPJPAService<PROXYPOJOCLASS, POJOCLASS>, PROXYPOJOCLASS extends ProxyExtendedBasicDMPJPAObject<POJOCLASS>, POJOCLASS extends ExtendedBasicDMPJPAObject>
		extends BasicDMPResourceUtils<POJOCLASSPERSISTENCESERVICE, PROXYPOJOCLASS, POJOCLASS> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(ExtendedBasicDMPResourceUtils.class);

	public ExtendedBasicDMPResourceUtils(final Class<POJOCLASS> pojoClassArg,
			final Provider<POJOCLASSPERSISTENCESERVICE> persistenceServiceProviderArg, final Provider<ObjectMapper> objectMapperProviderArg,
			final ResourceUtilsFactory utilsFactory) {

		super(pojoClassArg, persistenceServiceProviderArg, objectMapperProviderArg, utilsFactory);
	}
}
