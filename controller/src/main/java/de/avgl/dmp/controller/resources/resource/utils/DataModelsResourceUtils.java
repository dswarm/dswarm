package de.avgl.dmp.controller.resources.resource.utils;

import java.util.Set;

import javax.inject.Provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import de.avgl.dmp.controller.DMPControllerException;
import de.avgl.dmp.controller.resources.schema.utils.SchemasResourceUtils;
import de.avgl.dmp.controller.resources.utils.ExtendedBasicDMPResourceUtils;
import de.avgl.dmp.controller.resources.utils.ResourceUtilsFactory;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.DataModel;
import de.avgl.dmp.persistence.model.resource.Resource;
import de.avgl.dmp.persistence.model.resource.proxy.ProxyDataModel;
import de.avgl.dmp.persistence.model.schema.Schema;
import de.avgl.dmp.persistence.service.resource.DataModelService;

/**
 * @author tgaengler
 * @param <POJOCLASSPERSISTENCESERVICE>
 * @param <POJOCLASS>
 * @param <POJOCLASSIDTYPE>
 */
public class DataModelsResourceUtils extends ExtendedBasicDMPResourceUtils<DataModelService, ProxyDataModel, DataModel> {

	@Inject
	public DataModelsResourceUtils(final Provider<DataModelService> persistenceServiceProviderArg,
			final Provider<ObjectMapper> objectMapperProviderArg, final ResourceUtilsFactory utilsFactory) {

		super(DataModel.class, persistenceServiceProviderArg, objectMapperProviderArg, utilsFactory);
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

			utilsFactory.get(ResourcesResourceUtils.class).replaceRelevantDummyIds(resource, jsonNode, dummyIdCandidates);
		}

		final Configuration configuration = object.getConfiguration();

		if (configuration != null) {

			if (areDummyIdCandidatesEmpty(dummyIdCandidates)) {

				return jsonNode;
			}

			utilsFactory.get(ConfigurationsResourceUtils.class).replaceRelevantDummyIds(configuration, jsonNode, dummyIdCandidates);
		}

		final Schema schema = object.getSchema();

		if (schema != null) {

			if (areDummyIdCandidatesEmpty(dummyIdCandidates)) {

				return jsonNode;
			}

			utilsFactory.get(SchemasResourceUtils.class).replaceRelevantDummyIds(schema, jsonNode, dummyIdCandidates);
		}

		return jsonNode;
	}

}
