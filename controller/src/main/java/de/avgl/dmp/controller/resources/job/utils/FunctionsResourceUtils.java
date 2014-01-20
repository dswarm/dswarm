package de.avgl.dmp.controller.resources.job.utils;

import java.util.Set;

import javax.inject.Provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import de.avgl.dmp.controller.DMPControllerException;
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

	private final Provider<TransformationsResourceUtils>	transformationsResourceUtilsProvider;

	@Inject
	public FunctionsResourceUtils(final Provider<FunctionService> persistenceServiceProviderArg,
			final Provider<ObjectMapper> objectMapperProviderArg, final Provider<TransformationsResourceUtils> transformationsResourceUtilsProviderArg) {

		super(Function.class, persistenceServiceProviderArg, objectMapperProviderArg);

		transformationsResourceUtilsProvider = transformationsResourceUtilsProviderArg;

		// add here all identifiers for attributes that bear native JSON objects/arrays

		toBeSkippedJsonNodes.add("function_description");
	}

	@Override
	public JsonNode replaceRelevantDummyIds(final Function object, final JsonNode jsonNode, final Set<Long> dummyIdCandidates)
			throws DMPControllerException {

		if (checkObject(object, dummyIdCandidates)) {

			return jsonNode;
		}

		if (Transformation.class.isInstance(object)) {

			transformationsResourceUtilsProvider.get().replaceRelevantDummyIds((Transformation) object, jsonNode, dummyIdCandidates);
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
