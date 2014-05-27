package de.avgl.dmp.controller.resources.job.utils;

import javax.inject.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.avgl.dmp.controller.resources.utils.ExtendedBasicDMPResourceUtils;
import de.avgl.dmp.controller.resources.utils.ResourceUtilsFactory;
import de.avgl.dmp.persistence.model.job.Function;
import de.avgl.dmp.persistence.model.job.proxy.ProxyBasicFunction;
import de.avgl.dmp.persistence.service.job.BasicFunctionService;

/**
 * @author tgaengler
 * @param <POJOCLASSPERSISTENCESERVICE>
 * @param <POJOCLASS>
 * @param <POJOCLASSIDTYPE>
 */
public abstract class BasicFunctionsResourceUtils<POJOCLASSPERSISTENCESERVICE extends BasicFunctionService<PROXYPOJOCLASS, POJOCLASS>, PROXYPOJOCLASS extends ProxyBasicFunction<POJOCLASS>, POJOCLASS extends Function>
		extends ExtendedBasicDMPResourceUtils<POJOCLASSPERSISTENCESERVICE, PROXYPOJOCLASS, POJOCLASS> {

	public BasicFunctionsResourceUtils(final Class<POJOCLASS> pojoClassArg,
			final Provider<POJOCLASSPERSISTENCESERVICE> persistenceServiceProviderArg, final Provider<ObjectMapper> objectMapperProviderArg,
			final ResourceUtilsFactory utilsFactory) {

		super(pojoClassArg, persistenceServiceProviderArg, objectMapperProviderArg, utilsFactory);
	}
}
