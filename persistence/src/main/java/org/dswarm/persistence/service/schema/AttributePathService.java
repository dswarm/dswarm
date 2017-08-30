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
package org.dswarm.persistence.service.schema;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.persist.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.proxy.RetrievalType;
import org.dswarm.persistence.model.schema.Attribute;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.proxy.ProxyAttributePath;
import org.dswarm.persistence.service.BasicJPAService;
import org.dswarm.persistence.service.PersistenceType;
import org.dswarm.persistence.service.UUIDService;

/**
 * A persistence service for {@link AttributePath}s.
 *
 * @author tgaengler
 */
public class AttributePathService extends BasicJPAService<ProxyAttributePath, AttributePath> {

	private static final Logger LOG = LoggerFactory.getLogger(AttributePathService.class);

	/**
	 * Creates a new attribute path persistence service with the given entity manager provider.
	 *
	 * @param entityManagerProvider an entity manager provider
	 */
	@Inject
	public AttributePathService(final Provider<EntityManager> entityManagerProvider) {

		super(AttributePath.class, ProxyAttributePath.class, entityManagerProvider);
	}

	/**
	 * Creates an attribute path with the given ordered list of attributes or returns the existing one from the DB.
	 *
	 * @param attributes an ordered list of attributes
	 * @return the persisted or matched attribute path from DB
	 * @throws DMPPersistenceException
	 */
	@Transactional(rollbackOn = Exception.class)
	public ProxyAttributePath createOrGetObjectTransactional(final List<Attribute> attributes) throws DMPPersistenceException {

		final AttributePath tempAttributePath = new AttributePath(attributes);

		return createObject(tempAttributePath);
	}

	/**
	 * Tries to retrieve an attribute path object for the given ordered list of attribute paths
	 * TODO: check, when this method was called in the code
	 *
	 * @param attributePathJSONArrayString
	 * @return
	 * @throws DMPPersistenceException
	 */
	public AttributePath getObjectViaAttributePathJSON(final String attributePathJSONArrayString) throws DMPPersistenceException {

		final EntityManager entityManager = acquire();

		return getObject(attributePathJSONArrayString, entityManager);
	}

	@Override
	public ProxyAttributePath createObject(final AttributePath object) throws DMPPersistenceException {

		final EntityManager em = acquire();

		// TODO: change persistent type as required or make it a parameter of the method
		return createObjectInternal(object, em, NON_TRANSACTIONAL_TRANSACTION_TYPE, PersistenceType.Merge);
	}

	@Override
	protected ProxyAttributePath createObjectInternal(final AttributePath object, final EntityManager entityManager, final String transactionalType,
			final PersistenceType persistenceType)
			throws DMPPersistenceException {

		final AttributePath existingObject = getObject(object.getAttributePathAsJSONObjectString(), entityManager);

		final AttributePath newObject;

		if (null == existingObject) {

			final AttributePath tempAttributePath = mergeAttributesIntoEntityManager(object, entityManager);

			final Optional<AttributePath> optionalPersistentObject = persistObject(tempAttributePath, entityManager, persistenceType);
			newObject = determinePersistentObject(tempAttributePath, optionalPersistentObject);

			return new ProxyAttributePath(newObject);
		} else {

			AttributePathService.LOG.debug("attribute path with path '" + object.toAttributePath()
					+ "' exists already in the database. Will return the existing object, instead of creating a new one");

			return new ProxyAttributePath(existingObject, RetrievalType.RETRIEVED);
		}
	}

	@Override
	protected ProxyAttributePath getObjectInternal(final AttributePath object, final EntityManager entityManager) throws DMPPersistenceException {

		// 1. try to receive attribute path by id (as usual)

		final ProxyAttributePath tempProxyAttributePath = super.getObjectInternal(object, entityManager);

		// 2. compare attribute path (strings) of the retrieved attribute path with the current attribute path to determine,
		// whether has anything changed inbetween

		if (object != null && tempProxyAttributePath != null && tempProxyAttributePath.getObject() != null && object.toAttributePath() != null
				&& !object.toAttributePath().equals(tempProxyAttributePath.getObject().toAttributePath())) {

			final AttributePath tempAttributePath = getObject(object.getAttributePathAsJSONObjectString(), entityManager);

			final AttributePath currentObject;
			final RetrievalType type;

			if (tempAttributePath != null) {

				// attribute path was modified to another existing one => attribute path object changes to the retrieved attribute
				// path object

				currentObject = tempAttributePath;
				type = RetrievalType.RETRIEVED;
			} else {

				// attribute path was modified to a non-existing attribute path => attribute path object will be kept

				currentObject = object;
				type = RetrievalType.UPDATED;
			}

			return createNewProxyObject(currentObject, type);
		}

		return tempProxyAttributePath;
	}

	private AttributePath getObject(final String attributePath, final EntityManager entityManager) throws DMPPersistenceException {

		final AttributePath object;

		final String queryString = "SELECT o FROM " + className + " o WHERE o.attributePath = '" + attributePath + "'";
		final TypedQuery<AttributePath> query = entityManager.createQuery(queryString, clasz);

		try {

			object = query.getSingleResult();
		} catch (final NoResultException e) {

			AttributePathService.LOG.debug("couldn't find " + className + " for attribute path JSON string '" + attributePath + "' in the database");

			return null;
		} catch (final NonUniqueResultException e) {

			throw new DMPPersistenceException("there is more than one " + className + " in the database for attribute path JSON string '"
					+ attributePath + "'");
		}

		return object;
	}

	private AttributePath mergeAttributesIntoEntityManager(final AttributePath object, final EntityManager entityManager) {

		final String uuid;

		if (object.getUuid() != null) {

			uuid = object.getUuid();
		} else {

			uuid = UUIDService.getUUID(AttributePath.class.getSimpleName());
		}

		final AttributePath tempAttributePath = new AttributePath(uuid);

		final List<Attribute> attributes = object.getAttributePath();

		if (attributes != null) {

			for (final Attribute attribute : attributes) {

				final Attribute managedAttribute = entityManager.merge(attribute);
				tempAttributePath.addAttribute(managedAttribute);
			}
		}

		return tempAttributePath;
	}

	@Override
	protected void prepareObjectForRemoval(final AttributePath object) {

		// should clear the relationship to the attributes
		object.setAttributePath(null);
	}

	@Override
	public void updateObjectInternal(final AttributePath object, final AttributePath updateObject)
			throws DMPPersistenceException {

		final List<Attribute> attributes = object.getAttributePath();

		updateObject.setAttributePath(attributes);
	}

	public List<AttributePath> getAttributePathsWithPath(final String attributePathJSONArrayString) {

		final EntityManager entityManager = acquire(true);

		final String queryString =
				"SELECT o FROM " + AttributePath.class.getName() + " o WHERE o.attributePath = '" + attributePathJSONArrayString + "'";

		final TypedQuery<AttributePath> query = entityManager.createQuery(queryString, AttributePath.class);

		return query.getResultList();
	}

}
