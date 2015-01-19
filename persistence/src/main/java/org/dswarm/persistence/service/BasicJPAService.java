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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;

import javax.persistence.CacheRetrieveMode;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import com.google.inject.Provider;
import com.google.inject.persist.Transactional;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.DMPObject;
import org.dswarm.persistence.model.proxy.ProxyDMPObject;
import org.dswarm.persistence.model.proxy.RetrievalType;

/**
 * A generic persistence service implementation, whose concrete implementations can be derived with a given implementation of
 * {@link DMPObject} and the related identifier type. This service delivers basic persistence layer functionality to create a new
 * object, update an existing one, retrieve existing ones or delete existing objects.
 *
 * @param <POJOCLASS> the concrete POJO class
 * @author tgaengler
 */
public abstract class BasicJPAService<PROXYPOJOCLASS extends ProxyDMPObject<POJOCLASS>, POJOCLASS extends DMPObject> {

	private static final Logger LOG = LoggerFactory.getLogger(BasicJPAService.class);

	/**
	 * The concrete POJO class of this persistence service.
	 */
	protected final Class<POJOCLASS> clasz;

	/**
	 * The name of the concrete POJO class this persistence service.
	 */
	protected final String className;

	/**
	 * The concrete proxy POJO class of this persistence service.
	 */
	protected final Class<PROXYPOJOCLASS> proxyClasz;

	/**
	 * The name of the concrete proxy POJO class this persistence service.
	 */
	protected final String proxyClassName;

	/**
	 * The entity manager provider (powered by Guice).
	 */
	private final Provider<EntityManager> entityManagerProvider;

	/**
	 * Creates a new persistence service for the given concrete POJO class and the entity manager provider.
	 *
	 * @param clasz                 a concrete POJO class
	 * @param entityManagerProvider an entity manager provider
	 */
	public BasicJPAService(final Class<POJOCLASS> clasz, final Class<PROXYPOJOCLASS> proxyClasz,
			final Provider<EntityManager> entityManagerProvider) {

		this.clasz = clasz;
		this.className = clasz.getSimpleName();

		this.proxyClasz = proxyClasz;
		this.proxyClassName = proxyClasz.getSimpleName();

		this.entityManagerProvider = entityManagerProvider;
	}

	/**
	 * Acquire a new or reused EntityManager with its cache cleared
	 *
	 * @return the EntityManager
	 */
	protected EntityManager acquire() {
		return acquire(true);
	}

	/**
	 * Acquire a new or reused EntityManager
	 *
	 * @param clear true if the EM's cache should be cleared
	 * @return the EntityManager
	 */
	protected EntityManager acquire(final boolean clear) {

		final EntityManager entityManager = entityManagerProvider.get();
		if (clear) {
			entityManager.clear();
		}

		return entityManager;
	}

	/**
	 * Gets the concrete POJO class of this persistence service.
	 *
	 * @return the concrete POJO class
	 */
	public Class<POJOCLASS> getClasz() {

		return clasz;
	}

	/**
	 * Create and persist an object of the specific class transactional.<br>
	 *
	 * @return the persisted object of the specific class
	 */
	@Transactional(rollbackOn = Exception.class)
	public PROXYPOJOCLASS createObjectTransactional() throws DMPPersistenceException {

		return createObject();
	}

	/**
	 * Create and persist an object of the specific class transactional with the given uuid.<br>
	 *
	 * @param uuid the uuid that should be utilised to created the object
	 * @return the persisted object of the specific class with the given uuid
	 */
	@Transactional(rollbackOn = Exception.class)
	public PROXYPOJOCLASS createObjectTransactional(final String uuid) throws DMPPersistenceException {

		return createObject(uuid);
	}

	/**
	 * Create and persist an object of the specific class non-transactional.<br>
	 *
	 * @return the persisted object of the specific class
	 */
	public PROXYPOJOCLASS createObject() throws DMPPersistenceException {

		final EntityManager entityManager = acquire(false);

		return createObjectInternal(entityManager);
	}

	/**
	 * Create and persist an object of the specific class non-transactional with the given uuid.<br>
	 *
	 * @param uuid the uuid that should be utilised to created the object
	 * @return the persisted object of the specific class with the given uuid
	 */
	public PROXYPOJOCLASS createObject(final String uuid) throws DMPPersistenceException {

		final EntityManager entityManager = acquire(false);

		return createObjectInternal(uuid, entityManager);
	}

	protected PROXYPOJOCLASS createObjectInternal(final EntityManager entityManager) throws DMPPersistenceException {

		// i.e. uuid will be created on demand in createNewObject
		final POJOCLASS object = createNewObject(null);

		persistObject(object, entityManager);

		return createNewProxyObject(object);
	}

	protected PROXYPOJOCLASS createObjectInternal(final String uuid, final EntityManager entityManager) throws DMPPersistenceException {

		final POJOCLASS object = createNewObject(uuid);

		persistObject(object, entityManager);

		return createNewProxyObject(object);
	}

	/**
	 * Create and persist an object of the specific class transactional. The given object can be utilised for initialisation of
	 * the persisted object or to ensure certain constraints, e.g., a uniqueness constraint.<br>
	 *
	 * @param object
	 * @return
	 * @throws DMPPersistenceException
	 */
	@Transactional(rollbackOn = Exception.class)
	public PROXYPOJOCLASS createObjectTransactional(final POJOCLASS object) throws DMPPersistenceException {

		return createObjectInternal(object, "transactional");
	}

	/**
	 * Create and persist an object of the specific class non-transactional. The given object can be utilised for initialisation
	 * of the persisted object or to ensure certain constraints, e.g., a uniqueness constraint.<br>
	 *
	 * @param object
	 * @return the persisted object of the specific class
	 * @throws DMPPersistenceException
	 */
	public PROXYPOJOCLASS createObject(final POJOCLASS object) throws DMPPersistenceException {

		return createObjectInternal(object, "non-transactional");
	}

	protected PROXYPOJOCLASS createObjectInternal(final POJOCLASS object, final String transactionType) throws DMPPersistenceException {

		final EntityManager entityManager = acquire(false);

		return createObjectInternal(object, entityManager, transactionType);
	}

	protected PROXYPOJOCLASS createObjectInternal(final POJOCLASS object, final EntityManager entityManager, final String transactionType)
			throws DMPPersistenceException {

		// TODO: shall we check, whether the entity with the UUID already exists in the DB, or not?
		final POJOCLASS newObject = createNewObject(object.getUuid());

		persistObject(newObject, entityManager);

		updateObjectInternal(object, newObject, entityManager);

		entityManager.merge(newObject);

		return createNewProxyObject(newObject, RetrievalType.CREATED);
	}

	/**
	 * Creates (if it doesn't exist before) or updates a given instance of the specific class and writes this object persistent to
	 * the DB afterwards.<br>
	 * Created by: tgaengler
	 *
	 * @param object the to be updated instance of the specific class
	 */
	@Transactional(rollbackOn = Exception.class)
	public PROXYPOJOCLASS updateObjectTransactional(final POJOCLASS object) throws DMPPersistenceException {

		// TODO [@tgaengler]: maybe we should also make use of a _fresh_ entity manager here (cf. concurrent modification exception case from Resource <-> Configuration
		final EntityManager entityManager = acquire(false);

		return updateObjectInternal(object, entityManager, "transactional");
	}

	/**
	 * Updates a given instance of the specific class without writing this object persistent right now, i.e., the process that
	 * calls this method needs to ensure that the update will be written persistent.<br>
	 * Created by: tgaengler
	 *
	 * @param object the to be updated instance of the specific class
	 */
	public PROXYPOJOCLASS updateObject(final POJOCLASS object) throws DMPPersistenceException {

		final EntityManager entityManager = acquire(false);

		return updateObjectInternal(object, entityManager, "non-transactional");
	}

	protected PROXYPOJOCLASS updateObjectInternal(final POJOCLASS object, final EntityManager entityManager, final String transactionType)
			throws DMPPersistenceException {

		final PROXYPOJOCLASS proxyUpdateObject = getObject(object, entityManager, transactionType);

		if (proxyUpdateObject == null) {

			BasicJPAService.LOG.debug("couldn't continue " + transactionType + " update for " + className + " with id '" + object.getUuid()
					+ "', because the proxy object is invalid.");

			return proxyUpdateObject;
		}

		if (proxyUpdateObject.getObject() == null) {

			BasicJPAService.LOG.debug("couldn't continue " + transactionType + " update for " + className + " with id '" + object.getUuid()
					+ "', because the retrieved/created object is invalid.");

			return proxyUpdateObject;
		}

		BasicJPAService.LOG.debug("try to update " + className + " with id '" + object.getUuid() + "' " + transactionType);

		final POJOCLASS updateObject = proxyUpdateObject.getObject();

		updateObjectInternal(object, updateObject, entityManager);

		entityManager.merge(updateObject);

		BasicJPAService.LOG.debug("updated " + className + " with id '" + object.getUuid() + "' " + transactionType);

		if (updateObject != null) {

			BasicJPAService.LOG.debug("updated " + className + " with id '" + updateObject.getUuid() + "' in the database " + transactionType);
			BasicJPAService.LOG.trace("= '" + ToStringBuilder.reflectionToString(updateObject) + "'");
		} else {

			BasicJPAService.LOG.debug("couldn't updated " + className + " with id '" + object.getUuid() + "' in the database " + transactionType);
		}

		return proxyUpdateObject;
	}

	/**
	 * The internal update method for the specific class that will be called from {@link BasicJPAService#updateObject(POJOCLASS)}
	 * and {@link BasicJPAService#updateObjectTransactional(POJOCLASS)}, i.e., this method includes the real update logic of this
	 * service.<br>
	 * Created by: tgaengler
	 *
	 * @param object        the instance of the specific class with the update data
	 * @param updateObject  the to be updated instance of the specific class
	 * @param entityManager the {@link EntityManager} instance for managing the update process
	 * @throws DMPPersistenceException
	 */
	protected abstract void updateObjectInternal(final POJOCLASS object, final POJOCLASS updateObject, final EntityManager entityManager)
			throws DMPPersistenceException;

	/**
	 * Generic 'find all instances of a specific class' method.<br>
	 * Created by: tgaengler
	 *
	 * @return the instance list of the specific class
	 */
	public List<POJOCLASS> getObjects() {

		final EntityManager entityManager = acquire();
		final TypedQuery<POJOCLASS> query = entityManager.createQuery("SELECT o FROM " + className + " o", clasz);

		return query.getResultList();
	}

	/**
	 * Generic 'exist instance for identifier of a specific class' method.<br>
	 * Created by: tgaengler
	 *
	 * @param id the idenfier of the requested instance of a specific class
	 * @return the instance for the identifier of the specific class
	 */
	public POJOCLASS getObject(final String id) {

		final EntityManager entityManager = acquire();

		return getObjectInternal(id, entityManager);
	}

	protected PROXYPOJOCLASS getObjectInternal(final POJOCLASS object, final EntityManager entityManager) throws DMPPersistenceException {

		if (object == null) {

			return null;
		}

		final POJOCLASS retrievedObject = getObjectInternal(object.getUuid(), entityManager);

		return createNewProxyObject(retrievedObject, RetrievalType.RETRIEVED);
	}

	protected POJOCLASS getObjectInternal(final String uuid, final EntityManager entityManager) {

		BasicJPAService.LOG.debug("try to find " + className + " with uuid '" + uuid + "' in the database");

		final POJOCLASS entity = entityManager.find(clasz, uuid);
		/*,
				Collections.<String, Object>singletonMap("javax.persistence.cache.retrieveMode", CacheRetrieveMode.BYPASS));*/

		if (entity != null) {

			BasicJPAService.LOG.debug("found " + className + " with uuid '" + uuid + "' in the database");
			BasicJPAService.LOG.trace(" = '" + ToStringBuilder.reflectionToString(entity) + "'");
		} else {

			BasicJPAService.LOG.debug("couldn't find " + className + " with uuid '" + uuid + "' in the database");
		}

		return entity;
	}

	/**
	 * Deletes an instance of the specific class permanently from the DB by a given identifier.<br>
	 * Created by: tgaengler
	 *
	 * @param uuid the identifier of the to be deleted instance of the specific class
	 */
	@Transactional(rollbackOn = Exception.class)
	public void deleteObject(final String uuid) {

		final EntityManager entityManager = acquire(false);
		final POJOCLASS updateObject = entityManager.find(clasz, uuid);

		BasicJPAService.LOG.debug("try to delete " + className + " with uuid '" + uuid + "' from the database");

		prepareObjectForRemoval(updateObject);

		entityManager.remove(updateObject);

		BasicJPAService.LOG.debug("deleted " + className + " with uuid '" + uuid + "' from the database");
	}

	/**
	 * Prepares the given object for removal, i.e., disconnect the object from all related objects that shouldn't be deleted at
	 * this time.
	 *
	 * @param object an object that should be deleted
	 */
	protected abstract void prepareObjectForRemoval(final POJOCLASS object);

	/**
	 * Tries to retrieve a POJOCLASS instance or will create a new one, if the identifier is null.<br>
	 * Created by: tgaengler
	 *
	 * @return the requested POJOCLASS instance fresh from the DB or a new POJOCLASS instance
	 * @throws DMPPersistenceException
	 */
	protected PROXYPOJOCLASS getObject(final POJOCLASS object, final EntityManager entityManager, final String transactionType)
			throws DMPPersistenceException {

		final PROXYPOJOCLASS proxyUpdateObject;

		// second condition is for new object creation on dummy id
		if (object.getUuid() == null) {

			// TODO: we don't need to generate new objects here, or?

			BasicJPAService.LOG.debug(className + " id is null, will create a new " + className);

			proxyUpdateObject = createObjectInternal(object, entityManager, transactionType);
		} else {

			proxyUpdateObject = getObjectInternal(object, entityManager);
		}

		return proxyUpdateObject;
	}

	protected void persistObject(final POJOCLASS object, final EntityManager entityManager) {

		BasicJPAService.LOG.debug("try to create new " + className);

		entityManager.persist(object);

		BasicJPAService.LOG.debug("created new " + className + " with id '" + object.getUuid() + "'");
	}

	/**
	 * Creates a new proxy object of the concrete proxy POJO class.
	 *
	 * @return a new instance of the concrete proxy POJO class
	 * @throws DMPPersistenceException if something went wrong.
	 */
	protected PROXYPOJOCLASS createNewProxyObject(final POJOCLASS object) throws DMPPersistenceException {

		final PROXYPOJOCLASS proxyObject;

		Constructor<PROXYPOJOCLASS> constructor = null;

		try {
			constructor = proxyClasz.getConstructor(clasz);
		} catch (final SecurityException | NoSuchMethodException e1) {

			throw new DMPPersistenceException(e1.getMessage());
		}

		if (null == constructor) {

			throw new DMPPersistenceException("couldn't find constructor to instantiate new '" + proxyClassName + "' with for " + className + " '"
					+ object.getUuid() + "'");
		}

		try {

			proxyObject = constructor.newInstance(object);
		} catch (final InstantiationException | InvocationTargetException | IllegalArgumentException | IllegalAccessException e) {

			BasicJPAService.LOG.error("something went wrong while " + proxyClassName + " object creation", e);

			throw new DMPPersistenceException(e.getMessage());
		}

		return proxyObject;
	}

	/**
	 * Creates a new proxy object of the concrete proxy POJO class.
	 *
	 * @return a new instance of the concrete proxy POJO class
	 * @throws DMPPersistenceException if something went wrong.
	 */
	public PROXYPOJOCLASS createNewProxyObject(final POJOCLASS object, final RetrievalType type) throws DMPPersistenceException {

		final PROXYPOJOCLASS proxyObject;

		Constructor<PROXYPOJOCLASS> constructor = null;

		try {
			constructor = proxyClasz.getConstructor(clasz, RetrievalType.class);
		} catch (final SecurityException | NoSuchMethodException e1) {

			throw new DMPPersistenceException(e1.getMessage());
		}

		if (null == constructor) {

			throw new DMPPersistenceException("couldn't find constructor to instantiate new '" + proxyClassName + "' with for " + className + " '"
					+ object.getUuid() + "' and type '" + type + "'");
		}

		try {

			proxyObject = constructor.newInstance(object, type);
		} catch (final InstantiationException | InvocationTargetException | IllegalArgumentException | IllegalAccessException e) {

			BasicJPAService.LOG.error("something went wrong while " + proxyClassName + " object creation", e);

			throw new DMPPersistenceException(e.getMessage());
		}

		return proxyObject;
	}

	/**
	 * Creates a new object of the concrete POJO class.
	 *
	 * @return a new instance of the concrete POJO class
	 * @throws DMPPersistenceException if something went wrong.
	 */
	private POJOCLASS createNewObject(final String uuid) throws DMPPersistenceException {

		//		final POJOCLASS object;
		//
		//		try {
		//
		//			object = clasz.newInstance();
		//		} catch (final InstantiationException | IllegalAccessException e) {
		//
		//			BasicJPAService.LOG.error("something went wrong while " + className + "object creation", e);
		//
		//			throw new DMPPersistenceException(e.getMessage());
		//		}
		//
		//		return object;

		final POJOCLASS object;

		Constructor<POJOCLASS> constructor = null;

		try {
			constructor = clasz.getConstructor(String.class);
		} catch (final SecurityException | NoSuchMethodException e1) {

			throw new DMPPersistenceException(e1.getMessage());
		}

		if (null == constructor) {

			throw new DMPPersistenceException("couldn't find constructor to instantiate new '" + className + "' with a uuid");
		}

		final String finalUUID;

		if (uuid != null) {

			finalUUID = uuid;
		} else {
			finalUUID = UUIDService.getUUID(className);
		}

		try {

			object = constructor.newInstance(finalUUID);
		} catch (final InstantiationException | InvocationTargetException | IllegalArgumentException | IllegalAccessException e) {

			BasicJPAService.LOG.error("something went wrong while " + className + "object creation", e);

			throw new DMPPersistenceException(e.getMessage());
		}

		return object;
	}
}
