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
package org.dswarm.persistence.service;

import java.util.Map;

import com.google.common.base.Optional;

import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.internal.Model;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.schema.Schema;

/**
 * A service for persistence work with internal models, i.e., data of {@link DataModel}s.
 * 
 * @author phorn
 * @author tgaengler
 */
public interface InternalModelService {

	/**
	 * Persists an object (model) to a data model.
	 * 
	 * @param dataModelId the identifier of the data model
	 * @param model the model of the object that should be persisted
	 * @throws DMPPersistenceException
	 */
	void createObject(Long dataModelId, Object model) throws DMPPersistenceException;

	/**
	 * Retrieves a collection of objects from a data model.
	 * 
	 * @param dataModelId the identifier of the data model
	 * @param atMost the number of objects that should be retrieved at most
	 * @return (optional) a map of objects and their identifier
	 * @throws DMPPersistenceException
	 */
	Optional<Map<String, Model>> getObjects(Long dataModelId, Optional<Integer> atMost) throws DMPPersistenceException;

	/**
	 * Deletes a whole data model (incl. all its objects).
	 * 
	 * @param dataModelId the identifier of the data model
	 * @throws DMPPersistenceException
	 */
	void deleteObject(Long dataModelId) throws DMPPersistenceException;

	/**
	 * Retrieves the schema of the data model.
	 * 
	 * @param dataModelId the identifier of the data model
	 * @return (optional) the schema of the data model
	 * @throws DMPPersistenceException
	 */
	Optional<Schema> getSchema(Long dataModelId) throws DMPPersistenceException;
}
