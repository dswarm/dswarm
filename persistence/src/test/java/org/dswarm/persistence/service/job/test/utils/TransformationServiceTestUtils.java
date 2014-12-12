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

import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import org.junit.Assert;

import org.dswarm.persistence.model.job.Component;
import org.dswarm.persistence.model.job.Transformation;
import org.dswarm.persistence.model.job.proxy.ProxyTransformation;
import org.dswarm.persistence.service.job.TransformationService;

public class TransformationServiceTestUtils extends BasicFunctionServiceTestUtils<TransformationService, ProxyTransformation, Transformation> {

	private final ComponentServiceTestUtils	componentsResourceTestUtils;

	public TransformationServiceTestUtils() {

		super(Transformation.class, TransformationService.class);

		componentsResourceTestUtils = new ComponentServiceTestUtils();
	}

	public TransformationServiceTestUtils(final ComponentServiceTestUtils componentsResourceTestUtilsArg) {

		super(Transformation.class, TransformationService.class);

		componentsResourceTestUtils = componentsResourceTestUtilsArg;
	}

	/**
	 * {@inheritDoc} <br />
	 * Assert both transformations have either no components or their components are equal, see
	 * {@link org.dswarm.persistence.service.test.utils.BasicJPAServiceTestUtils#compareObjects(Set, Map)} for details.
	 */
	@Override
	public void compareObjects(final Transformation expectedTransformation, final Transformation actualTransformation) {

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

			componentsResourceTestUtils.compareObjects(expectedTransformation.getComponents(), actualComponentsMap);
		}
	}

	public Transformation createTransformation(final String name, final String description, final Set<Component> components,
			final LinkedList<String> parameters) throws Exception {

		final Transformation transformation = new Transformation();

		transformation.setName(name);
		transformation.setDescription(description);
		transformation.setComponents(components);
		transformation.setParameters(parameters);

		final Transformation updatedTransformation = createObject(transformation, transformation);

		return updatedTransformation;
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

		componentsResourceTestUtils.reset();
	}
}
