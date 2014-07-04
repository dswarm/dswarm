package org.dswarm.persistence.service.resource.test.utils;

import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.resource.Resource;
import org.dswarm.persistence.model.resource.proxy.ProxyDataModel;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.service.resource.DataModelService;
import org.dswarm.persistence.service.schema.test.utils.SchemaServiceTestUtils;
import org.dswarm.persistence.service.test.utils.ExtendedBasicDMPJPAServiceTestUtils;
import org.junit.Assert;

public class DataModelServiceTestUtils extends ExtendedBasicDMPJPAServiceTestUtils<DataModelService, ProxyDataModel, DataModel> {

	private final ResourceServiceTestUtils		resourcesResourceTestUtils;

	private final ConfigurationServiceTestUtils	configurationsResourceTestUtils;

	private final SchemaServiceTestUtils		schemasResourceTestUtils;

	public DataModelServiceTestUtils() {

		super(DataModel.class, DataModelService.class);

		resourcesResourceTestUtils = new ResourceServiceTestUtils();
		configurationsResourceTestUtils = new ConfigurationServiceTestUtils();
		schemasResourceTestUtils = new SchemaServiceTestUtils();
	}

	/**
	 * {@inheritDoc} <br />
	 * Assert that either both data models have no {@link Resource} or their resources are equal, see
	 * {@link ResourceServiceTestUtils#compareObjects(Resource, Resource)}. <br />
	 * Assert that either both data models have no {@link Configuration} or their configurations are equal, see
	 * {@link ConfigurationServiceTestUtils#compareObjects(Configuration, Configuration)}. <br />
	 * Assert that either both data models have no {@link Schema} or their schemata are equal, see
	 * {@link SchemaServiceTestUtils#compareObjects(Schema, Schema)}. <br />
	 */
	@Override
	public void compareObjects(final DataModel expectedDataModel, final DataModel actualDataModel) {

		super.compareObjects(expectedDataModel, actualDataModel);

		// TODO: re-enable reference deserializer or manually retrieve objects by from DB

		// check resource
		if (expectedDataModel.getDataResource() == null) {

			Assert.assertNull("the actual data model shouldn't have a resource", actualDataModel.getDataResource());

		} else {
			resourcesResourceTestUtils.compareObjects(expectedDataModel.getDataResource(), actualDataModel.getDataResource());
		}

		// check configuration
		if (expectedDataModel.getConfiguration() == null) {

			Assert.assertNull("the actual data model shouldn't have a configuration", actualDataModel.getConfiguration());

		} else {
			configurationsResourceTestUtils.compareObjects(expectedDataModel.getConfiguration(), actualDataModel.getConfiguration());
		}

		// check schema
		if (expectedDataModel.getSchema() == null) {

			Assert.assertNull("the actual data model shouldn't have a schema", actualDataModel.getSchema());

		} else {
			schemasResourceTestUtils.compareObjects(expectedDataModel.getSchema(), actualDataModel.getSchema());
		}
	}

	/**
	 * {@inheritDoc}<br/>
	 * Updates the name, description, resource, configuration and schema of the data model.
	 */
	@Override
	protected DataModel prepareObjectForUpdate(final DataModel objectWithUpdates, final DataModel object) {

		super.prepareObjectForUpdate(objectWithUpdates, object);

		object.setDataResource(objectWithUpdates.getDataResource());
		object.setConfiguration(objectWithUpdates.getConfiguration());
		object.setSchema(objectWithUpdates.getSchema());

		return object;
	}

	@Override
	public void reset() {

		schemasResourceTestUtils.reset();
		resourcesResourceTestUtils.reset();
		configurationsResourceTestUtils.reset();
	}
}
