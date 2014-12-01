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
package org.dswarm.controller.resources.schema.utils;

import java.util.Set;

import javax.inject.Provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import org.dswarm.controller.DMPControllerException;
import org.dswarm.controller.resources.utils.ResourceUtilsFactory;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.model.schema.SchemaAttributePathInstance;
import org.dswarm.persistence.model.schema.proxy.ProxySchemaAttributePathInstance;
import org.dswarm.persistence.service.schema.SchemaAttributePathInstanceService;

/**
 * @author tgaengler
 */
public class SchemaAttributePathInstancesResourceUtils extends
		AttributePathInstancesResourceUtils<SchemaAttributePathInstanceService, ProxySchemaAttributePathInstance, SchemaAttributePathInstance> {

	@Inject
	public SchemaAttributePathInstancesResourceUtils(final Provider<SchemaAttributePathInstanceService> persistenceServiceProviderArg,
			final Provider<ObjectMapper> objectMapperProviderArg, final ResourceUtilsFactory utilsFactory) {

		super(SchemaAttributePathInstance.class, persistenceServiceProviderArg, objectMapperProviderArg, utilsFactory);
	}

	@Override
	public JsonNode replaceRelevantDummyIds(final SchemaAttributePathInstance object, final JsonNode jsonNode, final Set<Long> dummyIdCandidates)
			throws DMPControllerException {

		if (checkObject(object, dummyIdCandidates)) {

			return jsonNode;
		}

		super.replaceRelevantDummyIds(object, jsonNode, dummyIdCandidates);

		final Schema subSchema = object.getSubSchema();

		if (subSchema != null) {

			if (areDummyIdCandidatesEmpty(dummyIdCandidates)) {

				return jsonNode;
			}

			utilsFactory.get(SchemasResourceUtils.class).replaceRelevantDummyIds(subSchema, jsonNode, dummyIdCandidates);
		}

		return jsonNode;
	}
}
