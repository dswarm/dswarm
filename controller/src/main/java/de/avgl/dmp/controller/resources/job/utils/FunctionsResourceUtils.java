package de.avgl.dmp.controller.resources.job.utils;

import javax.inject.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import de.avgl.dmp.persistence.model.job.Function;
import de.avgl.dmp.persistence.service.job.FunctionService;

/**
 * @author tgaengler
 * @param <POJOCLASSPERSISTENCESERVICE>
 * @param <POJOCLASS>
 * @param <POJOCLASSIDTYPE>
 */
public class FunctionsResourceUtils extends BasicFunctionsResourceUtils<FunctionService, Function> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(FunctionsResourceUtils.class);

	@Inject
	public FunctionsResourceUtils(final Provider<FunctionService> persistenceServiceProviderArg, final Provider<ObjectMapper> objectMapperProviderArg) {

		super(Function.class, persistenceServiceProviderArg, objectMapperProviderArg);
	}
}
