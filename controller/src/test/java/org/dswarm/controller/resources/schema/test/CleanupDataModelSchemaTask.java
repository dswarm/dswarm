package org.dswarm.controller.resources.schema.test;

import java.util.Map;

import com.google.common.collect.Maps;

import org.dswarm.controller.resources.resource.test.utils.ConfigurationsResourceTestUtils;
import org.dswarm.controller.resources.resource.test.utils.DataModelsResourceTestUtils;
import org.dswarm.controller.resources.resource.test.utils.ResourcesResourceTestUtils;
import org.dswarm.controller.resources.schema.test.utils.AttributePathsResourceTestUtils;
import org.dswarm.controller.resources.schema.test.utils.AttributesResourceTestUtils;
import org.dswarm.controller.resources.schema.test.utils.ClaszesResourceTestUtils;
import org.dswarm.controller.resources.schema.test.utils.SchemasResourceTestUtils;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.resource.Resource;
import org.dswarm.persistence.model.schema.Attribute;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.Clasz;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.model.types.Tuple;
import org.dswarm.persistence.service.internal.test.utils.InternalGDMGraphServiceTestUtils;


final class CleanupDataModelSchemaTask {

	private final DataModelsResourceTestUtils dataModelsResourceTestUtils;
	private final AttributesResourceTestUtils attributesResourceTestUtils;
	private final ClaszesResourceTestUtils claszesResourceTestUtils;
	private final AttributePathsResourceTestUtils attributePathsResourceTestUtils;
	private final ResourcesResourceTestUtils resourcesResourceTestUtils;
	private final ConfigurationsResourceTestUtils configurationsResourceTestUtils;
	private final SchemasResourceTestUtils schemasResourceTestUtils;

	CleanupDataModelSchemaTask() {
		dataModelsResourceTestUtils = new DataModelsResourceTestUtils();
		attributesResourceTestUtils = new AttributesResourceTestUtils();
		claszesResourceTestUtils = new ClaszesResourceTestUtils();
		attributePathsResourceTestUtils = new AttributePathsResourceTestUtils();
		resourcesResourceTestUtils = new ResourcesResourceTestUtils();
		configurationsResourceTestUtils = new ConfigurationsResourceTestUtils();
		schemasResourceTestUtils = new SchemasResourceTestUtils();
	}

	void cleanUpResources(final DataModel dataModel, final Schema schema) {
		final Tuple<Map<Long, AttributePath>, Map<Long, Attribute>> schemaAttributePaths = determineSchemaAttributePaths(schema.getUniqueAttributePaths());

		cleanUpDataModel(dataModel);
		cleanUpSchema(schema);
		cleanUpAttributePaths(schemaAttributePaths.v1());
		cleanUpAttributes(schemaAttributePaths.v2());
		InternalGDMGraphServiceTestUtils.cleanGraphDB();
	}

	private static Tuple<Map<Long, AttributePath>, Map<Long, Attribute>> determineSchemaAttributePaths(final Iterable<AttributePath> schemaAttributePaths) {
		final Map<Long, AttributePath> attributePaths = Maps.newLinkedHashMap();
		final Map<Long, Attribute> attributes = Maps.newLinkedHashMap();

		if (schemaAttributePaths != null) {
			for (final AttributePath schemaAttributePath : schemaAttributePaths) {
				attributePaths.put(schemaAttributePath.getId(), schemaAttributePath);
				final Iterable<Attribute> attributePathsAttributes = schemaAttributePath.getAttributes();
				if (attributePathsAttributes != null) {
					for (final Attribute attribute : attributePathsAttributes) {
						attributes.put(attribute.getId(), attribute);
					}
				}
			}
		}

		return Tuple.tuple(attributePaths, attributes);
	}

	private void cleanUpDataModel(final DataModel dataModel) {
		final Resource dataModelResource = dataModel.getDataResource();
		final Configuration configuration = dataModel.getConfiguration();
		dataModelsResourceTestUtils.deleteObject(dataModel);
		cleanUpDataResource(dataModelResource);
		cleanUpConfiguration(configuration);
	}

	private void cleanUpSchema(final Schema schema) {
		final Clasz recordClass = schema.getRecordClass();
		schemasResourceTestUtils.deleteObject(schema);
		cleanUpRecordClass(recordClass);
	}

	private void cleanUpRecordClass(final Clasz recordClass) {
		if (recordClass != null) {
			claszesResourceTestUtils.deleteObjectViaPersistenceServiceTestUtils(recordClass);
		}
	}

	private void cleanUpDataResource(final Resource dataModelResource) {
		if (dataModelResource != null) {
			resourcesResourceTestUtils.deleteObject(dataModelResource);
		}
	}

	private void cleanUpConfiguration(final Configuration configuration) {
		if (configuration != null) {
			configurationsResourceTestUtils.deleteObject(configuration);
		}
	}

	private void cleanUpAttributes(final Map<Long, Attribute> attributes) {
		for (final Attribute attribute : attributes.values()) {
			attributesResourceTestUtils.deleteObjectViaPersistenceServiceTestUtils(attribute);
		}
	}

	private void cleanUpAttributePaths(final Map<Long, AttributePath> attributePaths) {
		for (final AttributePath attributePath : attributePaths.values()) {
			attributePathsResourceTestUtils.deleteObjectViaPersistenceServiceTestUtils(attributePath);
		}
	}
}
