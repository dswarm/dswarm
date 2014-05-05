package de.avgl.dmp.persistence.service.schema.test.utils;

import java.util.Map;
import java.util.Set;

import org.junit.Assert;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.schema.Attribute;
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

	public Schema createSchema(final String name, final Set<AttributePath> attributePaths, final Clasz recordClass) throws Exception {

		final Schema schema = new Schema();

		schema.setName(name);
		schema.setAttributePaths(attributePaths);
		schema.setRecordClass(recordClass);

		// update schema

		final Schema updatedSchema = createObject(schema, schema);

		Assert.assertNotNull("updated schema shouldn't be null", updatedSchema);
		Assert.assertNotNull("updated schema id shouldn't be null", updatedSchema.getId());

		return updatedSchema;
	}

	public void removeAddedAttributePathsFromOutputModelSchema(final Schema outputDataModelSchema, final Map<Long, Attribute> attributes,
			final Map<Long, AttributePath> attributePaths) throws DMPPersistenceException {

		final Set<AttributePath> outputDataModelSchemaAttributePathRemovalCandidates = Sets.newHashSet();

		// collect attribute paths of attributes that were created via processing the transformation result
		if (outputDataModelSchema != null) {

			final Set<AttributePath> outputDataModelSchemaAttributePaths = outputDataModelSchema.getAttributePaths();

			if (outputDataModelSchemaAttributePaths != null) {

				for (final AttributePath outputDataModelSchemaAttributePath : outputDataModelSchemaAttributePaths) {

					final Set<Attribute> outputDataModelSchemaAttributePathAttributes = outputDataModelSchemaAttributePath.getAttributes();

					for (final Attribute outputDataModelSchemaAttribute : outputDataModelSchemaAttributePathAttributes) {

						if (attributes.containsKey(outputDataModelSchemaAttribute.getId())) {

							// found candidate for removal

							attributePaths.put(outputDataModelSchemaAttributePath.getId(), outputDataModelSchemaAttributePath);

							// remove candidate from output data model schema
							outputDataModelSchemaAttributePathRemovalCandidates.add(outputDataModelSchemaAttributePath);
						}
					}
				}
			}
		}

		for (final AttributePath outputDataModelSchemaAttributePath : outputDataModelSchemaAttributePathRemovalCandidates) {

			outputDataModelSchema.removeAttributePath(outputDataModelSchemaAttributePath);
		}

		// update output data model schema to persist possible changes
		jpaService.updateObjectTransactional(outputDataModelSchema);
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
