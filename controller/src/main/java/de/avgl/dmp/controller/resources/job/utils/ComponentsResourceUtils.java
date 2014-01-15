package de.avgl.dmp.controller.resources.job.utils;

import javax.inject.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import de.avgl.dmp.controller.resources.utils.ExtendedBasicDMPResourceUtils;
import de.avgl.dmp.persistence.model.job.Component;
import de.avgl.dmp.persistence.service.job.ComponentService;

/**
 * @author tgaengler
 * @param <POJOCLASSPERSISTENCESERVICE>
 * @param <POJOCLASS>
 * @param <POJOCLASSIDTYPE>
 */
public class ComponentsResourceUtils extends ExtendedBasicDMPResourceUtils<ComponentService, Component> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(ComponentsResourceUtils.class);

	@Inject
	public ComponentsResourceUtils(final Provider<ComponentService> persistenceServiceProviderArg, final Provider<ObjectMapper> objectMapperProviderArg) {

		super(Component.class, persistenceServiceProviderArg, objectMapperProviderArg);
	}
}
