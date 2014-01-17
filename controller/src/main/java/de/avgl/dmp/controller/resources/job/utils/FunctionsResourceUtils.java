package de.avgl.dmp.controller.resources.job.utils;

import java.util.Set;

import javax.inject.Provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import de.avgl.dmp.controller.DMPControllerException;
import de.avgl.dmp.controller.resources.utils.ResourceUtilsFactory;
import de.avgl.dmp.persistence.model.job.Function;
import de.avgl.dmp.persistence.model.job.Transformation;
import de.avgl.dmp.persistence.service.job.FunctionService;

/**
 * @author tgaengler
 * @param <POJOCLASSPERSISTENCESERVICE>
 * @param <POJOCLASS>
 * @param <POJOCLASSIDTYPE>
 */
public class FunctionsResourceUtils extends BasicFunctionsResourceUtils<FunctionService, Function> {

	private static final org.apache.log4j.Logger			LOG	= org.apache.log4j.Logger.getLogger(FunctionsResourceUtils.class);

	@Inject
	public FunctionsResourceUtils(final Provider<FunctionService> persistenceServiceProviderArg,
	                              final Provider<ObjectMapper> objectMapperProviderArg,
	                              final ResourceUtilsFactory utilsFactory) {

		super(Function.class, persistenceServiceProviderArg, objectMapperProviderArg, utilsFactory);
	}

	@Override
	public JsonNode replaceRelevantDummyIds(final Function object, final JsonNode jsonNode, final Set<Long> dummyIdCandidates)
			throws DMPControllerException {

		if (checkObject(object, dummyIdCandidates)) {

			return jsonNode;
		}

		if (Transformation.class.isInstance(object)) {

			utilsFactory.get(TransformationsResourceUtils.class).replaceRelevantDummyIds((Transformation) object, jsonNode, dummyIdCandidates);
		} else {

			super.replaceRelevantDummyIds(object, jsonNode, dummyIdCandidates);
		}

		return jsonNode;
	}
}
