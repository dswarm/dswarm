package de.avgl.dmp.controller.resources.schema.utils;

import java.util.Set;

import javax.inject.Provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import de.avgl.dmp.controller.DMPControllerException;
import de.avgl.dmp.controller.resources.job.utils.FiltersResourceUtils;
import de.avgl.dmp.controller.resources.utils.ResourceUtilsFactory;
import de.avgl.dmp.persistence.model.job.Filter;
import de.avgl.dmp.persistence.model.schema.MappingAttributePathInstance;
import de.avgl.dmp.persistence.model.schema.proxy.ProxyMappingAttributePathInstance;
import de.avgl.dmp.persistence.service.schema.MappingAttributePathInstanceService;

/**
 * @author tgaengler
 * @param <POJOCLASSPERSISTENCESERVICE>
 * @param <POJOCLASS>
 * @param <POJOCLASSIDTYPE>
 */
public class MappingAttributePathInstancesResourceUtils extends
		AttributePathInstancesResourceUtils<MappingAttributePathInstanceService, ProxyMappingAttributePathInstance, MappingAttributePathInstance> {

	@Inject
	public MappingAttributePathInstancesResourceUtils(final Provider<MappingAttributePathInstanceService> persistenceServiceProviderArg,
			final Provider<ObjectMapper> objectMapperProviderArg, final ResourceUtilsFactory utilsFactory) {

		super(MappingAttributePathInstance.class, persistenceServiceProviderArg, objectMapperProviderArg, utilsFactory);
	}

	@Override
	public JsonNode replaceRelevantDummyIds(final MappingAttributePathInstance object, final JsonNode jsonNode, final Set<Long> dummyIdCandidates)
			throws DMPControllerException {

		if (checkObject(object, dummyIdCandidates)) {

			return jsonNode;
		}

		super.replaceRelevantDummyIds(object, jsonNode, dummyIdCandidates);

		final Filter filter = object.getFilter();

		if (filter != null) {

			if (areDummyIdCandidatesEmpty(dummyIdCandidates)) {

				return jsonNode;
			}

			utilsFactory.get(FiltersResourceUtils.class).replaceRelevantDummyIds(filter, jsonNode, dummyIdCandidates);
		}

		return jsonNode;
	}
}
