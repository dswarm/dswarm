package de.avgl.dmp.controller.resources.test.utils;

import java.util.Map;
import java.util.Set;

import org.junit.Assert;

import com.google.common.collect.Maps;

import de.avgl.dmp.persistence.model.schema.AttributePath;
import de.avgl.dmp.persistence.model.schema.Schema;
import de.avgl.dmp.persistence.service.schema.SchemaService;

public class SchemasResourceTestUtils extends BasicResourceTestUtils<SchemaService, Schema, Long> {

	private final AttributePathsResourceTestUtils	attributePathsResourceTestUtils;

	private final ClaszesResourceTestUtils			claszesResourceTestUtils;

	public SchemasResourceTestUtils() {

		super("schemas", Schema.class, SchemaService.class);

		attributePathsResourceTestUtils = new AttributePathsResourceTestUtils();
		claszesResourceTestUtils = new ClaszesResourceTestUtils();
	}

	@Override
	public void compareObjects(final Schema expectedObject, final Schema actualObject) {

		super.compareObjects(expectedObject, actualObject);

		compareSchemas(expectedObject, actualObject);
	}

	private void compareSchemas(final Schema expectedSchema, final Schema actualSchema) {

		if (expectedSchema.getName() != null) {

			Assert.assertNotNull("the schema name shouldn't be null", actualSchema.getName());
			Assert.assertEquals("the schema names should be equal", expectedSchema.getName(), actualSchema.getName());
		}

		if (expectedSchema.getAttributePaths() != null && !expectedSchema.getAttributePaths().isEmpty()) {

			final Set<AttributePath> actualAttributePaths = actualSchema.getAttributePaths();

			Assert.assertNotNull("attribute paths of actual schema '" + actualSchema.getId() + "' shouldn't be null", actualAttributePaths);
			Assert.assertFalse("attribute paths of actual schema '" + actualSchema.getId() + "' shouldn't be empty", actualAttributePaths.isEmpty());

			final Map<Long, AttributePath> actualAttributePathsMap = Maps.newHashMap();

			for (final AttributePath actualAttributePath : actualAttributePaths) {

				actualAttributePathsMap.put(actualAttributePath.getId(), actualAttributePath);
			}

			attributePathsResourceTestUtils.compareObjects(expectedSchema.getAttributePaths(), actualAttributePathsMap);
		}

		if (expectedSchema.getRecordClass() != null) {

			claszesResourceTestUtils.compareObjects(expectedSchema.getRecordClass(), actualSchema.getRecordClass());
		}
	}
}
