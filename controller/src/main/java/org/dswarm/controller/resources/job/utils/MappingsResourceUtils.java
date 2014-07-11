package org.dswarm.controller.resources.job.utils;

import java.util.Set;

import javax.inject.Provider;

import org.dswarm.controller.DMPControllerException;
import org.dswarm.controller.resources.schema.utils.MappingAttributePathInstancesResourceUtils;
import org.dswarm.controller.resources.utils.BasicDMPResourceUtils;
import org.dswarm.controller.resources.utils.ResourceUtilsFactory;
import org.dswarm.persistence.model.job.Component;
import org.dswarm.persistence.model.job.Mapping;
import org.dswarm.persistence.model.job.proxy.ProxyMapping;
import org.dswarm.persistence.model.schema.MappingAttributePathInstance;
import org.dswarm.persistence.service.job.MappingService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

/**
 * @author tgaengler
 * @param <POJOCLASSPERSISTENCESERVICE>
 * @param <POJOCLASS>
 * @param <POJOCLASSIDTYPE>
 */
public class MappingsResourceUtils extends BasicDMPResourceUtils<MappingService, ProxyMapping, Mapping> {

	@Inject
	public MappingsResourceUtils(final Provider<MappingService> persistenceServiceProviderArg, final Provider<ObjectMapper> objectMapperProviderArg,
			final ResourceUtilsFactory utilsFactory) {

		super(Mapping.class, persistenceServiceProviderArg, objectMapperProviderArg, utilsFactory);

	}

	@Override
	public JsonNode replaceRelevantDummyIds(final Mapping object, final JsonNode jsonNode, final Set<Long> dummyIdCandidates)
			throws DMPControllerException {

		if (checkObject(object, dummyIdCandidates)) {

			return jsonNode;
		}

		super.replaceRelevantDummyIds(object, jsonNode, dummyIdCandidates);

		final Set<MappingAttributePathInstance> inputAttributePaths = object.getInputAttributePaths();

		if (inputAttributePaths != null) {

			for (final MappingAttributePathInstance inputAttributePath : inputAttributePaths) {

				if (replaceRelevantDummyIdsInAttributePath(inputAttributePath, jsonNode, dummyIdCandidates)) {

					return jsonNode;
				}
			}
		}

		final MappingAttributePathInstance outputAttributePath = object.getOutputAttributePath();

		if (replaceRelevantDummyIdsInAttributePath(outputAttributePath, jsonNode, dummyIdCandidates)) {

			return jsonNode;
		}

		final Component transformationComponent = object.getTransformation();

		if (transformationComponent != null) {

			if (areDummyIdCandidatesEmpty(dummyIdCandidates)) {

				return jsonNode;
			}

			utilsFactory.get(ComponentsResourceUtils.class).replaceRelevantDummyIds(transformationComponent, jsonNode, dummyIdCandidates);
		}

		return jsonNode;
	}

	private boolean replaceRelevantDummyIdsInAttributePath(final MappingAttributePathInstance attributePath, final JsonNode jsonNode,
			final Set<Long> dummyIdCandidates) throws DMPControllerException {

		if (attributePath != null) {

			if (areDummyIdCandidatesEmpty(dummyIdCandidates)) {

				return true;
			}

			utilsFactory.get(MappingAttributePathInstancesResourceUtils.class).replaceRelevantDummyIds(attributePath, jsonNode, dummyIdCandidates);
		}

		return false;
	}
}
