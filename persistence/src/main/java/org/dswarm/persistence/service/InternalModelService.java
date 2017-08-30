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
package org.dswarm.persistence.service;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import javax.ws.rs.core.Response;

import javaslang.Tuple2;
import rx.Observable;

import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.internal.Model;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.resource.UpdateFormat;
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
	 * @param dataModelUuid the identifier of the data model
	 * @param model         the model of the object that should be persisted
	 * @throws DMPPersistenceException
	 */
	Observable<Response> createObject(final String dataModelUuid, final Observable<Model> model) throws DMPPersistenceException;

	/**
	 * Updates an object (model) to an existing data model.
	 *
	 * @param dataModelUuid the identifier of the data model
	 * @param model         the model of the object that should be updated
	 * @throws DMPPersistenceException
	 */
	Observable<Response> updateObject(final String dataModelUuid, final Observable<Model> model, final UpdateFormat updateFormat,
			final boolean enableVersioning) throws DMPPersistenceException;

	/**
	 * Retrieves a collection of objects from a data model.
	 *
	 * @param dataModelUuid the identifier of the data model
	 * @param atMost        the number of objects that should be retrieved at most
	 * @return (optional) a map of objects and their identifier
	 * @throws DMPPersistenceException
	 */
	Observable<Tuple2<String, Model>> getObjects(final String dataModelUuid, final Optional<Integer> atMost) throws DMPPersistenceException;

	/**
	 * Deletes a whole data model (incl. all its objects).
	 *
	 * @param dataModelUuid the identifier of the data model
	 * @throws DMPPersistenceException
	 */
	void deleteObject(final String dataModelUuid) throws DMPPersistenceException;

	/**
	 * Deprecates a whole data model (incl. all its objects).
	 *
	 * @param dataModelUuid the identifier of the data model
	 * @throws DMPPersistenceException
	 */
	Observable<Response> deprecateDataModel(final String dataModelUuid) throws DMPPersistenceException;

	/**
	 * Deprecate some records of a data model.
	 *
	 * @param dataModelUuid the identifier of the data model
	 * @throws DMPPersistenceException
	 */
	Observable<Response> deprecateRecords(final Collection<String> recordURIs, final String dataModelUuid) throws DMPPersistenceException;

	/**
	 * Retrieves the schema of the data model.
	 *
	 * @param dataModelUuid the identifier of the data model
	 * @return (optional) the schema of the data model
	 * @throws DMPPersistenceException
	 */
	Optional<Schema> getSchema(final String dataModelUuid) throws DMPPersistenceException;

	/**
	 * Retrieves a collection of objects from a data model that matches the search criteria.
	 *
	 * @param dataModelUuid the identifier of the data model
	 * @param keyAttributePathString the key attribute path as string
	 * @param searchValue the search value
	 * @param atMost        the number of objects that should be retrieved at most
	 * @return (optional) a map of objects and their identifier
	 * @throws DMPPersistenceException
	 */
	Observable<Tuple2<String, Model>> searchObjects(final String dataModelUuid, final String keyAttributePathString, final String searchValue,
			final Optional<Integer> atMost) throws DMPPersistenceException;

	/**
	 * Retrieves the record in the specific data model with the given record identifier from the datahub.
	 *
	 * @param recordIdentifier the record identifier
	 * @param dataModelUuid the identifier of the data model
	 * @return
	 * @throws DMPPersistenceException
	 */
	Observable<Model> getRecord(final String recordIdentifier, final String dataModelUuid) throws DMPPersistenceException;

	/**
	 * Retrieves the records in the specific data model with the given record identifiers from the datahub.
	 *
	 * @param recordIdentifiers the record identifiers
	 * @param dataModelUuid the identifier of the data model
	 * @return
	 * @throws DMPPersistenceException
	 */
	Observable<Tuple2<String, Model>> getRecords(final Set<String> recordIdentifiers, final String dataModelUuid) throws DMPPersistenceException;
}
