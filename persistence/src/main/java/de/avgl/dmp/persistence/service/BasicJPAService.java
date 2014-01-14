package de.avgl.dmp.persistence.service;

import java.util.Collections;
import java.util.List;

import javax.persistence.CacheRetrieveMode;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.google.inject.Provider;
import com.google.inject.persist.Transactional;

import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.DMPObject;

/**
 * A generic persistence service implementation, whose concrete implementations can be derived with a given implementation of
 * {@link DMPObject} and the related identifier type. This service delivers basic persistence layer functionality to create a new
 * object, update an existing one, retrieve existing ones or delete existing objects.
 * 
 * @author tgaengler
 * @param <POJOCLASS> the concrete POJO class
 * @param <POJOCLASSIDTYPE> the identifier type of the concrete POJO class
 */
public abstract class BasicJPAService<POJOCLASS extends DMPObject<POJOCLASSIDTYPE>, POJOCLASSIDTYPE> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(BasicJPAService.class);

	/**
	 * The concrete POJO class of this persistence service.
	 */
	final Class<POJOCLASS>							clasz;

	/**
	 * The name of the concrete POJO class this persistence service.
	 */
	final String									className;

	/**
	 * The entity manager provider (powered by Guice).
	 */
	private final Provider<EntityManager>			entityManagerProvider;

	/**
	 * Creates a new persistence service for the given concrete POJO class and the entity manager provider.
	 * 
	 * @param clasz a concrete POJO class
	 * @param entityManagerProvider an entity manager provider
	 */
	BasicJPAService(final Class<POJOCLASS> clasz, final Provider<EntityManager> entityManagerProvider) {

		this.clasz = clasz;
		this.className = clasz.getSimpleName();

		this.entityManagerProvider = entityManagerProvider;
	}

	/**
	 * Acquire a new or reused EntityManager with its cache cleared
	 * 
	 * @return the EntityManager
	 */
	private EntityManager acquire() {
		return acquire(true);
	}

	/**
	 * Acquire a new or reused EntityManager
	 * 
	 * @param clear true if the EM's cache should be cleared
	 * @return the EntityManager
	 */
	private EntityManager acquire(final boolean clear) {

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
	 * Create and persist an object of the specific class.<br>
	 * 
	 * @return the persisted object of the specific class
	 */
	@Transactional(rollbackOn = DMPPersistenceException.class)
	public POJOCLASS createObject() throws DMPPersistenceException {

		final POJOCLASS object = createNewObject();

		persistObject(object);

		return object;
	}

	/**
	 * Creates (if it doesn't exist before) or updates a given instance of the specific class and writes this object persistent to
	 * the DB afterwards.<br>
	 * Created by: tgaengler
	 * 
	 * @param object the to be updated instance of the specific class
	 */
	@Transactional(rollbackOn = Exception.class)
	public POJOCLASS updateObjectTransactional(final POJOCLASS object) throws DMPPersistenceException {

		final EntityManager entityManager = acquire(false);

		final POJOCLASS updateObject = getObject(object, entityManager);

		BasicJPAService.LOG.debug("try to update " + className + " with id '" + object.getId() + "' transactional");

		updateObjectInternal(object, updateObject, entityManager);

		entityManager.merge(updateObject);

		BasicJPAService.LOG.debug("updated " + className + " with id '" + object.getId() + "' transactional");

		if (updateObject != null) {

			BasicJPAService.LOG.debug("updated " + className + " with id '" + updateObject.getId() + "' in the database = '"
					+ ToStringBuilder.reflectionToString(updateObject) + "'");
		} else {

			BasicJPAService.LOG.debug("couldn't updated " + className + " with id '" + object.getId() + "' in the database");
		}

		return updateObject;
	}

	/**
	 * Updates a given instance of the specific class without writing this object persistent right now, i.e., the process that
	 * calls this method needs to ensure that the update will be written persistent.<br>
	 * Created by: tgaengler
	 * 
	 * @param object the to be updated instance of the specific class
	 */
	// @Transactional(rollbackOn = DMPPersistenceException.class)
	public POJOCLASS updateObject(final POJOCLASS object) throws DMPPersistenceException {

		final EntityManager entityManager = acquire(false);
		final POJOCLASS updateObject = getObject(object, entityManager);

		BasicJPAService.LOG.debug("try to update " + className + " with id '" + object.getId() + "' non-transactional");

		updateObjectInternal(object, updateObject, entityManager);

		BasicJPAService.LOG.debug("updated " + className + " with id '" + object.getId() + "' non-transactional");

		return updateObject;
	}

	/**
	 * The internal update method for the specific class that will be called from {@link BasicJPAService#updateObject(POJOCLASS)}
	 * and {@link BasicJPAService#updateObjectTransactional(POJOCLASS)}, i.e., this method includes the real update logic of this
	 * service.<br>
	 * Created by: tgaengler
	 * 
	 * @param object the instance of the specific class with the update data
	 * @param updateObject the to be updated instance of the specific class
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
		final TypedQuery<POJOCLASS> query = entityManager.createQuery("from " + className, clasz);

		return query.getResultList();
	}

	/**
	 * Generic 'exist instance for identifier of a specific class' method.<br>
	 * Created by: tgaengler
	 * 
	 * @param id the idenfier of the requested instance of a specific class
	 * @return the instance for the identifier of the specific class
	 */
	public POJOCLASS getObject(final POJOCLASSIDTYPE id) {

		final EntityManager entityManager = acquire();

		BasicJPAService.LOG.debug("try to find " + className + " with id '" + id + "' in the database");

		final POJOCLASS entity = entityManager.find(clasz, id,
				Collections.<String, Object> singletonMap("javax.persistence.cache.retrieveMode", CacheRetrieveMode.BYPASS));

		if (entity != null) {

			BasicJPAService.LOG.debug("found " + className + " with id '" + id + "' in the database = '" + ToStringBuilder.reflectionToString(entity)
					+ "'");

		} else {

			BasicJPAService.LOG.debug("couldn't find " + className + " with id '" + id + "' in the database");
		}

		return entity;
	}

	/**
	 * Deletes an instance of the specific class permanently from the DB by a given identifier.<br>
	 * Created by: tgaengler
	 * 
	 * @param id the identifier of the to be deleted instance of the specific class
	 */
	@Transactional(rollbackOn = DMPPersistenceException.class)
	public void deleteObject(final POJOCLASSIDTYPE id) {

		final EntityManager entityManager = acquire(false);
		final POJOCLASS updateObject = entityManager.find(clasz, id);

		BasicJPAService.LOG.debug("try to delete " + className + " with id '" + id + "' from the database");

		prepareObjectForRemoval(updateObject);

		entityManager.remove(updateObject);

		BasicJPAService.LOG.debug("deleted " + className + " with id '" + id + "' from the database");
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
	private POJOCLASS getObject(final POJOCLASS object, final EntityManager entityManager) throws DMPPersistenceException {

		final POJOCLASS updateObject;

		// second condition is for new object creation on dummy id
		if (object.getId() == null) {

			BasicJPAService.LOG.debug(className + " id is null, will create a new " + className);

			updateObject = this.createObject();
		} else if (Long.class.isInstance(object.getId()) && ((Long) object.getId()).longValue() < 0) {
			
			BasicJPAService.LOG.debug(className + " id is a dummy id, will create a new " + className);

			updateObject = this.createObject();
			
			// TODO: cache all ids of objects that have dummy id?
		} else {

			updateObject = entityManager.find(clasz, object.getId(),
					Collections.<String, Object> singletonMap("javax.persistence.cache.retrieveMode", CacheRetrieveMode.BYPASS));

			if (updateObject != null) {

				BasicJPAService.LOG.debug("found " + className + " with id '" + updateObject.getId() + "' in the database = '"
						+ ToStringBuilder.reflectionToString(updateObject) + "'");

				// entityManager.refresh(updateObject);
			} else {

				BasicJPAService.LOG.debug("couldn't find " + className + " with id '" + object.getId() + "' in the database");
			}
		}

		return updateObject;
	}

	/**
	 * Persists the given instance of the concrete POJO class.
	 * 
	 * @param object an instance of the concrete POJO class
	 */
	protected void persistObject(final POJOCLASS object) {

		final EntityManager entityManager = acquire(false);

		BasicJPAService.LOG.debug("try to create new " + className);

		entityManager.persist(object);

		BasicJPAService.LOG.debug("created new " + className + " with id '" + object.getId() + "'");
	}

	/**
	 * Creates a new object of the concrete POJO class.
	 * 
	 * @return a new instance of the concrete POJO class
	 * @throws DMPPersistenceException if something went wrong.
	 */
	private POJOCLASS createNewObject() throws DMPPersistenceException {

		final POJOCLASS object;

		try {

			object = clasz.newInstance();
		} catch (final InstantiationException | IllegalAccessException e) {

			BasicJPAService.LOG.error("something went wrong while " + className + "object creation", e);

			throw new DMPPersistenceException(e.getMessage());
		}

		return object;
	}
}
