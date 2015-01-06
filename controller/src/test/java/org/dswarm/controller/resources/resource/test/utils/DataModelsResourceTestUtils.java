/**
 * Copyright (C) 2013 – 2015 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dswarm.controller.resources.resource.test.utils;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.Assert;

import org.dswarm.controller.resources.test.utils.ExtendedBasicDMPResourceTestUtils;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.resource.proxy.ProxyDataModel;
import org.dswarm.persistence.service.resource.DataModelService;
import org.dswarm.persistence.service.resource.test.utils.DataModelServiceTestUtils;

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
