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
package org.dswarm.controller.resources.job.utils;

import java.util.Set;

import javax.inject.Provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import org.dswarm.controller.DMPControllerException;
import org.dswarm.controller.resources.schema.utils.MappingAttributePathInstancesResourceUtils;
import org.dswarm.controller.resources.utils.BasicDMPResourceUtils;
import org.dswarm.controller.resources.utils.ResourceUtilsFactory;
import org.dswarm.persistence.model.job.Component;
import org.dswarm.persistence.model.job.Mapping;
import org.dswarm.persistence.model.job.proxy.ProxyMapping;
import org.dswarm.persistence.model.schema.MappingAttributePathInstance;
import org.dswarm.persistence.service.job.MappingService;

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
