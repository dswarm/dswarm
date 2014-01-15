package de.avgl.dmp.controller.resources.job.utils;

import javax.inject.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import de.avgl.dmp.persistence.model.job.Transformation;
import de.avgl.dmp.persistence.service.job.TransformationService;

/**
 * @author tgaengler
 * @param <POJOCLASSPERSISTENCESERVICE>
 * @param <POJOCLASS>
 * @param <POJOCLASSIDTYPE>
 */
public class TransformationsResourceUtils extends BasicFunctionsResourceUtils<TransformationService, Transformation> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(TransformationsResourceUtils.class);

	@Inject
	public TransformationsResourceUtils(final Provider<TransformationService> persistenceServiceProviderArg, final Provider<ObjectMapper> objectMapperProviderArg) {

		super(Transformation.class, persistenceServiceProviderArg, objectMapperProviderArg);
	}
}
