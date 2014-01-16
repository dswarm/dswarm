package de.avgl.dmp.controller.resources.resource.utils;

import java.util.Set;

import javax.inject.Provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import de.avgl.dmp.controller.DMPControllerException;
import de.avgl.dmp.controller.resources.schema.utils.SchemasResourceUtils;
import de.avgl.dmp.controller.resources.utils.ExtendedBasicDMPResourceUtils;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.DataModel;
import de.avgl.dmp.persistence.model.resource.Resource;
import de.avgl.dmp.persistence.model.schema.Schema;
import de.avgl.dmp.persistence.service.resource.DataModelService;

/**
 * @author tgaengler
 * @param <POJOCLASSPERSISTENCESERVICE>
 * @param <POJOCLASS>
 * @param <POJOCLASSIDTYPE>
 */
public class DataModelsResourceUtils extends ExtendedBasicDMPResourceUtils<DataModelService, DataModel> {

	private static final org.apache.log4j.Logger		LOG	= org.apache.log4j.Logger.getLogger(DataModelsResourceUtils.class);

	private final Provider<ResourcesResourceUtils>		resourcesResourceUtilsProvider;

	private final Provider<ConfigurationsResourceUtils>	configurationsResourceUtilsProvider;

	private final Provider<SchemasResourceUtils>		schemasResourceUtilsProvider;

	@Inject
	public DataModelsResourceUtils(final Provider<DataModelService> persistenceServiceProviderArg,
			final Provider<ObjectMapper> objectMapperProviderArg, final Provider<ResourcesResourceUtils> resourcesResourceUtilsProviderArg,
			final Provider<ConfigurationsResourceUtils> configurationsResourceUtilsProviderArg,
			final Provider<SchemasResourceUtils> schemasResourceUtilsProviderArg) {

		super(DataModel.class, persistenceServiceProviderArg, objectMapperProviderArg);

		resourcesResourceUtilsProvider = resourcesResourceUtilsProviderArg;
		configurationsResourceUtilsProvider = configurationsResourceUtilsProviderArg;
		schemasResourceUtilsProvider = schemasResourceUtilsProviderArg;
	}

	@Override
	public JsonNode replaceRelevantDummyIds(final DataModel object, final JsonNode jsonNode, final Set<Long> dummyIdCandidates)
			throws DMPControllerException {

		if (checkObject(object, dummyIdCandidates)) {

			return jsonNode;
		}

		super.replaceRelevantDummyIds(object, jsonNode, dummyIdCandidates);

		final Resource resource = object.getDataResource();

		if (resource != null) {

			if (areDummyIdCandidatesEmpty(dummyIdCandidates)) {

				return jsonNode;
			}

			resourcesResourceUtilsProvider.get().replaceRelevantDummyIds(resource, jsonNode, dummyIdCandidates);
		}

		final Configuration configuration = object.getConfiguration();

		if (configuration != null) {

			if (areDummyIdCandidatesEmpty(dummyIdCandidates)) {

				return jsonNode;
			}

			configurationsResourceUtilsProvider.get().replaceRelevantDummyIds(configuration, jsonNode, dummyIdCandidates);
		}

		final Schema schema = object.getSchema();

		if (schema != null) {

			if (areDummyIdCandidatesEmpty(dummyIdCandidates)) {

				return jsonNode;
			}

			schemasResourceUtilsProvider.get().replaceRelevantDummyIds(schema, jsonNode, dummyIdCandidates);
		}

		return jsonNode;
	}

}
