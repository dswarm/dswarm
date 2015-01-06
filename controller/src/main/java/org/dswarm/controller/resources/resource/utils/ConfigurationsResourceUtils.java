/**
 * Copyright (C) 2013 – 2015 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dswarm.controller.resources.resource.utils;

import java.util.Set;

import javax.inject.Provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import org.dswarm.controller.DMPControllerException;
import org.dswarm.controller.resources.utils.ExtendedBasicDMPResourceUtils;
import org.dswarm.controller.resources.utils.ResourceUtilsFactory;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.Resource;
import org.dswarm.persistence.model.resource.proxy.ProxyConfiguration;
import org.dswarm.persistence.service.resource.ConfigurationService;

/**
 * @author tgaengler
 * @param <POJOCLASSPERSISTENCESERVICE>
 * @param <POJOCLASS>
 * @param <POJOCLASSIDTYPE>
 */
public class ConfigurationsResourceUtils extends ExtendedBasicDMPResourceUtils<ConfigurationService, ProxyConfiguration, Configuration> {

	@Inject
	public ConfigurationsResourceUtils(final ResourceUtilsFactory utilsFactory, final Provider<ConfigurationService> persistenceServiceProviderArg,
			final Provider<ObjectMapper> objectMapperProviderArg) {

		super(Configuration.class, persistenceServiceProviderArg, objectMapperProviderArg, utilsFactory);
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

				utilsFactory.get(ResourcesResourceUtils.class).replaceRelevantDummyIds(resource, jsonNode, dummyIdCandidates);
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
