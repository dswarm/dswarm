package de.avgl.dmp.persistence.service.resource.test.utils;

import de.avgl.dmp.persistence.model.resource.DataModel;
import de.avgl.dmp.persistence.model.resource.proxy.ProxyDataModel;
import de.avgl.dmp.persistence.service.resource.DataModelService;
import de.avgl.dmp.persistence.service.schema.test.utils.SchemaServiceTestUtils;
import de.avgl.dmp.persistence.service.test.utils.ExtendedBasicDMPJPAServiceTestUtils;

public class DataModelServiceTestUtils extends ExtendedBasicDMPJPAServiceTestUtils<DataModelService, ProxyDataModel, DataModel> {

	private final ResourceServiceTestUtils		resourcesResourceTestUtils;

	private final ConfigurationServiceTestUtils	configurationsResourceTestUtils;

	private final SchemaServiceTestUtils			schemasResourceTestUtils;

	public DataModelServiceTestUtils() {

		super(DataModel.class, DataModelService.class);

		resourcesResourceTestUtils = new ResourceServiceTestUtils();
		configurationsResourceTestUtils = new ConfigurationServiceTestUtils();
		schemasResourceTestUtils = new SchemaServiceTestUtils();
	}

	@Override
	public void compareObjects(final DataModel expectedObject, final DataModel actualObject) {

		super.compareObjects(expectedObject, actualObject);

		compareDataModels(expectedObject, actualObject);
	}

	private void compareDataModels(final DataModel expectedDataModel, final DataModel actualDataModel) {

		// TODO: re-enable reference deserializer or manually retrieve objects by from DB

		if (expectedDataModel.getDataResource() != null) {

			resourcesResourceTestUtils.compareObjects(expectedDataModel.getDataResource(), actualDataModel.getDataResource());
		}

		if (expectedDataModel.getConfiguration() != null) {

			configurationsResourceTestUtils.compareObjects(expectedDataModel.getConfiguration(), actualDataModel.getConfiguration());
		}

		if (expectedDataModel.getSchema() != null) {

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
