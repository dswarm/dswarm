package de.avgl.dmp.controller.resources.resource.test.utils;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.Assert;

import de.avgl.dmp.controller.resources.test.utils.ExtendedBasicDMPResourceTestUtils;
import de.avgl.dmp.persistence.model.resource.DataModel;
import de.avgl.dmp.persistence.model.resource.proxy.ProxyDataModel;
import de.avgl.dmp.persistence.service.resource.DataModelService;
import de.avgl.dmp.persistence.service.resource.test.utils.DataModelServiceTestUtils;

public class DataModelsResourceTestUtils extends
		ExtendedBasicDMPResourceTestUtils<DataModelServiceTestUtils, DataModelService, ProxyDataModel, DataModel> {

	public DataModelsResourceTestUtils() {

		super("datamodels", DataModel.class, DataModelService.class, DataModelServiceTestUtils.class);
	}

	public String getData(final Long dataModelId, final int atMost) {

		final Response response1 = target(String.valueOf(dataModelId), "data").queryParam("atMost", atMost).request()
				.accept(MediaType.APPLICATION_JSON_TYPE).get(Response.class);

		Assert.assertEquals("200 OK was expected", 200, response1.getStatus());

		final String responseString = response1.readEntity(String.class);

		return responseString;
	}
}
