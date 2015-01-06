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
package org.dswarm.controller.resources.utils;

import javax.inject.Provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.dswarm.persistence.model.DMPJPAObject;
import org.dswarm.persistence.model.proxy.ProxyDMPJPAObject;
import org.dswarm.persistence.service.BasicIDJPAService;
import org.dswarm.persistence.util.DMPPersistenceUtil;

/**
 * @author tgaengler
 * @param <POJOCLASSPERSISTENCESERVICE>
 * @param <POJOCLASS>
 * @param <POJOCLASSIDTYPE>
 */
public abstract class BasicIDResourceUtils<POJOCLASSPERSISTENCESERVICE extends BasicIDJPAService<PROXYPOJOCLASS, POJOCLASS>, PROXYPOJOCLASS extends ProxyDMPJPAObject<POJOCLASS>, POJOCLASS extends DMPJPAObject>
		extends BasicResourceUtils<POJOCLASSPERSISTENCESERVICE, PROXYPOJOCLASS, POJOCLASS, Long> {

	public BasicIDResourceUtils(final Class<POJOCLASS> pojoClassArg, final Provider<POJOCLASSPERSISTENCESERVICE> persistenceServiceProviderArg,
			final Provider<ObjectMapper> objectMapperProviderArg, final ResourceUtilsFactory utilsFactory) {

		super(pojoClassArg, Long.class, persistenceServiceProviderArg, objectMapperProviderArg, utilsFactory);
	}

	@Override
	protected ObjectNode replaceDummyId(final JsonNode idNode, final Long dummyId, final Long realId, final ObjectNode objectJSON) {

		if (idNode.canConvertToLong()) {

			final long longId = idNode.asLong();

			if (Long.class.isAssignableFrom(pojoClassIdType) && dummyId.equals(Long.valueOf(longId))) {

				// replace long id

				objectJSON.put("id", Long.valueOf(realId.toString()));
			}
		}

		return objectJSON;
	}

	@Override
	protected void checkObjectId(final JsonNode idNode) {

		if (idNode.canConvertToLong()) {

			final long longId = idNode.asLong();

			if (longId < 0) {

				addDummyIdCandidate(longId);
			}
		}
	}

	@Override
	protected ObjectNode addDummyId(final ObjectNode objectJSON) {

		final long randomDummyId = DMPPersistenceUtil.generateRandomDummyId();

		// add dummy id to object
		objectJSON.put("id", randomDummyId);

		return objectJSON;
	}
}
