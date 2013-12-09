package de.avgl.dmp.controller.resources.test.utils;

import java.util.Map;
import java.util.Set;

import org.junit.Assert;

import com.google.common.collect.Maps;

import de.avgl.dmp.persistence.model.schema.Attribute;
import de.avgl.dmp.persistence.model.schema.AttributePath;
import de.avgl.dmp.persistence.service.schema.AttributePathService;

public class AttributePathsResourceTestUtils extends BasicResourceTestUtils<AttributePathService, AttributePath, Long> {

	private final AttributesResourceTestUtils	attributeResourceTestUtils;

	public AttributePathsResourceTestUtils() {

		super("attributepaths", AttributePath.class, AttributePathService.class);

		attributeResourceTestUtils = new AttributesResourceTestUtils();
	}

	@Override
	public void compareObjects(final AttributePath expectedObject, final AttributePath actualObject) {

		super.compareObjects(expectedObject, actualObject);

		if (expectedObject.getAttributes() != null && !expectedObject.getAttributes().isEmpty()) {

			final Set<Attribute> actualAttributes = actualObject.getAttributes();

			Assert.assertNotNull("attributes of actual attribute path '" + actualObject.getId() + "' shouldn't be null", actualAttributes);
			Assert.assertFalse("attributes of actual attribute path '" + actualObject.getId() + "' shouldn't be empty", actualAttributes.isEmpty());

			final Map<String, Attribute> actualAttributesMap = Maps.newHashMap();

			for (final Attribute actualAttribute : actualAttributes) {

				actualAttributesMap.put(actualAttribute.getId(), actualAttribute);
			}

			attributeResourceTestUtils.compareObjects(expectedObject.getAttributes(), actualAttributesMap);
		}
	}
}
