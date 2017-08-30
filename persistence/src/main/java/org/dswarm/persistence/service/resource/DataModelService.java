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
package org.dswarm.persistence.service.resource;

import javax.persistence.EntityManager;

import com.google.inject.Inject;
import com.google.inject.Provider;

import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.dto.resource.MediumDataModelDTO;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.resource.Resource;
import org.dswarm.persistence.model.resource.proxy.ProxyDataModel;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.service.ExtendedMediumBasicDMPJPAService;

/**
 * A persistence service for {@link DataModel}s.
 *
 * @author tgaengler
 */
public class DataModelService extends ExtendedMediumBasicDMPJPAService<ProxyDataModel, DataModel, MediumDataModelDTO> {

	/**
	 * Creates a new data model persistence service with the given entity manager provider.
	 *
	 * @param entityManagerProvider an entity manager provider
	 */
	@Inject
	public DataModelService(final Provider<EntityManager> entityManagerProvider) {

		super(DataModel.class, ProxyDataModel.class, entityManagerProvider);
	}

	/**
	 * {@inheritDoc}<br/>
	 * Clears the relationship to schema, data resource and configuration.
	 */
	@Override
	protected void prepareObjectForRemoval(final DataModel object) {

		// should clear the relationship to the schema, data resource, configuration
		object.setSchema(null);
		object.setDataResource(null);
		object.setConfiguration(null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateObjectInternal(final DataModel object, final DataModel updateObject)
			throws DMPPersistenceException {

		super.updateObjectInternal(object, updateObject);

		final Schema schema = object.getSchema();
		final Resource resource = object.getDataResource();
		final Configuration configuration = object.getConfiguration();
		final boolean deprecated = object.isDeprecated();

		updateObject.setSchema(schema);
		updateObject.setDataResource(resource);
		updateObject.setConfiguration(configuration);
		updateObject.setDeprecated(deprecated);
	}

	@Override
	public MediumDataModelDTO createMediumVariant(final DataModel object) {
		return MediumDataModelDTO.of(object, null);
	}
}
