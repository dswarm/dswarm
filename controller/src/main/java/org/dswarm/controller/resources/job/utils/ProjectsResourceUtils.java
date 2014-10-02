/**
 * Copyright (C) 2013, 2014 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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
import org.dswarm.controller.resources.resource.utils.DataModelsResourceUtils;
import org.dswarm.controller.resources.utils.ExtendedBasicDMPResourceUtils;
import org.dswarm.controller.resources.utils.ResourceUtilsFactory;
import org.dswarm.persistence.model.job.Function;
import org.dswarm.persistence.model.job.Mapping;
import org.dswarm.persistence.model.job.Project;
import org.dswarm.persistence.model.job.proxy.ProxyProject;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.service.job.ProjectService;

/**
 * @author tgaengler
 * @param <POJOCLASSPERSISTENCESERVICE>
 * @param <POJOCLASS>
 * @param <POJOCLASSIDTYPE>
 */
public class ProjectsResourceUtils extends ExtendedBasicDMPResourceUtils<ProjectService, ProxyProject, Project> {

	@Inject
	public ProjectsResourceUtils(final Provider<ProjectService> persistenceServiceProviderArg, final Provider<ObjectMapper> objectMapperProviderArg,
			final ResourceUtilsFactory utilsFactory) {
		super(Project.class, persistenceServiceProviderArg, objectMapperProviderArg, utilsFactory);
	}

	@Override
	public JsonNode replaceRelevantDummyIds(final Project object, final JsonNode jsonNode, final Set<Long> dummyIdCandidates)
			throws DMPControllerException {

		if (checkObject(object, dummyIdCandidates)) {

			return jsonNode;
		}

		super.replaceRelevantDummyIds(object, jsonNode, dummyIdCandidates);

		final DataModel inputDataModel = object.getInputDataModel();

		if (replaceRelevantDummyIdsInDataModel(inputDataModel, jsonNode, dummyIdCandidates)) {

			return jsonNode;
		}

		final DataModel outputDataModel = object.getOutputDataModel();

		if (replaceRelevantDummyIdsInDataModel(outputDataModel, jsonNode, dummyIdCandidates)) {

			return jsonNode;
		}

		final Set<Mapping> mappings = object.getMappings();

		if (mappings != null) {

			for (final Mapping mapping : mappings) {

				if (areDummyIdCandidatesEmpty(dummyIdCandidates)) {

					return jsonNode;
				}

				utilsFactory.get(MappingsResourceUtils.class).replaceRelevantDummyIds(mapping, jsonNode, dummyIdCandidates);
			}
		}

		final Set<Function> functions = object.getFunctions();

		if (functions != null) {

			for (final Function function : functions) {

				if (areDummyIdCandidatesEmpty(dummyIdCandidates)) {

					return jsonNode;
				}

				utilsFactory.get(FunctionsResourceUtils.class).replaceRelevantDummyIds(function, jsonNode, dummyIdCandidates);
			}
		}

		return jsonNode;
	}

	private boolean replaceRelevantDummyIdsInDataModel(final DataModel dataModel, final JsonNode jsonNode, final Set<Long> dummyIdCandidates)
			throws DMPControllerException {

		if (dataModel != null) {

			if (areDummyIdCandidatesEmpty(dummyIdCandidates)) {

				return true;
			}

			utilsFactory.get(DataModelsResourceUtils.class).replaceRelevantDummyIds(dataModel, jsonNode, dummyIdCandidates);
		}

		return false;
	}
}
