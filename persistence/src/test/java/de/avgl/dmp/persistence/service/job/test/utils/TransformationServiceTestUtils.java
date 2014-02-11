package de.avgl.dmp.persistence.service.job.test.utils;

import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;

import com.google.common.collect.Maps;

import de.avgl.dmp.persistence.model.job.Component;
import de.avgl.dmp.persistence.model.job.Transformation;
import de.avgl.dmp.persistence.model.job.proxy.ProxyTransformation;
import de.avgl.dmp.persistence.service.job.TransformationService;

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
