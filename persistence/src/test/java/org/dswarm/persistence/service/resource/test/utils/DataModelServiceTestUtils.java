/**
 * Copyright (C) 2013, 2014 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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
package org.dswarm.persistence.service.resource.test.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.json.JSONException;
import org.junit.Assert;

import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.resource.Resource;
import org.dswarm.persistence.model.resource.proxy.ProxyDataModel;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.service.resource.DataModelService;
import org.dswarm.persistence.service.schema.test.utils.SchemaServiceTestUtils;
import org.dswarm.persistence.service.test.utils.ExtendedBasicDMPJPAServiceTestUtils;

public class DataModelServiceTestUtils extends ExtendedBasicDMPJPAServiceTestUtils<DataModelService, ProxyDataModel, DataModel> {

	private final ResourceServiceTestUtils resourcesResourceTestUtils;

	private final ConfigurationServiceTestUtils configurationsResourceTestUtils;

	private final SchemaServiceTestUtils schemasResourceTestUtils;

	public DataModelServiceTestUtils() {

		super(DataModel.class, DataModelService.class);

		resourcesResourceTestUtils = new ResourceServiceTestUtils();
		configurationsResourceTestUtils = new ConfigurationServiceTestUtils(resourcesResourceTestUtils);
		schemasResourceTestUtils = new SchemaServiceTestUtils();
	}

	@Override public DataModel createObject(JsonNode objectDescription) throws Exception {
		return null;
	}

	@Override public DataModel createObject(String identifier) throws Exception {
		return null;
	}

	@Override public DataModel createAndPersistDefaultObject() throws Exception {

		final String dataModelName = "my data model";
		final String dataModelDescription = "my data model description";

		final Schema schema = schemasResourceTestUtils.createAlternativeSchema();
		final Resource resource = resourcesResourceTestUtils.createAndPersistDefaultObject();
		final Configuration configuration = configurationsResourceTestUtils.createAndPersistDefaultObject();
		resource.addConfiguration(configuration);
		final Resource updatedResource = resourcesResourceTestUtils.updateAndCompareObject(resource, resource);

		return createDataModel(dataModelName, dataModelDescription, updatedResource, configuration, schema);
	}

	@Override public DataModel createDefaultObject() throws Exception {
		return null;
	}

	public DataModel createDataModel(final String name, final String description, final Resource resource, final Configuration configuration,
			final Schema schema)
			throws Exception {

		final DataModel dataModel = new DataModel();
		dataModel.setName(name);
		dataModel.setDescription(description);
		dataModel.setDataResource(resource);
		dataModel.setConfiguration(configuration);
		dataModel.setSchema(schema);

		return createAndCompareObject(dataModel, dataModel);
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
	public void compareObjects(final DataModel expectedDataModel, final DataModel actualDataModel) throws JsonProcessingException, JSONException {

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
