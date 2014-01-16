package de.avgl.dmp.controller.resources.job.utils;

import java.util.Set;

import javax.inject.Provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import de.avgl.dmp.controller.DMPControllerException;
import de.avgl.dmp.controller.resources.schema.utils.AttributePathsResourceUtils;
import de.avgl.dmp.controller.resources.utils.BasicDMPResourceUtils;
import de.avgl.dmp.persistence.model.job.Component;
import de.avgl.dmp.persistence.model.job.Filter;
import de.avgl.dmp.persistence.model.job.Mapping;
import de.avgl.dmp.persistence.model.schema.AttributePath;
import de.avgl.dmp.persistence.service.job.MappingService;

/**
 * @author tgaengler
 * @param <POJOCLASSPERSISTENCESERVICE>
 * @param <POJOCLASS>
 * @param <POJOCLASSIDTYPE>
 */
public class MappingsResourceUtils extends BasicDMPResourceUtils<MappingService, Mapping> {

	private static final org.apache.log4j.Logger		LOG	= org.apache.log4j.Logger.getLogger(MappingsResourceUtils.class);

	private final Provider<ComponentsResourceUtils>		componentsResourceUtilsProvider;

	private final Provider<AttributePathsResourceUtils>	attributePathsResourceUtilsProvider;

	private final Provider<FiltersResourceUtils>		filtersResourceUtilsProvider;

	@Inject
	public MappingsResourceUtils(final Provider<MappingService> persistenceServiceProviderArg, final Provider<ObjectMapper> objectMapperProviderArg,
			final Provider<ComponentsResourceUtils> componentsResourceUtilsProviderArg,
			final Provider<AttributePathsResourceUtils> attributePathsResourceUtilsProviderArg,
			final Provider<FiltersResourceUtils> filtersResourceUtilsProviderArg) {

		super(Mapping.class, persistenceServiceProviderArg, objectMapperProviderArg);

		componentsResourceUtilsProvider = componentsResourceUtilsProviderArg;
		attributePathsResourceUtilsProvider = attributePathsResourceUtilsProviderArg;
		filtersResourceUtilsProvider = filtersResourceUtilsProviderArg;
	}

	@Override
	public JsonNode replaceRelevantDummyIds(final Mapping object, final JsonNode jsonNode, final Set<Long> dummyIdCandidates)
			throws DMPControllerException {

		if (checkObject(object, dummyIdCandidates)) {

			return jsonNode;
		}

		super.replaceRelevantDummyIds(object, jsonNode, dummyIdCandidates);

		final Set<AttributePath> inputAttributePaths = object.getInputAttributePaths();

		if (inputAttributePaths != null) {

			for (final AttributePath inputAttributePath : inputAttributePaths) {

				if (replaceRelevantDummyIdsInAttributePath(inputAttributePath, jsonNode, dummyIdCandidates)) {

					return jsonNode;
				}
			}
		}

		final AttributePath outputAttributePath = object.getOutputAttributePath();

		if (replaceRelevantDummyIdsInAttributePath(outputAttributePath, jsonNode, dummyIdCandidates)) {

			return jsonNode;
		}

		final Filter inputFilter = object.getInputFilter();

		if (replaceRelevantDummyIdsInFilter(inputFilter, jsonNode, dummyIdCandidates)) {

			return jsonNode;
		}

		final Filter outputFilter = object.getOutputFilter();

		if (replaceRelevantDummyIdsInFilter(outputFilter, jsonNode, dummyIdCandidates)) {

			return jsonNode;
		}

		final Component transformationComponent = object.getTransformation();

		if (transformationComponent != null) {

			if (areDummyIdCandidatesEmpty(dummyIdCandidates)) {

				return jsonNode;
			}

			componentsResourceUtilsProvider.get().replaceRelevantDummyIds(transformationComponent, jsonNode, dummyIdCandidates);
		}

		return jsonNode;
	}

	private boolean replaceRelevantDummyIdsInAttributePath(final AttributePath attributePath, final JsonNode jsonNode,
			final Set<Long> dummyIdCandidates) throws DMPControllerException {

		if (attributePath != null) {

			if (areDummyIdCandidatesEmpty(dummyIdCandidates)) {

				return true;
			}

			attributePathsResourceUtilsProvider.get().replaceRelevantDummyIds(attributePath, jsonNode, dummyIdCandidates);
		}

		return false;
	}

	private boolean replaceRelevantDummyIdsInFilter(final Filter filter, final JsonNode jsonNode, final Set<Long> dummyIdCandidates)
			throws DMPControllerException {

		if (filter != null) {

			if (areDummyIdCandidatesEmpty(dummyIdCandidates)) {

				return true;
			}

			filtersResourceUtilsProvider.get().replaceRelevantDummyIds(filter, jsonNode, dummyIdCandidates);
		}

		return false;
	}
}
