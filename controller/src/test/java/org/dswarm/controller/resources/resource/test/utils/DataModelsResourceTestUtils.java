/**
 * Copyright (C) 2013 – 2017 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.Assert;

import org.dswarm.controller.resources.resource.DataModelsResource;
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

	public String getData(final String dataModelId, final int atMost) {

		final Response response1 = target(String.valueOf(dataModelId), "data").queryParam("atMost", atMost).request()
				.accept(MediaType.APPLICATION_JSON_TYPE).get(Response.class);

		Assert.assertEquals("200 OK was expected", 200, response1.getStatus());

		return response1.readEntity(String.class);
	}

	public DataModel createObject(final String objectJSONString, final DataModel expectedObject, final boolean doIngest, final boolean enhanceDataResource) throws Exception {

		final DataModel actualObject = createObjectWithoutComparison(objectJSONString, doIngest, enhanceDataResource);
		compareObjects(expectedObject, actualObject);

		return actualObject;
	}

	/**
	 * Creates the object in db and asserts the response status is '201 created' but does not compare the response with the JSON
	 * string.
	 *
	 * @param objectJSONString the JSON string of the object to be created
	 * @return the actual object as created in db, never null.
	 * @throws Exception
	 */
	public DataModel createObjectWithoutComparison(final String objectJSONString, final boolean doIngest, final boolean enhanceDataResource) throws Exception {

		final Response response;

		if (!doIngest || enhanceDataResource) {

			final Map<String, String> queryParams = new LinkedHashMap<>();

			if(!doIngest) {

				queryParams.put(DataModelsResource.DO_INGEST_QUERY_PARAM_IDENTIFIER, Boolean.FALSE.toString());
			}

			if(enhanceDataResource) {

				queryParams.put(DataModelsResource.ENHANCE_DATA_RESOURCE_QUERY_PARAM_IDENTIFIER, Boolean.TRUE.toString());
			}

			response = executeCreateObject(objectJSONString, Optional.of(queryParams));
		} else {

			response = executeCreateObject(objectJSONString);
		}

		Assert.assertEquals("201 Created was expected", 201, response.getStatus());

		final String responseString = response.readEntity(String.class);

		Assert.assertNotNull("the response JSON shouldn't be null", responseString);

		return readObject(responseString);
	}

	public Response executeCreateObject(final String objectJSONString, Optional<Map<String, String>> optionalQueryParams) throws Exception {

		WebTarget target = target();

		if (optionalQueryParams.isPresent()) {

			for (final Map.Entry<String, String> queryParamEntry : optionalQueryParams.get().entrySet()) {

				final String key = queryParamEntry.getKey();
				final String value = queryParamEntry.getValue();

				target = target.queryParam(key, value);
			}
		}

		return target.request(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE).post(Entity.json(objectJSONString));
	}
}
