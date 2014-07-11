package org.dswarm.controller.resources.job.utils;

import java.util.Set;

import javax.inject.Provider;

import org.dswarm.controller.DMPControllerException;
import org.dswarm.controller.resources.utils.ResourceUtilsFactory;
import org.dswarm.persistence.model.job.Function;
import org.dswarm.persistence.model.job.Transformation;
import org.dswarm.persistence.model.job.proxy.ProxyFunction;
import org.dswarm.persistence.service.job.FunctionService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

/**
 * @author tgaengler
 * @param <POJOCLASSPERSISTENCESERVICE>
 * @param <POJOCLASS>
 * @param <POJOCLASSIDTYPE>
 */
public class FunctionsResourceUtils extends BasicFunctionsResourceUtils<FunctionService, ProxyFunction, Function> {

	@Inject
	public FunctionsResourceUtils(final Provider<FunctionService> persistenceServiceProviderArg,
			final Provider<ObjectMapper> objectMapperProviderArg, final ResourceUtilsFactory utilsFactory) {

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

	@Override
	public String prepareObjectJSONString(final String objectJSONString) throws DMPControllerException {

		// a function is not a complex object

		return objectJSONString;
	}
}
