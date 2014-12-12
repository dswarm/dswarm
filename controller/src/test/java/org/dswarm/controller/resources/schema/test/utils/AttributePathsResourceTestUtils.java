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
package org.dswarm.controller.resources.schema.test.utils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.dswarm.controller.resources.test.utils.BasicResourceTestUtils;
import org.dswarm.persistence.model.schema.Attribute;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.proxy.ProxyAttributePath;
import org.dswarm.persistence.service.schema.AttributePathService;
import org.dswarm.persistence.service.schema.test.utils.AttributePathServiceTestUtils;
import org.dswarm.persistence.util.DMPPersistenceUtil;

public class AttributePathsResourceTestUtils extends
		BasicResourceTestUtils<AttributePathServiceTestUtils, AttributePathService, ProxyAttributePath, AttributePath, Long> {

	public AttributePathsResourceTestUtils() {

		super("attributepaths", AttributePath.class, AttributePathService.class, AttributePathServiceTestUtils.class);
	}

	@Override
	public void compareObjects(final AttributePath expectedObject, final AttributePath actualObject) {

		// note: expected object need to be patched here

		// attribute path was already retrieved by attribute path string (maybe + attributes where created, because they
		// didn't exist before)
		// => replace dummy id'ed attributes with real ids by attribute uri

		final List<Attribute> attributePath = expectedObject.getAttributePath();

		if (attributePath != null) {

			final Set<Attribute> persistentAttributes = actualObject.getAttributes();

			if (persistentAttributes != null) {

				final Set<String> attributeURIsFromDummyIdsFromObjectFromJSON = Sets.newHashSet();
				final Map<String, Attribute> attributeFromRealIdsFromObject = Maps.newHashMap();

				// collect uris of attributes with dummy id

				for (final Attribute attribute : attributePath) {

					// note: one could even collect all attribute ids and replace them by their actual ones

					if (attribute.getId() < 0) {

						attributeURIsFromDummyIdsFromObjectFromJSON.add(attribute.getUri());
					}
				}

				// collect attributes that match the uris of the attribute with dummy id

				for (final Attribute attribute : persistentAttributes) {

					if (attributeURIsFromDummyIdsFromObjectFromJSON.contains(attribute.getUri())) {

						attributeFromRealIdsFromObject.put(attribute.getUri(), attribute);
					}
				}

				final List<Attribute> newAttributePath = Lists.newLinkedList();

				// construct new attribute path

				for (final Attribute attribute : attributePath) {

					final Attribute newAttribute = attributeFromRealIdsFromObject.get(attribute.getUri());

					if (newAttribute == null) {

						newAttributePath.add(attribute);
					} else {

						newAttributePath.add(newAttribute);
					}
				}

				expectedObject.setAttributePath(newAttributePath);
			}
		}

		super.compareObjects(expectedObject, actualObject);
	}

	public AttributePath prepareAttributePath(final String attributePathJSONFileName, final Map<Long, AttributePath> attributePaths,
			final Map<Long, Attribute> attributes) throws Exception {

		String attributePathJSONString = DMPPersistenceUtil.getResourceAsString(attributePathJSONFileName);
		final AttributePath attributePath = objectMapper.readValue(attributePathJSONString, AttributePath.class);

		final Iterable<Attribute> attributePathAttributes = attributePath.getAttributePath();
		final List<Attribute> newAttributes = Lists.newLinkedList();

		for (final Attribute attribute : attributePathAttributes) {
			newAttributes.addAll(findAttribute(attributes.values(), attribute).asSet());
		}

		attributePath.setAttributePath(newAttributes);

		attributePathJSONString = objectMapper.writeValueAsString(attributePath);
		final AttributePath expectedAttributePath = objectMapper.readValue(attributePathJSONString, AttributePath.class);
		final AttributePath actualAttributePath = createObject(attributePathJSONString, expectedAttributePath);

		attributePaths.put(actualAttributePath.getId(), actualAttributePath);

		return actualAttributePath;
	}

	private static Optional<Attribute> findAttribute(final Iterable<Attribute> haystack, final Attribute needle) {
		for (final Attribute attribute : haystack) {
			if (attribute.getUri().equals(needle.getUri())) {
				return Optional.of(attribute);
			}
		}
		return Optional.absent();
	}
}
