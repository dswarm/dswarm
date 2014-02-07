package de.avgl.dmp.persistence.service.schema.test.utils;

import java.util.Map;
import java.util.Set;

import org.junit.Assert;

import com.google.common.collect.Maps;

import de.avgl.dmp.persistence.model.schema.AttributePath;
import de.avgl.dmp.persistence.model.schema.Clasz;
import de.avgl.dmp.persistence.model.schema.Schema;
import de.avgl.dmp.persistence.model.schema.proxy.ProxySchema;
import de.avgl.dmp.persistence.service.schema.SchemaService;
import de.avgl.dmp.persistence.service.test.utils.BasicDMPJPAServiceTestUtils;

public class SchemaServiceTestUtils extends BasicDMPJPAServiceTestUtils<SchemaService, ProxySchema, Schema> {

	private final AttributePathServiceTestUtils	attributePathsResourceTestUtils;

	private final ClaszServiceTestUtils			claszesResourceTestUtils;

	public SchemaServiceTestUtils() {

		super(Schema.class, SchemaService.class);

		attributePathsResourceTestUtils = new AttributePathServiceTestUtils();
		claszesResourceTestUtils = new ClaszServiceTestUtils();
	}

	@Override
	public void compareObjects(final Schema expectedObject, final Schema actualObject) {

		super.compareObjects(expectedObject, actualObject);

		compareSchemas(expectedObject, actualObject);
	}

	private void compareSchemas(final Schema expectedSchema, final Schema actualSchema) {

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
	
	/**
	 * {@inheritDoc}<br/>
	 * Updates the name, attribute paths and record class of the schema.
	 */
	@Override
	protected Schema prepareObjectForUpdate(final Schema objectWithUpdates, final Schema object) {

		super.prepareObjectForUpdate(objectWithUpdates, object);

		final Set<AttributePath> attributePaths = objectWithUpdates.getAttributePaths();

		object.setAttributePaths(attributePaths);

		final Clasz recordClass = objectWithUpdates.getRecordClass();

		object.setRecordClass(recordClass);

		return object;
	}

	@Override
	public void reset() {

		attributePathsResourceTestUtils.reset();
		claszesResourceTestUtils.reset();
	}
}
