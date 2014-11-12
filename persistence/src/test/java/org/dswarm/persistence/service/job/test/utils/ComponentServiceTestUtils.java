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
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.junit.Assert;

import org.dswarm.persistence.model.job.Component;
import org.dswarm.persistence.model.job.Function;
import org.dswarm.persistence.model.job.Transformation;
import org.dswarm.persistence.model.job.proxy.ProxyComponent;
import org.dswarm.persistence.service.job.ComponentService;
import org.dswarm.persistence.service.test.utils.BasicJPAServiceTestUtils;
import org.dswarm.persistence.service.test.utils.ExtendedBasicDMPJPAServiceTestUtils;

public class ComponentServiceTestUtils extends ExtendedBasicDMPJPAServiceTestUtils<ComponentService, ProxyComponent, Component> {

	private final FunctionServiceTestUtils			functionsResourceTestUtils;

	private final TransformationServiceTestUtils	transformationsResourceTestUtils;

	private final Set<Long>							checkedExpectedComponents	= Sets.newHashSet();

	private final Set<Long>							checkedActualComponents		= Sets.newHashSet();

	public ComponentServiceTestUtils() {

		super(Component.class, ComponentService.class);

		functionsResourceTestUtils = new FunctionServiceTestUtils();
		transformationsResourceTestUtils = new TransformationServiceTestUtils(this);
	}

	public Component createComponent(final String name, final Map<String, String> parameterMappings, final Function function,
			final Set<Component> inputComponents, final Set<Component> outputComponents) throws Exception {

		// component needs to be a persistent object from the beginning
		final Component component = createObject().getObject();

		component.setName(name);
		component.setFunction(function);
		component.setParameterMappings(parameterMappings);

		if (inputComponents != null) {

			component.setInputComponents(inputComponents);
		}

		if (outputComponents != null) {

			component.setOutputComponents(outputComponents);
		}

		// update method needs to be utilised here, because component was already created
		final Component updatedComponent = updateAndCompareObject(component, component);

		Assert.assertNotNull("the updated component shouldn't be null", updatedComponent);
		Assert.assertNotNull("the component name shouldn't be null", updatedComponent.getId());

		return updatedComponent;
	}

	/**
	 * {@inheritDoc} <br/>
	 * Assert either both components have no {@link Function} or {@link Function}s are equal: see
	 * {@link BasicFunctionServiceTestUtils#compareObjects(Function, Function)} and
	 * {@link TransformationServiceTestUtils#compareObjects(Transformation, Transformation)} for details. <br />
	 * Assert either both components have no input components or input components are equal regarding
	 * {@link BasicJPAServiceTestUtils#compareObjects(Set, Map)} <br />
	 * Assert either both components have no output components or output components are equal regarding
	 * {@link BasicJPAServiceTestUtils#compareObjects(Set, Map)} <br />
	 * Assert either both components have no or the same number of parameter mappings and parameter mapping keys and values are
	 * pairwise equal.
	 */
	@Override
	public void compareObjects(final Component expectedComponent, final Component actualComponent) {

		// Start skip already checked objects
		if (expectedComponent != null && expectedComponent.getId() != null) {

			if (checkedExpectedComponents.contains(expectedComponent.getId())) {

				// SR FIXME why do we return here? we may have seen the expectedObject before but get a different actualObject
				// that needs to be compared to the one already known.
				// Furthermore, even if we have already seen expected A and actual B, how do we know that we already compared A
				// with B? previous calls may have been
				// A, C
				// D, B
				// current: A, B
				return;
			}

			checkedExpectedComponents.add(expectedComponent.getId());
		}

		if (actualComponent != null && actualComponent.getId() != null) {

			if (checkedActualComponents.contains(actualComponent.getId())) {

				// SR FIXME why do we return here? see above
				return;
			}

			checkedActualComponents.add(actualComponent.getId());
		}
		// End skip already checked objects

		// basic comparison
		super.compareObjects(expectedComponent, actualComponent);

		// Start compare parts of components
		// function
		if (expectedComponent.getFunction() == null) {

			Assert.assertNull("the function of actual component '" + actualComponent.getId() + "' should be null", actualComponent.getFunction());

		} else {

			Assert.assertNotNull("the function of actual component '" + actualComponent.getId() + "' shouldn't be null",
					actualComponent.getFunction());

			Assert.assertNotNull("function type of component must not be null", expectedComponent.getFunction().getFunctionType());

			Assert.assertEquals("the function types are not equal", expectedComponent.getFunction().getFunctionType(), actualComponent.getFunction()
					.getFunctionType());

			switch (expectedComponent.getFunction().getFunctionType()) {

				case Function:

					functionsResourceTestUtils.compareObjects(expectedComponent.getFunction(), actualComponent.getFunction());

					break;
				case Transformation:

					transformationsResourceTestUtils.compareObjects((Transformation) expectedComponent.getFunction(),
							(Transformation) actualComponent.getFunction());

					break;
				default:
					Assert.assertTrue("unknown function type.", false);
			}

		}

		// input components
		if (expectedComponent.getInputComponents() == null || expectedComponent.getInputComponents().isEmpty()) {

			final boolean actualComponentHasNoInputComponents = (actualComponent.getInputComponents() == null || actualComponent.getInputComponents()
					.isEmpty());
			Assert.assertTrue("actual component should not have any input components", actualComponentHasNoInputComponents);

		} else { // (!null && !empty)

			prepareAndCompareComponents(actualComponent.getId(), expectedComponent.getInputComponents(), actualComponent.getInputComponents(),
					"input");
		}

		// output components
		if (expectedComponent.getOutputComponents() == null || expectedComponent.getOutputComponents().isEmpty()) {

			final boolean actualComponentHasNoOutputComponents = (actualComponent.getOutputComponents() == null || actualComponent
					.getOutputComponents().isEmpty());
			Assert.assertTrue("actual component should not have any output components", actualComponentHasNoOutputComponents);

		} else { // (!null && !empty)

			prepareAndCompareComponents(actualComponent.getId(), expectedComponent.getOutputComponents(), actualComponent.getOutputComponents(),
					"output");
		}

		// parameter mappings
		if (expectedComponent.getParameterMappings() == null || expectedComponent.getParameterMappings().isEmpty()) {

			final boolean actualComponentHasNoParameterMappings = (actualComponent.getParameterMappings() == null || actualComponent
					.getParameterMappings().isEmpty());
			Assert.assertTrue("actual component should not have any parameter mappings", actualComponentHasNoParameterMappings);

		} else {
			// (!null && !empty)

			final Map<String, String> actualParameterMappings = actualComponent.getParameterMappings();

			Assert.assertNotNull("parameter mappings of actual component '" + actualComponent.getId() + "' shouldn't be null",
					actualParameterMappings);
			Assert.assertFalse("parameter mappings of actual component '" + actualComponent.getId() + "' shouldn't be empty",
					actualParameterMappings.isEmpty());
			Assert.assertEquals("different number of parameter mappings", expectedComponent.getParameterMappings().size(), actualComponent
					.getParameterMappings().size());

			for (final Entry<String, String> expectedParameterMappingEntry : expectedComponent.getParameterMappings().entrySet()) {

				final String expectedParameterKey = expectedParameterMappingEntry.getKey();

				Assert.assertTrue("the actual parameter mappings doesn't contain a mapping for '" + expectedParameterKey + "'",
						actualParameterMappings.containsKey(expectedParameterKey));

				final String expectedParameterValue = expectedParameterMappingEntry.getValue();
				final String actualParameterValue = actualParameterMappings.get(expectedParameterKey);

				Assert.assertEquals("the parameter mappings for '" + expectedParameterKey + "' are not equal; is '" + actualParameterValue
						+ "' but should be '" + expectedParameterValue + "'", expectedParameterValue, actualParameterValue);
			}
		}
		// End compare parts of components
	}

	public void checkDeletedComponent(final Component component) {

		Component deletedComponent = null;

		deletedComponent = jpaService.getObject(component.getId());

		Assert.assertNull("component should be null", deletedComponent);

	}

	/**
	 * @see {@link BasicJPAServiceTestUtils#compareObjects(Set, Map)}
	 * @param actualComponentId
	 * @param expectedComponents
	 * @param actualComponents
	 * @param type
	 */
	private void prepareAndCompareComponents(final Long actualComponentId, final Set<Component> expectedComponents,
			final Set<Component> actualComponents, final String type) {

		Assert.assertNotNull(type + " components of actual component '" + actualComponentId + "' shouldn't be null", actualComponents);
		Assert.assertFalse(type + " components of actual component '" + actualComponentId + "' shouldn't be empty", actualComponents.isEmpty());

		final Map<Long, Component> actualComponentsMap = Maps.newHashMap();

		for (final Component actualComponent : actualComponents) {

			actualComponentsMap.put(actualComponent.getId(), actualComponent);
		}

		compareObjects(expectedComponents, actualComponentsMap);
	}

	/**
	 * {@inheritDoc}<br/>
	 * Updates the name, description, function, parameter mappings, input components and output components of the component.
	 */
	@Override
	protected Component prepareObjectForUpdate(final Component objectWithUpdates, final Component object) {

		super.prepareObjectForUpdate(objectWithUpdates, object);

		object.setFunction(objectWithUpdates.getFunction());
		object.setParameterMappings(objectWithUpdates.getParameterMappings());

		final Set<Component> inputComponents = objectWithUpdates.getInputComponents();
		Set<Component> newInputComponents;

		if (inputComponents != null) {

			newInputComponents = Sets.newCopyOnWriteArraySet();

			for (final Component inputComponent : inputComponents) {

				inputComponent.removeOutputComponent(objectWithUpdates);
				inputComponent.addOutputComponent(object);

				newInputComponents.add(inputComponent);
			}
		} else {

			newInputComponents = inputComponents;
		}

		object.setInputComponents(newInputComponents);

		final Set<Component> outputComponents = objectWithUpdates.getOutputComponents();
		Set<Component> newOutputComponents;

		if (outputComponents != null) {

			newOutputComponents = Sets.newCopyOnWriteArraySet();

			for (final Component outputComponent : outputComponents) {

				outputComponent.removeInputComponent(objectWithUpdates);
				outputComponent.addInputComponent(object);

				newOutputComponents.add(outputComponent);
			}
		} else {

			newOutputComponents = outputComponents;
		}

		object.setOutputComponents(newOutputComponents);

		return object;
	}

	@Override
	public void reset() {

		checkedActualComponents.clear();
		checkedExpectedComponents.clear();

		functionsResourceTestUtils.reset();
	}
}
