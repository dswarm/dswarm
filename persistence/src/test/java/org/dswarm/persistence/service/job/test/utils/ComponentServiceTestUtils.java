/**
 * Copyright (C) 2013 – 2016 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.json.JSONException;
import org.junit.Assert;

import org.dswarm.persistence.model.job.Component;
import org.dswarm.persistence.model.job.Function;
import org.dswarm.persistence.model.job.Transformation;
import org.dswarm.persistence.model.job.proxy.ProxyComponent;
import org.dswarm.persistence.service.job.ComponentService;
import org.dswarm.persistence.service.test.utils.BasicJPAServiceTestUtils;
import org.dswarm.persistence.service.test.utils.ExtendedBasicDMPJPAServiceTestUtils;

public class ComponentServiceTestUtils extends ExtendedBasicDMPJPAServiceTestUtils<ComponentService, ProxyComponent, Component> {

	private final FunctionServiceTestUtils functionServiceTestUtils;

	private final TransformationServiceTestUtils transformationsServiceTestUtils;

	private final Set<String> checkedExpectedComponents = Sets.newHashSet();

	private final Set<String> checkedActualComponents = Sets.newHashSet();

	public ComponentServiceTestUtils() {

		super(Component.class, ComponentService.class);

		functionServiceTestUtils = new FunctionServiceTestUtils();
		transformationsServiceTestUtils = new TransformationServiceTestUtils(this);
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
		Assert.assertNotNull("the component name shouldn't be null", updatedComponent.getUuid());

		return updatedComponent;
	}

	@Override
	public Component createObject(final JsonNode objectDescription) throws Exception {
		return null;
	}

	@Override
	public Component createObject(final String identifier) throws Exception {
		return null;
	}

	@Override
	public Component createAndPersistDefaultObject() throws Exception {

		return getSimpleTrimComponent();
	}

	@Override public Component createDefaultObject() throws Exception {
		return null;
	}

	@Override public Component createAndPersistDefaultCompleteObject() throws Exception {

		// previous component
		final Component component1 = getSimpleReplaceComponent();

		// next component
		final Component component2 = getSimpleLowerCaseComponent();

		// main component

		final Component component = getSimpleTrimComponent();

		final Set<Component> inputComponents = Sets.newLinkedHashSet();

		inputComponents.add(component1);

		final Set<Component> outputComponents = Sets.newLinkedHashSet();

		outputComponents.add(component2);

		component.setInputComponents(inputComponents);
		component.setOutputComponents(outputComponents);
		
		updateAndCompareObject(component2, component2);

		return updateAndCompareObject(component, component);
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
	public void compareObjects(final Component expectedComponent, final Component actualComponent) throws JsonProcessingException, JSONException {

		// Start skip already checked objects
		if (expectedComponent != null && expectedComponent.getUuid() != null) {

			if (checkedExpectedComponents.contains(expectedComponent.getUuid())) {

				return;
			}

			checkedExpectedComponents.add(expectedComponent.getUuid());
		}

		if (actualComponent != null && actualComponent.getUuid() != null) {

			if (checkedActualComponents.contains(actualComponent.getUuid())) {

				// SR FIXME why do we return here? see above
				return;
			}

			checkedActualComponents.add(actualComponent.getUuid());
		}
		// End skip already checked objects

		// basic comparison
		super.compareObjects(expectedComponent, actualComponent);

		// Start compare parts of components
		// function
		if (expectedComponent.getFunction() == null) {

			Assert.assertNull("the function of actual component '" + actualComponent.getUuid() + "' should be null", actualComponent.getFunction());

		} else {

			Assert.assertNotNull("the function of actual component '" + actualComponent.getUuid() + "' shouldn't be null",
					actualComponent.getFunction());

			Assert.assertNotNull("function type of component must not be null", expectedComponent.getFunction().getFunctionType());

			Assert.assertEquals("the function types are not equal", expectedComponent.getFunction().getFunctionType(), actualComponent.getFunction()
					.getFunctionType());

			switch (expectedComponent.getFunction().getFunctionType()) {

				case Function:

					functionServiceTestUtils.compareObjects(expectedComponent.getFunction(), actualComponent.getFunction());

					break;
				case Transformation:

					transformationsServiceTestUtils.compareObjects((Transformation) expectedComponent.getFunction(),
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

			prepareAndCompareComponents(actualComponent.getUuid(), expectedComponent.getInputComponents(), actualComponent.getInputComponents(),
					"input");
		}

		// output components
		if (expectedComponent.getOutputComponents() == null || expectedComponent.getOutputComponents().isEmpty()) {

			final boolean actualComponentHasNoOutputComponents = (actualComponent.getOutputComponents() == null || actualComponent
					.getOutputComponents().isEmpty());
			Assert.assertTrue("actual component should not have any output components", actualComponentHasNoOutputComponents);

		} else { // (!null && !empty)

			prepareAndCompareComponents(actualComponent.getUuid(), expectedComponent.getOutputComponents(), actualComponent.getOutputComponents(),
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

			Assert.assertNotNull("parameter mappings of actual component '" + actualComponent.getUuid() + "' shouldn't be null",
					actualParameterMappings);
			Assert.assertFalse("parameter mappings of actual component '" + actualComponent.getUuid() + "' shouldn't be empty",
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

		final Component deletedComponent = jpaService.getObject(component.getUuid());

		Assert.assertNull("component should be null", deletedComponent);

	}

	/**
	 * @param actualComponentId
	 * @param expectedComponents
	 * @param actualComponents
	 * @param type
	 * @see {@link BasicJPAServiceTestUtils#compareObjects(Set, Map)}
	 */
	private void prepareAndCompareComponents(final String actualComponentId, final Set<Component> expectedComponents,
			final Set<Component> actualComponents, final String type) throws JsonProcessingException, JSONException {

		Assert.assertNotNull(type + " components of actual component '" + actualComponentId + "' shouldn't be null", actualComponents);
		Assert.assertFalse(type + " components of actual component '" + actualComponentId + "' shouldn't be empty", actualComponents.isEmpty());

		final Map<String, Component> actualComponentsMap = Maps.newHashMap();

		for (final Component actualComponent : actualComponents) {

			actualComponentsMap.put(actualComponent.getUuid(), actualComponent);
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

			newInputComponents = null;
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

			newOutputComponents = null;
		}

		object.setOutputComponents(newOutputComponents);

		return object;
	}

	@Override
	public void reset() {

		checkedActualComponents.clear();
		checkedExpectedComponents.clear();

		functionServiceTestUtils.reset();
	}

	public Component getSimpleReplaceComponent() throws Exception {

		final Function function1 = functionServiceTestUtils.getSimpleReplaceFunction();

		final String component1Name = "my replace component";
		final Map<String, String> parameterMapping1 = Maps.newLinkedHashMap();

		final String functionParameterName1 = "inputString";
		final String componentVariableName1 = "previousComponent.outputString";
		final String functionParameterName2 = "regex";
		final String componentVariableName2 = "\\.";
		final String functionParameterName3 = "replaceString";
		final String componentVariableName3 = ":";

		parameterMapping1.put(functionParameterName1, componentVariableName1);
		parameterMapping1.put(functionParameterName2, componentVariableName2);
		parameterMapping1.put(functionParameterName3, componentVariableName3);

		return createComponent(component1Name, parameterMapping1, function1, null, null);
	}

	public Component getSimpleLowerCaseComponent() throws Exception {

		final Function function2 = functionServiceTestUtils.getSimpleLowerCaseFunction();

		final String component2Name = "my lower case component";
		final Map<String, String> parameterMapping2 = Maps.newLinkedHashMap();

		final String functionParameterName4 = "inputString";
		final String componentVariableName4 = "previousComponent.outputString";

		parameterMapping2.put(functionParameterName4, componentVariableName4);

		return createComponent(component2Name, parameterMapping2, function2, null, null);
	}

	public Component getSimpleTrimComponent() throws Exception {

		final Function function = functionServiceTestUtils.getSimpleTrimFunction();

		final String componentName = "my trim component";
		final Map<String, String> parameterMapping = Maps.newLinkedHashMap();

		final String functionParameterName = "inputString";
		final String componentVariableName = "previousComponent.outputString";

		parameterMapping.put(functionParameterName, componentVariableName);

		return createComponent(componentName, parameterMapping, function, null, null);
	}

	public Component getTransformationComponentSimpleTrimComponent(final String inputAttributePath, final String outputAttributePath)
			throws Exception {

		final Transformation transformation = transformationsServiceTestUtils.getSimpleTrimTransformation();

		final Map<String, String> transformationComponentParameterMappings = Maps.newLinkedHashMap();

		transformationComponentParameterMappings.put(transformation.getParameters().get(0), inputAttributePath);
		transformationComponentParameterMappings.put("transformationOutputVariable", outputAttributePath);

		return createComponent(transformation.getName() + " (component)",
				transformationComponentParameterMappings, transformation, null, null);
	}

	/**
	 * note: result will be cache (temporarily - for re-utilisation in a test)
	 *
	 * @return
	 * @throws Exception
	 */
	public Component getFirstNameTransformationComponentDefaultCompleteComponent() throws Exception {

		final String transformationComponentName = "prepare first name";

		if (!cache.containsKey(transformationComponentName)) {

			final Transformation transformation = transformationsServiceTestUtils.createAndPersistDefaultCompleteObject();

			final String transformationComponentFunctionParameterName = "transformationInputString";
			final String transformationComponentVariableName = "firstName";

			final Map<String, String> transformationComponentParameterMappings = Maps.newLinkedHashMap();

			transformationComponentParameterMappings.put(transformationComponentFunctionParameterName, transformationComponentVariableName);

			final Component transformationComponent = createComponent(transformationComponentName,
					transformationComponentParameterMappings, transformation, null, null);

			cache.put(transformationComponentName, transformationComponent);
		}

		return cache.get(transformationComponentName);
	}

	/**
	 * note: result will be cache (temporarily - for re-utilisation in a test)
	 *
	 * @return
	 * @throws Exception
	 */
	public Component getFamilyNameTransformationComponentDefaultCompleteComponent() throws Exception {

		final String name = "prepare family name";

		if (!cache.containsKey(name)) {

			final Transformation transformation = transformationsServiceTestUtils.createAndPersistDefaultCompleteObject();

			final Map<String, String> transformationComponentParameterMappings2 = Maps.newLinkedHashMap();

			transformationComponentParameterMappings2.put("transformationInputString", "familyName");

			final Component transformationComponent = createComponent(name,
					transformationComponentParameterMappings2, transformation, null, null);

			cache.put(name, transformationComponent);
		}

		return cache.get(name);
	}

	/**
	 * note: result will be cache (temporarily - for re-utilisation in a test)
	 *
	 * @return
	 * @throws Exception
	 */
	public Component getFullNameComponent() throws Exception {

		final String component4Name = "full name";

		if (!cache.containsKey(component4Name)) {

			final Component transformationComponent = getFirstNameTransformationComponentDefaultCompleteComponent();
			final Component transformationComponent2 = getFamilyNameTransformationComponentDefaultCompleteComponent();
			final Function function4 = functionServiceTestUtils.getSimpleConcatFunction();

			final Map<String, String> parameterMapping4 = Maps.newLinkedHashMap();

			final String functionParameterName5 = "firstString";
			final String componentVariableName5 = transformationComponent.getUuid() + ".outputVariable";
			final String functionParameterName6 = "secondString";
			final String componentVariableName6 = transformationComponent2.getUuid() + ".outputVariable";

			parameterMapping4.put(functionParameterName5, componentVariableName5);
			parameterMapping4.put(functionParameterName6, componentVariableName6);

			final Set<Component> component4InputComponents = Sets.newLinkedHashSet();

			component4InputComponents.add(transformationComponent);
			component4InputComponents.add(transformationComponent2);

			final Component component4 = createComponent(component4Name, parameterMapping4, function4,
					component4InputComponents, null);

			cache.put(component4Name, component4);
		}

		return cache.get(component4Name);
	}

	public Component getComplexTransformationComponent(final String firstInputAttributePath, final String secondInputAttributePath,
			final String outputAttributePath) throws Exception {

		final Transformation transformation2 = transformationsServiceTestUtils.getComplexTransformation();

		final Map<String, String> transformationComponent3ParameterMappings = Maps.newLinkedHashMap();

		transformationComponent3ParameterMappings.put(transformation2.getParameters().getFirst(), firstInputAttributePath);
		transformationComponent3ParameterMappings.put(transformation2.getParameters().get(1), secondInputAttributePath);
		transformationComponent3ParameterMappings.put("transformationOutputVariable", outputAttributePath);

		return createComponent(transformation2.getName() + " (component)",
				transformationComponent3ParameterMappings, transformation2, null, null);
	}
}
