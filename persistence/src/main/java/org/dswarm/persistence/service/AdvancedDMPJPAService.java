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
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;

import com.google.inject.Provider;
import com.google.inject.persist.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.AdvancedDMPJPAObject;
import org.dswarm.persistence.model.proxy.ProxyAdvancedDMPJPAObject;
import org.dswarm.persistence.model.proxy.RetrievalType;

/**
 * A generic persistence service implementation for {@link AdvancedDMPJPAObject}s, i.e., where the identifier will be set on
 * object creation.
 *
 * @param <POJOCLASS> a concrete POJO class
 * @author tgaengler
 */
public abstract class AdvancedDMPJPAService<PROXYPOJOCLASS extends ProxyAdvancedDMPJPAObject<POJOCLASS>, POJOCLASS extends AdvancedDMPJPAObject>
		extends BasicDMPJPAService<PROXYPOJOCLASS, POJOCLASS> {

	private static final Logger LOG = LoggerFactory.getLogger(AdvancedDMPJPAService.class);

	/**
	 * Creates a new persistence service for the given concrete POJO class and the entity manager provider.
	 *
	 * @param clasz                 a concrete POJO class
	 * @param entityManagerProvider an entity manager provider
	 */
	protected AdvancedDMPJPAService(final Class<POJOCLASS> clasz, final Class<PROXYPOJOCLASS> proxyClasz,
			final Provider<EntityManager> entityManagerProvider) {

		super(clasz, proxyClasz, entityManagerProvider);
	}

	/**
	 * Create and persist an object of the specific class with the given object.<br>
	 *
	 * @param object the object that should be utilised for checking or creating a new persistent object
	 * @return the persisted object of the specific class
	 */
	@Transactional(rollbackOn = DMPPersistenceException.class)
	public PROXYPOJOCLASS createOrGetObjectTransactional(final POJOCLASS object) throws DMPPersistenceException {

		return createOrGetObject(object, TRANSACTIONAL_TRANSACTION_TYPE);
	}

	/**
	 * Create and persist an object of the specific class with the given identifier.<br>
	 *
	 * @param uri the object identifier
	 * @return the persisted object of the specific class
	 */
	@Transactional(rollbackOn = DMPPersistenceException.class)
	public PROXYPOJOCLASS createOrGetObjectTransactional(final String uri) throws DMPPersistenceException {

		final POJOCLASS object = createNewObject(uri);

		return createOrGetObject(object, TRANSACTIONAL_TRANSACTION_TYPE);
	}

	/**
	 * Create and persist an object of the specific class with the given object.<br>
	 *
	 * @param object          the original object
	 * @param transactionType
	 * @return the persisted object of the specific class
	 * @throws DMPPersistenceException
	 */
	public PROXYPOJOCLASS createOrGetObject(final POJOCLASS object, final String transactionType) throws DMPPersistenceException {

		final EntityManager em = acquire();

		return createOrGetObjectInternal(object, em, transactionType);
	}

	protected PROXYPOJOCLASS createOrGetObjectInternal(final POJOCLASS object, final EntityManager entityManager, final String transactionType)
			throws DMPPersistenceException {

		final String uri = object.getUri();

		final POJOCLASS existingObject = getObjectByUri(uri, entityManager);

		final POJOCLASS newObject;

		if (null == existingObject) {

			final POJOCLASS existingUUIDObject;

			if (object.getUuid() != null) {

				// check, whether another object with this uuid exists in the DB or not
				existingUUIDObject = getObject(object.getUuid());
			} else {

				existingUUIDObject = null;
			}

			if (existingUUIDObject == null) {

				// we can create the new object with this uuid
				newObject = createNewObject(object.getUuid(), object.getUri());
			} else {

				newObject = createNewObject(object.getUri());
			}

			updateObjectInternal(object, newObject);

			// TODO: change persistent type as required or make it a parameter of the method
			final Optional<POJOCLASS> optionalPersistentObject = persistObject(newObject, entityManager, PersistenceType.Merge);
			final POJOCLASS persistentObject = determinePersistentObject(newObject, optionalPersistentObject);

			return createNewProxyObject(persistentObject);
		} else {

			AdvancedDMPJPAService.LOG.debug(className + " with uri '" + uri
					+ "' exists already in the database, will return the existing object, instead creating a new one");

			return createNewProxyObject(existingObject, RetrievalType.RETRIEVED);
		}
	}

	@Override
	protected PROXYPOJOCLASS createObjectInternal(final POJOCLASS object, final EntityManager entityManager, final String transactionType,
			final PersistenceType persistenceType)
			throws DMPPersistenceException {

		final String uri = object.getUri();

		final POJOCLASS existingObject = getObjectByUri(uri, entityManager);

		final POJOCLASS newObject;

		if (null == existingObject) {

			newObject = createNewObject(object.getUuid(), uri);

			updateObjectInternal(object, newObject);

			final Optional<POJOCLASS> optionalPersistentObject = persistObject(newObject, entityManager, persistenceType);
			final POJOCLASS persistentObject = determinePersistentObject(newObject, optionalPersistentObject);

			return createNewProxyObject(persistentObject);
		} else {

			AdvancedDMPJPAService.LOG.debug(className + " with uri '" + uri
					+ "' exists already in the database, will return the existing object, instead creating a new one");

			return createNewProxyObject(existingObject, RetrievalType.RETRIEVED);
		}
	}

	@Override
	protected void prepareObjectForRemoval(final POJOCLASS object) {
		// TODO Auto-generated method stub

	}

	public POJOCLASS getObjectByUri(final String uri) throws DMPPersistenceException {

		final EntityManager entityManager = acquire();

		return getObjectByUri(uri, entityManager);
	}

	@Override
	protected PROXYPOJOCLASS getObjectInternal(final POJOCLASS object, final EntityManager entityManager) throws DMPPersistenceException {

		// 1. try to receive object by id (as usual)

		final PROXYPOJOCLASS tempProxyObject = super.getObjectInternal(object, entityManager);

		// 2. compare uri of the retrieved object with the uri of the current object to determine,
		// whether has anything changed in between

		if ((object != null && object.getUri() != null && !object.getUri().trim().equals("")) && tempProxyObject != null
				&& tempProxyObject.getObject() != null && !object.getUri().equals(tempProxyObject.getObject().getUri())) {

			final POJOCLASS currentObject;
			final RetrievalType type;

			// retrieve object by uri first

			final POJOCLASS tempObject = getObjectByUri(object.getUri(), entityManager);

			if (tempObject != null) {

				// retrieved existing entity for this uri => object will be changed to the retrieved one

				currentObject = tempObject;
				type = RetrievalType.RETRIEVED;
			} else {

				// uri was modified to a non-existing one => a new entity with the given uri will be created

				currentObject = createNewObject(object.getUri());
				type = RetrievalType.CREATED;
			}

			return createNewProxyObject(currentObject, type);
		}

		return tempProxyObject;
	}

	private POJOCLASS getObjectByUri(final String uri, final EntityManager entityManager) throws DMPPersistenceException {

		final POJOCLASS object;

		final String queryString = "SELECT o FROM " + className + " o WHERE o.uri = '" + uri + "'";
		final TypedQuery<POJOCLASS> query = entityManager.createQuery(queryString, clasz);

		try {

			object = query.getSingleResult();
		} catch (final NoResultException e) {

			AdvancedDMPJPAService.LOG.debug("couldn't find " + className + " for uri '" + uri + "' in the database");

			return null;
		} catch (final NonUniqueResultException e) {

			throw new DMPPersistenceException("there is more than one " + className + " in the database for uri '" + uri + "'");
		}

		return object;
	}

	/**
	 * Creates a new object of the concrete POJO class with the given identifier.
	 *
	 * @param uri an object identifier
	 * @return the new instance of the concrete POJO class
	 * @throws DMPPersistenceException if something went wrong at object creation
	 */
	public POJOCLASS createNewObject(final String uri) throws DMPPersistenceException {

		final POJOCLASS object;

		Constructor<POJOCLASS> constructor = null;

		try {
			constructor = clasz.getConstructor(String.class, String.class);
		} catch (final SecurityException | NoSuchMethodException e1) {

			throw new DMPPersistenceException(e1.getMessage());
		}

		if (null == constructor) {

			throw new DMPPersistenceException("couldn't find constructor to instantiate new '" + className + "' with uri '" + uri + "'");
		}

		final String uuid = UUIDService.getUUID(className);

		try {

			object = constructor.newInstance(uuid, uri);
		} catch (final InstantiationException | InvocationTargetException | IllegalArgumentException | IllegalAccessException e) {

			AdvancedDMPJPAService.LOG.error("something went wrong while " + className + "object creation", e);

			throw new DMPPersistenceException(e.getMessage());
		}

		return object;
	}

	/**
	 * Creates a new object of the concrete POJO class with the given identifier.
	 *
	 * @param uuid the object uuid
	 * @param uri  an object identifier
	 * @return the new instance of the concrete POJO class
	 * @throws DMPPersistenceException if something went wrong at object creation
	 */
	private POJOCLASS createNewObject(final String uuid, final String uri) throws DMPPersistenceException {

		final POJOCLASS object;

		Constructor<POJOCLASS> constructor = null;

		try {
			constructor = clasz.getConstructor(String.class, String.class);
		} catch (final SecurityException | NoSuchMethodException e1) {

			throw new DMPPersistenceException(e1.getMessage());
		}

		if (null == constructor) {

			throw new DMPPersistenceException("couldn't find constructor to instantiate new '" + className + "' with uri '" + uri + "'");
		}

		final String finalUuid;

		if (uuid != null) {

			finalUuid = uuid;
		} else {

			finalUuid = UUIDService.getUUID(className);
		}

		try {

			object = constructor.newInstance(finalUuid, uri);
		} catch (final InstantiationException | InvocationTargetException | IllegalArgumentException | IllegalAccessException e) {

			AdvancedDMPJPAService.LOG.error("something went wrong while " + className + "object creation", e);

			throw new DMPPersistenceException(e.getMessage());
		}

		return object;
	}
}
