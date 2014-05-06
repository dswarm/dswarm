package de.avgl.dmp.persistence.service.job.test.utils;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Assert;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import de.avgl.dmp.persistence.model.job.Component;
import de.avgl.dmp.persistence.model.job.Function;
import de.avgl.dmp.persistence.model.job.FunctionType;
import de.avgl.dmp.persistence.model.job.Transformation;
import de.avgl.dmp.persistence.model.job.proxy.ProxyComponent;
import de.avgl.dmp.persistence.service.job.ComponentService;
import de.avgl.dmp.persistence.service.test.utils.ExtendedBasicDMPJPAServiceTestUtils;

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
		final Component updatedComponent = updateObject(component, component);

		Assert.assertNotNull("the updated component shouldn't be null", updatedComponent);
		Assert.assertNotNull("the component name shouldn't be null", updatedComponent.getId());

		return updatedComponent;
	}

	@Override
	public void compareObjects(final Component expectedObject, final Component actualObject) {

		if (expectedObject != null && expectedObject.getId() != null) {

			if (checkedExpectedComponents.contains(expectedObject.getId())) {

				return;
			}

			checkedExpectedComponents.add(expectedObject.getId());
		}

		if (actualObject != null && actualObject.getId() != null) {

			if (checkedActualComponents.contains(actualObject.getId())) {

				return;
			}

			checkedActualComponents.add(actualObject.getId());
		}

		super.compareObjects(expectedObject, actualObject);

		compareComponents(expectedObject, actualObject);
	}

	private void compareComponents(final Component expectedComponent, final Component actualComponent) {

		if (expectedComponent.getFunction() != null) {

			switch (expectedComponent.getFunction().getFunctionType()) {

				case Function:

					functionsResourceTestUtils.compareObjects(expectedComponent.getFunction(), actualComponent.getFunction());

					break;
				case Transformation:

					Assert.assertNotNull("the function shouldn't be null", actualComponent.getFunction());

					Assert.assertEquals("the function types are not equal", FunctionType.Transformation, actualComponent.getFunction()
							.getFunctionType());

					transformationsResourceTestUtils.compareObjects((Transformation) expectedComponent.getFunction(),
							(Transformation) actualComponent.getFunction());

					break;
			}

		}

		if (expectedComponent.getInputComponents() != null && !expectedComponent.getInputComponents().isEmpty()) {

			prepareCompareComponents(actualComponent.getId(), expectedComponent.getInputComponents(), actualComponent.getInputComponents(), "input");
		}

		if (expectedComponent.getOutputComponents() != null && !expectedComponent.getOutputComponents().isEmpty()) {

			prepareCompareComponents(actualComponent.getId(), expectedComponent.getOutputComponents(), actualComponent.getOutputComponents(),
					"output");
		}

		if (expectedComponent.getParameterMappings() != null && !expectedComponent.getParameterMappings().isEmpty()) {

			final Map<String, String> actualParameterMappings = actualComponent.getParameterMappings();

			Assert.assertNotNull("parameter mappings of actual component '" + actualComponent.getId() + "' shouldn't be null",
					actualParameterMappings);
			Assert.assertFalse("parameter mappings of actual component '" + actualComponent.getId() + "' shouldn't be empty",
					actualParameterMappings.isEmpty());

			for (final Entry<String, String> expectedParameterMappingEntry : expectedComponent.getParameterMappings().entrySet()) {

				final String expectedParameterKey = expectedParameterMappingEntry.getKey();

				Assert.assertTrue("the actual parameter mappings doesn't contain a mapping for '" + expectedParameterKey + "'",
						actualParameterMappings.containsKey(expectedParameterKey));

				final String expectedParameterValue = expectedParameterMappingEntry.getValue();
				final String actualParameterValue = actualParameterMappings.get(expectedParameterKey);

				Assert.assertEquals("the parameter mapping for '" + expectedParameterKey + "' are not equal; is '" + actualParameterValue
						+ "' but should be '" + expectedParameterValue + "'", expectedParameterValue, actualParameterValue);
			}
		}
	}

	public void checkDeletedComponent(final Component component) {

		Component deletedComponent = null;

		deletedComponent = jpaService.getObject(component.getId());

		Assert.assertNull("component should be null", deletedComponent);

	}

	private void prepareCompareComponents(final Long actualComponentId, final Set<Component> expectedComponents,
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
