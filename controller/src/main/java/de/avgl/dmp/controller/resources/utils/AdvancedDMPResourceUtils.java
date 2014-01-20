package de.avgl.dmp.controller.resources.utils;

import javax.inject.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.avgl.dmp.controller.DMPControllerException;
import de.avgl.dmp.persistence.model.AdvancedDMPJPAObject;
import de.avgl.dmp.persistence.service.AdvancedDMPJPAService;

/**
 * @author tgaengler
 * @param <POJOCLASSPERSISTENCESERVICE>
 * @param <POJOCLASS>
 * @param <POJOCLASSIDTYPE>
 */
public abstract class AdvancedDMPResourceUtils<POJOCLASSPERSISTENCESERVICE extends AdvancedDMPJPAService<POJOCLASS>, POJOCLASS extends AdvancedDMPJPAObject>
		extends BasicDMPResourceUtils<POJOCLASSPERSISTENCESERVICE, POJOCLASS> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(AdvancedDMPResourceUtils.class);

	public AdvancedDMPResourceUtils(final Class<POJOCLASS> pojoClassArg, final Provider<POJOCLASSPERSISTENCESERVICE> persistenceServiceProviderArg,
			final Provider<ObjectMapper> objectMapperProviderArg) {

		super(pojoClassArg, persistenceServiceProviderArg, objectMapperProviderArg);
	}
	
	@Override
	public String prepareObjectJSONString(String objectJSONString) throws DMPControllerException {

		// an attribute or clasz is not a complex object

		return objectJSONString;
	}
}
