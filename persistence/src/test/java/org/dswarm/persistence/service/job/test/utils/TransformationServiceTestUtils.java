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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.json.JSONException;
import org.junit.Assert;

import org.dswarm.persistence.model.job.Component;
import org.dswarm.persistence.model.job.Transformation;
import org.dswarm.persistence.model.job.proxy.ProxyTransformation;
import org.dswarm.persistence.service.job.TransformationService;

public class TransformationServiceTestUtils extends BasicFunctionServiceTestUtils<TransformationService, ProxyTransformation, Transformation> {

	private final ComponentServiceTestUtils componentServiceTestUtils;

	public TransformationServiceTestUtils() {

		super(Transformation.class, TransformationService.class);

		componentServiceTestUtils = new ComponentServiceTestUtils();
	}

	public TransformationServiceTestUtils(final ComponentServiceTestUtils componentsResourceTestUtilsArg) {

		super(Transformation.class, TransformationService.class);

		componentServiceTestUtils = componentsResourceTestUtilsArg;
	}

	@Override
	public Transformation createObject(final JsonNode objectDescription) throws Exception {
		return null;
	}

	@Override
	public Transformation createObject(final String identifier) throws Exception {
		return null;
	}

	@Override
	public Transformation createAndPersistDefaultObject() throws Exception {

		return getSimpleTrimTransformation();
	}

	@Override public Transformation createDefaultObject() throws Exception {
		return null;
	}

	/**
	 * note: result will be cache (temporarily - for re-utilisation in a test)
	 *
	 * @return
	 * @throws Exception
	 */
	@Override
	public Transformation createAndPeristDefaultCompleteObject() throws Exception {

		final String transformationName = "my transformation";

		if (!cache.containsKey(transformationName)) {

			final String transformationDescription = "transformation which just makes use of one function";
			final String transformationParameter = "transformationInputString";

			final Component component = componentServiceTestUtils.createAndPeristDefaultCompleteObject();

			final Set<Component> components = Sets.newLinkedHashSet();

			components.add(component.getInputComponents().iterator().next());
			components.add(component);
			components.add(component.getOutputComponents().iterator().next());

			final LinkedList<String> parameters = Lists.newLinkedList();
			parameters.add(transformationParameter);

			final Transformation transformation = createTransformation(transformationName, transformationDescription, components, parameters);

			cache.put(transformationName, transformation);
		}

		return cache.get(transformationName);
	}

	/**
	 * {@inheritDoc} <br />
	 * Assert both transformations have either no components or their components are equal, see
	 * {@link org.dswarm.persistence.service.test.utils.BasicJPAServiceTestUtils#compareObjects(Set, Map)} for details.
	 */
	@Override
	public void compareObjects(final Transformation expectedTransformation, final Transformation actualTransformation)
			throws JsonProcessingException, JSONException {

		super.compareObjects(expectedTransformation, actualTransformation);

		if (expectedTransformation.getComponents() == null || expectedTransformation.getComponents().isEmpty()) {

			final boolean actualTransformationHasNoComponents = (actualTransformation.getComponents() == null || actualTransformation.getComponents()
					.isEmpty());
			Assert.assertTrue("the actual transformation should not have any components", actualTransformationHasNoComponents);

		} else {
			// (!null && !empty)

			final Set<Component> actualComponents = actualTransformation.getComponents();

			Assert.assertNotNull("components of actual transformation '" + actualTransformation.getId() + "' shouldn't be null", actualComponents);
			Assert.assertFalse("components of actual transformation '" + actualTransformation.getId() + "' shouldn't be empty",
					actualComponents.isEmpty());

			final Map<Long, Component> actualComponentsMap = Maps.newHashMap();

			for (final Component actualComponent : actualComponents) {

				actualComponentsMap.put(actualComponent.getId(), actualComponent);
			}

			componentServiceTestUtils.compareObjects(expectedTransformation.getComponents(), actualComponentsMap);
		}
	}

	public Transformation createTransformation(final String name, final String description, final Set<Component> components,
			final LinkedList<String> parameters) throws Exception {

		final Transformation transformation = new Transformation();

		transformation.setName(name);
		transformation.setDescription(description);
		transformation.setComponents(components);
		transformation.setParameters(parameters);

		return createAndCompareObject(transformation, transformation);
	}

	/**
	 * {@inheritDoc}<br/>
	 * Updates the name, description, parameters, machine processable function description and components of the transformation.
	 */
	@Override
	protected Transformation prepareObjectForUpdate(final Transformation objectWithUpdates, final Transformation object) {

		super.prepareObjectForUpdate(objectWithUpdates, object);

		object.setComponents(objectWithUpdates.getComponents());

		return object;
	}

	@Override
	public void reset() {

		componentServiceTestUtils.reset();
	}

	public Transformation getSimpleTrimTransformation() throws Exception {

		final Component component = componentServiceTestUtils.getSimpleTrimComponent();

		// transformation

		final String transformationName = "my transformation";
		final String transformationDescription = "transformation which just makes use of one function";
		final String transformationParameter = "transformationInputString";

		final Set<Component> components = Sets.newLinkedHashSet();

		components.add(component);

		final LinkedList<String> parameters = Lists.newLinkedList();
		parameters.add(transformationParameter);

		return createTransformation(transformationName, transformationDescription, components, parameters);
	}

	public Transformation getComplexTransformation() throws Exception {

		final String transformation2Name = "my transformation 2";
		final String transformation2Description = "transformation which makes use of three functions (two transformations and one function)";
		final String transformation2Parameter = "firstName";
		final String transformation2Parameter2 = "familyName";

		final Set<Component> components2 = Sets.newLinkedHashSet();

		final Component component4 = componentServiceTestUtils.getFullNameComponent();

		final Iterator<Component> iter = component4.getInputComponents().iterator();

		components2.add(iter.next());
		components2.add(iter.next());
		components2.add(component4);

		final LinkedList<String> transformation2Parameters = Lists.newLinkedList();
		transformation2Parameters.add(transformation2Parameter);
		transformation2Parameters.add(transformation2Parameter2);

		return createTransformation(transformation2Name, transformation2Description,
				components2, transformation2Parameters);
	}
}
