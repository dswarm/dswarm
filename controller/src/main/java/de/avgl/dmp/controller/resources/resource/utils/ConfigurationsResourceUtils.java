package de.avgl.dmp.controller.resources.resource.utils;

import java.util.Set;

import javax.inject.Provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import de.avgl.dmp.controller.DMPControllerException;
import de.avgl.dmp.controller.resources.utils.ExtendedBasicDMPResourceUtils;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.Resource;
import de.avgl.dmp.persistence.service.resource.ConfigurationService;

/**
 * @author tgaengler
 * @param <POJOCLASSPERSISTENCESERVICE>
 * @param <POJOCLASS>
 * @param <POJOCLASSIDTYPE>
 */
public class ConfigurationsResourceUtils extends ExtendedBasicDMPResourceUtils<ConfigurationService, Configuration> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(ConfigurationsResourceUtils.class);

	private final Provider<ResourcesResourceUtils>	resourcesResourceUtilsProvider;

	@Inject
	public ConfigurationsResourceUtils(final Provider<ConfigurationService> persistenceServiceProviderArg,
			final Provider<ObjectMapper> objectMapperProviderArg, final Provider<ResourcesResourceUtils> resourcesResourceUtilsProviderArg) {

		super(Configuration.class, persistenceServiceProviderArg, objectMapperProviderArg);

		resourcesResourceUtilsProvider = resourcesResourceUtilsProviderArg;
	}

	@Override
	public JsonNode replaceRelevantDummyIds(final Configuration object, final JsonNode jsonNode, final Set<Long> dummyIdCandidates)
			throws DMPControllerException {

		if (checkObject(object, dummyIdCandidates)) {

			return jsonNode;
		}

		super.replaceRelevantDummyIds(object, jsonNode, dummyIdCandidates);

		final Set<Resource> resources = object.getResources();

		if (resources != null) {

			for (final Resource resource : resources) {

				if (areDummyIdCandidatesEmpty(dummyIdCandidates)) {

					return jsonNode;
				}

				resourcesResourceUtilsProvider.get().replaceRelevantDummyIds(resource, jsonNode, dummyIdCandidates);
			}
		}

		return jsonNode;
	}
	
	@Override
	public String prepareObjectJSONString(final String objectJSONString) throws DMPControllerException {

		// a configuration is not a complex object

		return objectJSONString;
	}
}
