package de.avgl.dmp.controller.resources.job.utils;

import javax.inject.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import de.avgl.dmp.controller.resources.utils.BasicDMPResourceUtils;
import de.avgl.dmp.persistence.model.job.Mapping;
import de.avgl.dmp.persistence.service.job.MappingService;

/**
 * @author tgaengler
 * @param <POJOCLASSPERSISTENCESERVICE>
 * @param <POJOCLASS>
 * @param <POJOCLASSIDTYPE>
 */
public class MappingsResourceUtils extends BasicDMPResourceUtils<MappingService, Mapping> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(MappingsResourceUtils.class);

	@Inject
	public MappingsResourceUtils(final Provider<MappingService> persistenceServiceProviderArg, final Provider<ObjectMapper> objectMapperProviderArg) {

		super(Mapping.class, persistenceServiceProviderArg, objectMapperProviderArg);
	}
}
