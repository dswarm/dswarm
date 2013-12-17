package de.avgl.dmp.controller.resources.test.utils;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.Assert;

import de.avgl.dmp.persistence.model.resource.DataModel;
import de.avgl.dmp.persistence.service.resource.DataModelService;

public class DataModelsResourceTestUtils extends ExtendedBasicDMPResourceTestUtils<DataModelService, DataModel> {

	private final ResourcesResourceTestUtils		resourcesResourceTestUtils;

	private final ConfigurationsResourceTestUtils	configurationsResourceTestUtils;

	private final SchemasResourceTestUtils			schemasResourceTestUtils;

	public DataModelsResourceTestUtils() {

		super("datamodels", DataModel.class, DataModelService.class);

		resourcesResourceTestUtils = new ResourcesResourceTestUtils();
		configurationsResourceTestUtils = new ConfigurationsResourceTestUtils();
		schemasResourceTestUtils = new SchemasResourceTestUtils();
	}

	@Override
	public void compareObjects(final DataModel expectedObject, final DataModel actualObject) {

		super.compareObjects(expectedObject, actualObject);

		compareDataModels(expectedObject, actualObject);
	}
	
	public String getData(final Long dataModelId, final int atMost) {

		final Response response1 = target(String.valueOf(dataModelId), "data")
				.queryParam("atMost", atMost).request().accept(MediaType.APPLICATION_JSON_TYPE).get(Response.class);

		Assert.assertEquals("200 OK was expected", 200, response1.getStatus());

		final String responseString = response1.readEntity(String.class);

		return responseString;
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

	@Override
	public void reset() {
		
		schemasResourceTestUtils.reset();
		resourcesResourceTestUtils.reset();
		configurationsResourceTestUtils.reset();
	}
}
