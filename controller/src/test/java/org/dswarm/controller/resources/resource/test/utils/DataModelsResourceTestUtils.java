package org.dswarm.controller.resources.resource.test.utils;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.dswarm.controller.resources.test.utils.ExtendedBasicDMPResourceTestUtils;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.resource.proxy.ProxyDataModel;
import org.dswarm.persistence.service.resource.DataModelService;
import org.dswarm.persistence.service.resource.test.utils.DataModelServiceTestUtils;
import org.junit.Assert;

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
