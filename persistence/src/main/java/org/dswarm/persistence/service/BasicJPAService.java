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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;

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

	protected static final String TRANSACTIONAL_TRANSACTION_TYPE     = "transactional";
	protected static final String NON_TRANSACTIONAL_TRANSACTION_TYPE = "non-transactional";

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

		return createObjectInternal(entityManager, PersistenceType.Merge);
	}

	/**
	 * Create and persist an object of the specific class non-transactional with the given uuid.<br>
	 *
	 * @param uuid the uuid that should be utilised to created the object
	 * @return the persisted object of the specific class with the given uuid
	 */
	public PROXYPOJOCLASS createObject(final String uuid) throws DMPPersistenceException {

		final EntityManager entityManager = acquire(false);

		return createObjectInternal(uuid, entityManager, PersistenceType.Merge);
	}

	/**
	 * Create and persist an object of the specific class non-transactional with the given uuid.<br>
	 *
	 * @param uuid the uuid that should be utilised to created the object
	 * @return the persisted object of the specific class with the given uuid
	 */
	public PROXYPOJOCLASS createObject(final String uuid, final PersistenceType persistenceType) throws DMPPersistenceException {

		final EntityManager entityManager = acquire(false);

		return createObjectInternal(uuid, entityManager, persistenceType);
	}

	protected PROXYPOJOCLASS createObjectInternal(final EntityManager entityManager, final PersistenceType persistenceType)
			throws DMPPersistenceException {

		// i.e. uuid will be created on demand in createNewObject
		final POJOCLASS object = createNewObject(null);

		final Optional<POJOCLASS> optionalPersistentObject = persistObject(object, entityManager, persistenceType);
		final POJOCLASS persistentObject = determinePersistentObject(object, optionalPersistentObject);

		return createNewProxyObject(persistentObject);
	}

	protected PROXYPOJOCLASS createObjectInternal(final String uuid, final EntityManager entityManager, final PersistenceType persistenceType)
			throws DMPPersistenceException {

		final POJOCLASS object = createNewObject(uuid);

		final Optional<POJOCLASS> optionalPersistentObject = persistObject(object, entityManager, persistenceType);
		final POJOCLASS persistentObject = determinePersistentObject(object, optionalPersistentObject);

		return createNewProxyObject(persistentObject);
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

		return createObjectInternal(object, TRANSACTIONAL_TRANSACTION_TYPE, PersistenceType.Merge);
	}

	/**
	 * Create and persist an object of the specific class transactional. The given object can be utilised for initialisation of
	 * the persisted object or to ensure certain constraints, e.g., a uniqueness constraint.<br>
	 *
	 * @param object
	 * @param persistenceType the persistence type on how the entity should be persisted in the database (i.e. with which persistence method from {@link EntityManager}
	 * @return
	 * @throws DMPPersistenceException
	 */
	@Transactional(rollbackOn = Exception.class)
	public PROXYPOJOCLASS createObjectTransactional(final POJOCLASS object, final PersistenceType persistenceType) throws DMPPersistenceException {

		return createObjectInternal(object, TRANSACTIONAL_TRANSACTION_TYPE, persistenceType);
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

		// TODO: change persistent type as required or make it a parameter of the method
		return createObjectInternal(object, NON_TRANSACTIONAL_TRANSACTION_TYPE, PersistenceType.Merge);
	}

	protected PROXYPOJOCLASS createObjectInternal(final POJOCLASS object, final String transactionType, final PersistenceType persistenceType)
			throws DMPPersistenceException {

		final EntityManager entityManager = acquire(false);

		return createObjectInternal(object, entityManager, transactionType, persistenceType);
	}

	protected PROXYPOJOCLASS createObjectInternal(final POJOCLASS object, final EntityManager entityManager, final String transactionType,
			final PersistenceType persistenceType)
			throws DMPPersistenceException {

		// TODO: shall we check, whether the entity with the UUID already exists in the DB, or not?
		final POJOCLASS newObject = createNewObject(object.getUuid());

		updateObjectInternal(object, newObject);

		// TODO: maybe merge updated entity into entity manager instance again (cf. updateObjectInternal)

		final Optional<POJOCLASS> optionalPersistentObject = persistObject(newObject, entityManager, persistenceType);

		final POJOCLASS persistentObject = determinePersistentObject(newObject, optionalPersistentObject);

		return createNewProxyObject(persistentObject, RetrievalType.CREATED);
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

		return updateObjectInternal(object, entityManager, TRANSACTIONAL_TRANSACTION_TYPE);
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

		return updateObjectInternal(object, entityManager, NON_TRANSACTIONAL_TRANSACTION_TYPE);
	}

	protected PROXYPOJOCLASS updateObjectInternal(final POJOCLASS object, final EntityManager entityManager, final String transactionType)
			throws DMPPersistenceException {

		// TODO: change persistent type as required or make it a parameter of the method
		final PROXYPOJOCLASS proxyUpdateObject = getObject(object, entityManager, transactionType, PersistenceType.Merge);

		if (proxyUpdateObject == null) {

			BasicJPAService.LOG
					.debug("couldn't continue {} update for {} with id '{}', because the proxy object is invalid.", transactionType, className,
							object.getUuid());

			return proxyUpdateObject;
		}

		if (proxyUpdateObject.getObject() == null) {

			BasicJPAService.LOG
					.debug("couldn't continue {} update for {} with id '{}', because the retrieved/created object is invalid.", transactionType,
							className, object.getUuid());

			return proxyUpdateObject;
		}

		BasicJPAService.LOG.debug("try to update {} with id '{}' {}", className, object.getUuid(), transactionType);

		final POJOCLASS updateObject = proxyUpdateObject.getObject();

		updateObjectInternal(object, updateObject);

		final POJOCLASS mergedUpdatedObject = entityManager.merge(updateObject);

		BasicJPAService.LOG.debug("updated {} with id '{}' {}", className, object.getUuid(), transactionType);

		if (updateObject != null) {

			BasicJPAService.LOG.debug("updated {} with id '{}' in the database {}", className, mergedUpdatedObject.getUuid(), transactionType);
			BasicJPAService.LOG.trace("= '{}'", ToStringBuilder.reflectionToString(mergedUpdatedObject));
		} else {

			BasicJPAService.LOG.debug("couldn't updated {} with id '{}' in the database {}", className, object.getUuid(), transactionType);
		}

		return createNewProxyObject(mergedUpdatedObject, proxyUpdateObject.getType());
	}

	/**
	 * The internal update method for the specific class that will be called from {@link BasicJPAService#updateObject(POJOCLASS)}
	 * and {@link BasicJPAService#updateObjectTransactional(POJOCLASS)}, i.e., this method includes the real update logic of this
	 * service.<br>
	 * Created by: tgaengler
	 *
	 * @param object        the instance of the specific class with the update data
	 * @param updateObject  the to be updated instance of the specific class
	 * @throws DMPPersistenceException
	 */
	public abstract void updateObjectInternal(final POJOCLASS object, final POJOCLASS updateObject)
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
	 * @param id the identifier of the requested instance of a specific class
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

		BasicJPAService.LOG.debug("try to find {} with uuid '{}' in the database", className, uuid);

		final POJOCLASS entity = entityManager.find(clasz, uuid);

		if (entity != null) {

			BasicJPAService.LOG.debug("found {} with uuid '{}' in the database", className, uuid);
			if (BasicJPAService.LOG.isTraceEnabled()) {
				BasicJPAService.LOG.trace(" = '{}'", ToStringBuilder.reflectionToString(entity));
			}
		} else {
			BasicJPAService.LOG.debug("couldn't find {} with uuid '{}' in the database", className, uuid);
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

		BasicJPAService.LOG.debug("try to delete {} with uuid '{}' from the database", className, uuid);

		prepareObjectForRemoval(updateObject);

		entityManager.remove(updateObject);

		BasicJPAService.LOG.debug("deleted {} with uuid '{}' from the database", className, uuid);
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
	protected PROXYPOJOCLASS getObject(final POJOCLASS object, final EntityManager entityManager, final String transactionType,
			final PersistenceType persistenceType)
			throws DMPPersistenceException {

		final PROXYPOJOCLASS proxyUpdateObject;

		// second condition is for new object creation on dummy id
		if (object.getUuid() == null) {

			// TODO: we don't need to generate new objects here, or?

			BasicJPAService.LOG.debug("{} id is null, will create a new {}", className, className);

			proxyUpdateObject = createObjectInternal(object, entityManager, transactionType, persistenceType);
		} else {

			proxyUpdateObject = getObjectInternal(object, entityManager);
		}

		return proxyUpdateObject;
	}

	protected Optional<POJOCLASS> persistObject(final POJOCLASS object, final EntityManager entityManager, final PersistenceType persistenceType)
			throws DMPPersistenceException {

		BasicJPAService.LOG.debug("try to create new {}", className);

		// http://blog.xebia.com/2009/03/23/jpa-implementation-patterns-saving-detached-entities/
		// "Because of the way merging works, we can also do this if we are unsure whether the object has been already persisted."
		// pro persist: http://stackoverflow.com/questions/1069992/jpa-entitymanager-why-use-persist-over-merge

		final Optional<POJOCLASS> optionalPersistentObject;

		switch (persistenceType) {

			case Persist:

				entityManager.persist(object);

				optionalPersistentObject = Optional.empty();

				break;
			case Merge:

				optionalPersistentObject = Optional.of(entityManager.merge(object));

				break;
			default:

				final String message = String.format("cannot persist %s; found unknown persistence type '%s'", className, persistenceType.toString());

				LOG.error(message);

				throw new DMPPersistenceException(message);
		}

		BasicJPAService.LOG.debug("created new {} with id '{}'", className, object.getUuid());

		return optionalPersistentObject;
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

			BasicJPAService.LOG.error("something went wrong while {} object creation", proxyClassName, e);

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

			BasicJPAService.LOG.error("something went wrong while {} object creation", proxyClassName, e);

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
	public POJOCLASS createNewObject(final String uuid) throws DMPPersistenceException {

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

			BasicJPAService.LOG.error("something went wrong while {}object creation", className, e);

			throw new DMPPersistenceException(e.getMessage());
		}

		return object;
	}

	protected POJOCLASS determinePersistentObject(final POJOCLASS newObject, final Optional<POJOCLASS> optionalPersistentObject) {

		final POJOCLASS persistentObject;

		if (optionalPersistentObject.isPresent()) {

			persistentObject = optionalPersistentObject.get();
		} else {

			persistentObject = newObject;
		}

		return persistentObject;
	}
}
