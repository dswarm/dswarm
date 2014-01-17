package de.avgl.dmp.controller.resources.utils;

import javax.inject.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.avgl.dmp.persistence.model.ExtendedBasicDMPJPAObject;
import de.avgl.dmp.persistence.service.ExtendedBasicDMPJPAService;

/**
 * @author tgaengler
 * @param <POJOCLASSPERSISTENCESERVICE>
 * @param <POJOCLASS>
 * @param <POJOCLASSIDTYPE>
 */
public abstract class ExtendedBasicDMPResourceUtils<POJOCLASSPERSISTENCESERVICE extends ExtendedBasicDMPJPAService<POJOCLASS>, POJOCLASS extends ExtendedBasicDMPJPAObject>
		extends BasicDMPResourceUtils<POJOCLASSPERSISTENCESERVICE, POJOCLASS> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(ExtendedBasicDMPResourceUtils.class);

	public ExtendedBasicDMPResourceUtils(final Class<POJOCLASS> pojoClassArg,
	                                     final Provider<POJOCLASSPERSISTENCESERVICE> persistenceServiceProviderArg,
	                                     final Provider<ObjectMapper> objectMapperProviderArg,
	                                     final ResourceUtilsFactory utilsFactory) {

		super(pojoClassArg, persistenceServiceProviderArg, objectMapperProviderArg, utilsFactory);
	}
}
