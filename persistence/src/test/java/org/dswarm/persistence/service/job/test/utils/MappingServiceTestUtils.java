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
package org.dswarm.persistence.service.job.test.utils;

import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.json.JSONException;
import org.junit.Assert;

import org.dswarm.persistence.model.job.Component;
import org.dswarm.persistence.model.job.Mapping;
import org.dswarm.persistence.model.job.proxy.ProxyMapping;
import org.dswarm.persistence.model.schema.MappingAttributePathInstance;
import org.dswarm.persistence.service.job.MappingService;
import org.dswarm.persistence.service.schema.test.utils.MappingAttributePathInstanceServiceTestUtils;
import org.dswarm.persistence.service.test.utils.BasicDMPJPAServiceTestUtils;

public class MappingServiceTestUtils extends BasicDMPJPAServiceTestUtils<MappingService, ProxyMapping, Mapping> {

	private final ComponentServiceTestUtils componentServiceTestUtils;

	private final MappingAttributePathInstanceServiceTestUtils mappingAttributePathInstanceServiceTestUtils;

	private final Set<Long> checkedExpectedAttributePaths = Sets.newHashSet();

	private final Set<Long> checkedActualAttributePaths = Sets.newHashSet();

	public MappingServiceTestUtils() {

		super(Mapping.class, MappingService.class);

		componentServiceTestUtils = new ComponentServiceTestUtils();
		mappingAttributePathInstanceServiceTestUtils = new MappingAttributePathInstanceServiceTestUtils();
	}

	@Override public Mapping createObject(JsonNode objectDescription) throws Exception {
		return null;
	}

	@Override public Mapping createObject(String identifier) throws Exception {
		return null;
	}

	@Override public Mapping createAndPersistDefaultObject() throws Exception {

		final Mapping mapping = createDefaultObject();

		return createAndCompareObject(mapping, mapping);
	}

	@Override public Mapping createDefaultObject() throws Exception {
		final MappingAttributePathInstance inputAttributePath = mappingAttributePathInstanceServiceTestUtils.getDefaultInputMAPI();
		final MappingAttributePathInstance outputAttributePath = mappingAttributePathInstanceServiceTestUtils.getDefaultOutputMAPI();

		final Component transformationComponent = componentServiceTestUtils
				.getTransformationComponentSimpleTrimComponent(inputAttributePath.getAttributePath().toAttributePath(),
						outputAttributePath.getAttributePath().toAttributePath());

		final String mappingName = "my mapping";

		final Mapping mapping = new Mapping();
		mapping.setName(mappingName);
		mapping.addInputAttributePath(inputAttributePath);
		mapping.setOutputAttributePath(outputAttributePath);
		mapping.setTransformation(transformationComponent);

		return mapping;
	}

	@Override public Mapping createAndPersistDefaultCompleteObject() throws Exception {

		final MappingAttributePathInstance firstNameMappingAttributePathInstance = mappingAttributePathInstanceServiceTestUtils
				.getDctermsCreatorFoafFirstnameMAPI();
		final MappingAttributePathInstance familyNameMappingAttributePathInstance = mappingAttributePathInstanceServiceTestUtils
				.getDctermsCreatorFoafFamilynameMAPI();
		final MappingAttributePathInstance outputMappingAttributePathInstance = mappingAttributePathInstanceServiceTestUtils
				.getDctermsCreatorFoafNameMAPI();

		final Component transformationComponent3 = componentServiceTestUtils
				.getComplexTransformationComponent(firstNameMappingAttributePathInstance.getAttributePath().toAttributePath(),
						familyNameMappingAttributePathInstance.getAttributePath().toAttributePath(),
						outputMappingAttributePathInstance.getAttributePath().toAttributePath());

		final String mappingName = "my mapping";

		final Mapping mapping = new Mapping();
		mapping.setName(mappingName);
		mapping.addInputAttributePath(firstNameMappingAttributePathInstance);
		mapping.addInputAttributePath(familyNameMappingAttributePathInstance);
		mapping.setOutputAttributePath(outputMappingAttributePathInstance);
		mapping.setTransformation(transformationComponent3);

		return createAndCompareObject(mapping, mapping);
	}

	/**
	 * {@inheritDoc} <br />
	 * Assert that both mappings either have no or equal (transformation) components, see
	 * {@link ComponentServiceTestUtils#compareObjects(Component, Component)}. <br />
	 * Assert that both mappings either have no or equal input attribute paths, see {@link
	 * org.dswarm.persistence.service.test.utils.BasicJPAServiceTestUtils#compareObjects(java.util.Set, java.util.Map)}. <br />
	 * Assert that both mappings either have no or equal output attribute paths, see {@link
	 * org.dswarm.persistence.service.test.utils.BasicJPAServiceTestUtils#compareObjects(java.util.Set, java.util.Map)} . <br />
	 */
	@Override
	public void compareObjects(final Mapping expectedMapping, final Mapping actualMapping) throws JsonProcessingException, JSONException {

		super.compareObjects(expectedMapping, actualMapping);

		// transformation (component)
		if (expectedMapping.getTransformation() == null) {

			Assert.assertNull("the actual mapping '" + actualMapping.getId() + "' shouldn't have a transformation",
					actualMapping.getTransformation());

		} else {
			componentServiceTestUtils.compareObjects(expectedMapping.getTransformation(), actualMapping.getTransformation());
		}

		// input attribute paths
		if (expectedMapping.getInputAttributePaths() == null || expectedMapping.getInputAttributePaths().isEmpty()) {

			final boolean actualMappingHasNoInputAttributePaths = (actualMapping.getInputAttributePaths() == null || actualMapping
					.getInputAttributePaths().isEmpty());
			Assert.assertTrue("actual mapping '" + actualMapping.getId() + "' shouldn't have input attribute paths",
					actualMappingHasNoInputAttributePaths);

		} else { // !null && !empty

			final Set<MappingAttributePathInstance> actualInputAttributePaths = actualMapping.getInputAttributePaths();

			Assert.assertNotNull("input attribute paths of actual mapping '" + actualMapping.getId() + "' shouldn't be null",
					actualInputAttributePaths);
			Assert.assertFalse("input attribute paths of actual mapping '" + actualMapping.getId() + "' shouldn't be empty",
					actualInputAttributePaths.isEmpty());

			final Map<Long, MappingAttributePathInstance> actualInputAttributePathsMap = Maps.newHashMap();

			for (final MappingAttributePathInstance actualInputAttributePath : actualInputAttributePaths) {

				if (checkAttributePath(actualInputAttributePath, checkedActualAttributePaths)) {

					continue;
				}

				actualInputAttributePathsMap.put(actualInputAttributePath.getId(), actualInputAttributePath);
			}

			final Set<MappingAttributePathInstance> uncheckedExpectedInputAttributePaths = Sets.newHashSet();

			for (final MappingAttributePathInstance expectedInputAttributePath : expectedMapping.getInputAttributePaths()) {

				if (checkAttributePath(expectedInputAttributePath, checkedExpectedAttributePaths)) {

					continue;
				}

				uncheckedExpectedInputAttributePaths.add(expectedInputAttributePath);

			}

			mappingAttributePathInstanceServiceTestUtils.compareObjects(uncheckedExpectedInputAttributePaths, actualInputAttributePathsMap);
		}

		// output attribute paths
		if (expectedMapping.getOutputAttributePath() == null) {

			Assert.assertNull("actual mapping '" + actualMapping.getId() + "' shouldn't have an output attribute path",
					actualMapping.getOutputAttributePath());

		} else if (!checkAttributePath(expectedMapping.getOutputAttributePath(), checkedExpectedAttributePaths)
				&& !checkAttributePath(actualMapping.getOutputAttributePath(), checkedActualAttributePaths)) {

			mappingAttributePathInstanceServiceTestUtils.compareObjects(expectedMapping.getOutputAttributePath(),
					actualMapping.getOutputAttributePath());

		}
	}

	private boolean checkAttributePath(final MappingAttributePathInstance attributePath, final Set<Long> checkedAttributePaths) {

		if (attributePath != null && attributePath.getId() != null) {

			if (checkedAttributePaths.contains(attributePath.getId())) {

				// attribute path was already checked

				return true;
			}

			checkedAttributePaths.add(attributePath.getId());
		}

		return false;
	}

	/**
	 * {@inheritDoc}<br/>
	 * Updates the name, transformation (component), input attribute paths and output attribute path of the mapping.
	 */
	@Override
	protected Mapping prepareObjectForUpdate(final Mapping objectWithUpdates, final Mapping object) {

		super.prepareObjectForUpdate(objectWithUpdates, object);

		object.setTransformation(objectWithUpdates.getTransformation());
		object.setInputAttributePaths(objectWithUpdates.getInputAttributePaths());
		object.setOutputAttributePath(objectWithUpdates.getOutputAttributePath());

		return object;
	}

	@Override
	public void reset() {

		checkedExpectedAttributePaths.clear();
		checkedActualAttributePaths.clear();

		mappingAttributePathInstanceServiceTestUtils.reset();
		componentServiceTestUtils.reset();
	}
}
