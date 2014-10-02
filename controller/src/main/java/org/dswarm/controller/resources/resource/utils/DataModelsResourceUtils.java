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
package org.dswarm.controller.resources.resource.utils;

import java.util.Set;

import javax.inject.Provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import org.dswarm.controller.DMPControllerException;
import org.dswarm.controller.resources.schema.utils.SchemasResourceUtils;
import org.dswarm.controller.resources.utils.ExtendedBasicDMPResourceUtils;
import org.dswarm.controller.resources.utils.ResourceUtilsFactory;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.resource.Resource;
import org.dswarm.persistence.model.resource.proxy.ProxyDataModel;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.service.resource.DataModelService;

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
