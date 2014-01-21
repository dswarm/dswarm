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
import de.avgl.dmp.persistence.service.resource.ResourceService;

/**
 * @author tgaengler
 * @param <POJOCLASSPERSISTENCESERVICE>
 * @param <POJOCLASS>
 * @param <POJOCLASSIDTYPE>
 */
public class ResourcesResourceUtils extends ExtendedBasicDMPResourceUtils<ResourceService, Resource> {

	private static final org.apache.log4j.Logger		LOG	= org.apache.log4j.Logger.getLogger(ResourcesResourceUtils.class);

	@Inject
	public ResourcesResourceUtils(final Provider<ResourceService> persistenceServiceProviderArg,
	                              final Provider<ObjectMapper> objectMapperProviderArg,
	                              final ResourceUtilsFactory utilsFactory) {

		super(Resource.class, persistenceServiceProviderArg, objectMapperProviderArg, utilsFactory);
	}

	@Override
	public JsonNode replaceRelevantDummyIds(final Resource object, final JsonNode jsonNode, final Set<Long> dummyIdCandidates)
			throws DMPControllerException {
		
		if(checkObject(object, dummyIdCandidates)) {
			
			return jsonNode;
		}

		super.replaceRelevantDummyIds(object, jsonNode, dummyIdCandidates);
		
		final Set<Configuration> configurations = object.getConfigurations();
		
		if(configurations != null) {
			
			for(final Configuration configuration : configurations) {
				
				if (areDummyIdCandidatesEmpty(dummyIdCandidates)) {

					return jsonNode;
				}

				utilsFactory.get(ConfigurationsResourceUtils.class).replaceRelevantDummyIds(configuration, jsonNode, dummyIdCandidates);
			}
		}

		return jsonNode;
	}
}
