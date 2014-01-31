package de.avgl.dmp.controller.resources.schema.utils;

import javax.inject.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import de.avgl.dmp.controller.resources.utils.AdvancedDMPResourceUtils;
import de.avgl.dmp.controller.resources.utils.ResourceUtilsFactory;
import de.avgl.dmp.persistence.model.schema.Clasz;
import de.avgl.dmp.persistence.model.schema.proxy.ProxyClasz;
import de.avgl.dmp.persistence.service.schema.ClaszService;

/**
 * @author tgaengler
 * @param <POJOCLASSPERSISTENCESERVICE>
 * @param <POJOCLASS>
 * @param <POJOCLASSIDTYPE>
 */
public class ClaszesResourceUtils extends AdvancedDMPResourceUtils<ClaszService, ProxyClasz, Clasz> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(ClaszesResourceUtils.class);

	@Inject
	public ClaszesResourceUtils(final Provider<ClaszService> persistenceServiceProviderArg, final Provider<ObjectMapper> objectMapperProviderArg,
			final ResourceUtilsFactory utilsFactory) {

		super(Clasz.class, persistenceServiceProviderArg, objectMapperProviderArg, utilsFactory);
	}
}
