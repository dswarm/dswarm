package org.dswarm.controller.resources.schema.utils;

import javax.inject.Provider;

import org.dswarm.controller.resources.utils.AdvancedDMPResourceUtils;
import org.dswarm.controller.resources.utils.ResourceUtilsFactory;
import org.dswarm.persistence.model.schema.Clasz;
import org.dswarm.persistence.model.schema.proxy.ProxyClasz;
import org.dswarm.persistence.service.schema.ClaszService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

/**
 * @author tgaengler
 * @param <POJOCLASSPERSISTENCESERVICE>
 * @param <POJOCLASS>
 * @param <POJOCLASSIDTYPE>
 */
public class ClaszesResourceUtils extends AdvancedDMPResourceUtils<ClaszService, ProxyClasz, Clasz> {

	@Inject
	public ClaszesResourceUtils(final Provider<ClaszService> persistenceServiceProviderArg, final Provider<ObjectMapper> objectMapperProviderArg,
			final ResourceUtilsFactory utilsFactory) {

		super(Clasz.class, persistenceServiceProviderArg, objectMapperProviderArg, utilsFactory);
	}
}
