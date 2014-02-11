package de.avgl.dmp.controller.resources.job.test.utils;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Assert;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import de.avgl.dmp.controller.resources.test.utils.ExtendedBasicDMPResourceTestUtils;
import de.avgl.dmp.persistence.model.job.Component;
import de.avgl.dmp.persistence.model.job.FunctionType;
import de.avgl.dmp.persistence.model.job.Transformation;
import de.avgl.dmp.persistence.model.job.proxy.ProxyComponent;
import de.avgl.dmp.persistence.service.job.ComponentService;

public class ComponentsResourceTestUtils extends ExtendedBasicDMPResourceTestUtils<ComponentService, ProxyComponent, Component> {

	private final FunctionsResourceTestUtils		functionsResourceTestUtils;

	private final TransformationsResourceTestUtils	transformationsResourceTestUtils;

	private final Set<Long>							checkedExpectedComponents	= Sets.newHashSet();

	private final Set<Long>							checkedActualComponents		= Sets.newHashSet();

	public ComponentsResourceTestUtils() {

		super("components", Component.class, ComponentService.class);

		functionsResourceTestUtils = new FunctionsResourceTestUtils();
		transformationsResourceTestUtils = new TransformationsResourceTestUtils(this);
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

	@Override
	public void reset() {

		checkedActualComponents.clear();
		checkedExpectedComponents.clear();

		functionsResourceTestUtils.reset();
	}
}
