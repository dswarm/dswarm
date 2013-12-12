package de.avgl.dmp.controller.resources.test.utils;

import java.util.Map;
import java.util.Set;

import org.junit.Assert;

import com.google.common.collect.Maps;

import de.avgl.dmp.persistence.model.job.Component;
import de.avgl.dmp.persistence.model.job.Transformation;
import de.avgl.dmp.persistence.service.job.TransformationService;

public class TransformationsResourceTestUtils extends BasicFunctionsResourceTestUtils<TransformationService, Transformation> {

	private final ComponentsResourceTestUtils	componentsResourceTestUtils;

	public TransformationsResourceTestUtils() {

		super("transformations", Transformation.class, TransformationService.class);

		componentsResourceTestUtils = new ComponentsResourceTestUtils();
	}

	public TransformationsResourceTestUtils(final ComponentsResourceTestUtils componentsResourceTestUtilsArg) {

		super("transformations", Transformation.class, TransformationService.class);

		componentsResourceTestUtils = componentsResourceTestUtilsArg;
	}

	@Override
	public void compareObjects(final Transformation expectedObject, final Transformation actualObject) {

		super.compareObjects(expectedObject, actualObject);

		if (expectedObject.getComponents() != null && !expectedObject.getComponents().isEmpty()) {

			final Set<Component> actualComponents = actualObject.getComponents();

			Assert.assertNotNull("components of transformation '" + actualObject.getId() + "' shouldn't be null", actualComponents);
			Assert.assertFalse("components of transformation '" + actualObject.getId() + "' shouldn't be empty", actualComponents.isEmpty());

			final Map<Long, Component> actualComponentsMap = Maps.newHashMap();

			for (final Component actualComponent : actualComponents) {

				actualComponentsMap.put(actualComponent.getId(), actualComponent);
			}

			componentsResourceTestUtils.compareObjects(expectedObject.getComponents(), actualComponentsMap);
		}
	}

	@Override
	public void reset() {
		
		componentsResourceTestUtils.reset();
	}
}
