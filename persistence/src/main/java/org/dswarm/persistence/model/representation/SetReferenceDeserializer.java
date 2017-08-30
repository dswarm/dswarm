/**
 * Copyright (C) 2013 – 2017 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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
package org.dswarm.persistence.model.representation;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.inject.Key;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.persistence.model.DMPObject;
import org.dswarm.persistence.model.proxy.ProxyDMPObject;
import org.dswarm.persistence.service.BasicJPAService;
import org.dswarm.persistence.util.DMPPersistenceUtil;

/**
 * @param <DMPOBJECTIMPL> the concrete model class
 * @author tgaengler
 */
abstract class SetReferenceDeserializer<JPASERVICEIMPL extends BasicJPAService<PROXYDMPOBJECTIMPL, DMPOBJECTIMPL>, PROXYDMPOBJECTIMPL extends ProxyDMPObject<DMPOBJECTIMPL>, DMPOBJECTIMPL extends DMPObject>
		extends JsonDeserializer<Set<DMPOBJECTIMPL>> {

	private static final Logger LOG = LoggerFactory.getLogger(SetReferenceDeserializer.class);

	private static final String UUID_KEY = "uuid";

	protected abstract JPASERVICEIMPL getJpaService(final DeserializationContext deserializationContext) throws IllegalStateException;

	protected abstract DMPOBJECTIMPL getNewObject(final String uuid);

	@Override
	public Set<DMPOBJECTIMPL> deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException {

		final ArrayNode arrayNode = DMPPersistenceUtil.getJSONObjectMapper().readValue(jp, ArrayNode.class);

		if (arrayNode == null) {

			throw new IOException("couldn't parse JSON array");
		}

		if (arrayNode.size() <= 0) {

			return null;
		}

		Optional<BasicJPAService<PROXYDMPOBJECTIMPL, DMPOBJECTIMPL>> optionalJPAService;

		try {

			optionalJPAService = Optional.ofNullable(getJpaService(ctxt));
		} catch (final IllegalStateException e) {

			SetReferenceDeserializer.LOG.error("Couldn't get persistence service, will try to deserialize the entities without DB access.", e);

			optionalJPAService = Optional.empty();
		}

		final Set<DMPOBJECTIMPL> set = Sets.newHashSet();

		for (final JsonNode reference : arrayNode) {

			if (reference == null) {

				SetReferenceDeserializer.LOG.debug("reference node is null");

				continue;
			}

			final JsonNode uuidNode = reference.get(UUID_KEY);

			if (uuidNode == null) {

				SetReferenceDeserializer.LOG.debug("uuid node is null");

				continue;
			}

			final String uuid = uuidNode.asText();

			if (uuid == null || uuid.trim().isEmpty()) {

				SetReferenceDeserializer.LOG.debug("uuid is null or empty");

				continue;
			}

			final DMPOBJECTIMPL object;

			if (optionalJPAService.isPresent()) {

				object = optionalJPAService.get().getObject(uuid);
			} else {

				object = getNewObject(uuid);
			}

			final DMPOBJECTIMPL finalObject;

			if (object != null) {

				finalObject = object;
			} else {

				SetReferenceDeserializer.LOG.debug("couldn't find " + optionalJPAService.get().getClasz().getSimpleName() + " with uuid '" + uuid
						+ "' in the DB, will simply create a new object with the uuid.");

				finalObject = getNewObject(uuid);
			}

			set.add(finalObject);
		}

		return set;
	}

	protected JPASERVICEIMPL getJPAService(final DeserializationContext deserializationContext,
			final Class<JPASERVICEIMPL> jpaServiceClass) throws IllegalStateException {

		Preconditions.checkNotNull(deserializationContext);

		final JPASERVICEIMPL jpaService = (JPASERVICEIMPL) deserializationContext.findInjectableValue(Key.get(jpaServiceClass), null, null);

		return Preconditions.checkNotNull(jpaService);
	}

}
