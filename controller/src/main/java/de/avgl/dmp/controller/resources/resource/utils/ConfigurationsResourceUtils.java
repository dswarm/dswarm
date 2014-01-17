package de.avgl.dmp.controller.resources.resource.utils;

import java.util.Set;

import javax.inject.Provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import de.avgl.dmp.controller.DMPControllerException;
import de.avgl.dmp.controller.resources.utils.ExtendedBasicDMPResourceUtils;
import de.avgl.dmp.controller.resources.utils.ResourceUtilsFactory;
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

	@Inject
	public ConfigurationsResourceUtils(final ResourceUtilsFactory utilsFactory,
	                                   final Provider<ConfigurationService> persistenceServiceProviderArg,
	                                   final Provider<ObjectMapper> objectMapperProviderArg) {

		super(Configuration.class, persistenceServiceProviderArg, objectMapperProviderArg, utilsFactory);
	}

	@Override
	public JsonNode replaceRelevantDummyIds(final Configuration object, final JsonNode jsonNode, final Set<Long> dummyIdCandidates)
			throws DMPControllerException {

		if(checkObject(object, dummyIdCandidates)) {

			return jsonNode;
		}

		super.replaceRelevantDummyIds(object, jsonNode, dummyIdCandidates);

		final Set<Resource> resources = object.getResources();

		if (resources != null) {

			for (final Resource resource : resources) {

				if (areDummyIdCandidatesEmpty(dummyIdCandidates)) {

					return jsonNode;
				}

				utilsFactory.get(ResourcesResourceUtils.class).replaceRelevantDummyIds(resource, jsonNode, dummyIdCandidates);
			}
		}

		return jsonNode;
	}
}
