package de.avgl.dmp.controller.resources.job.utils;

import java.util.Set;

import javax.inject.Provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import de.avgl.dmp.controller.DMPControllerException;
import de.avgl.dmp.controller.resources.schema.utils.MappingAttributePathInstancesResourceUtils;
import de.avgl.dmp.controller.resources.utils.BasicDMPResourceUtils;
import de.avgl.dmp.controller.resources.utils.ResourceUtilsFactory;
import de.avgl.dmp.persistence.model.job.Component;
import de.avgl.dmp.persistence.model.job.Mapping;
import de.avgl.dmp.persistence.model.job.proxy.ProxyMapping;
import de.avgl.dmp.persistence.model.schema.MappingAttributePathInstance;
import de.avgl.dmp.persistence.service.job.MappingService;

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
