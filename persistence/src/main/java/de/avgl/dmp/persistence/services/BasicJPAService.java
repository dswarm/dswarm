package de.avgl.dmp.persistence.services;

import java.util.Collections;
import java.util.List;

import javax.persistence.CacheRetrieveMode;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.google.inject.Provider;
import com.google.inject.persist.Transactional;

import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.job.DMPObject;

public abstract class BasicJPAService<POJOCLASS extends DMPObject<POJOCLASSIDTYPE>, POJOCLASSIDTYPE> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(BasicJPAService.class);

	protected final Class<POJOCLASS>				clasz;
	protected final String							className;

	protected final Provider<EntityManager>			entityManagerProvider;

	public BasicJPAService(final Class<POJOCLASS> clasz, final Provider<EntityManager> entityManagerProvider) {

		this.clasz = clasz;
		this.className = clasz.getSimpleName();

		this.entityManagerProvider = entityManagerProvider;
	}

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
	 * Updates a given instance of the specific class and writes this object persistent to the DB afterwards.<br>
	 * Created by: tgaengler
	 * 
	 * @param object the to be updated instance of the specific class
	 */
	@Transactional(rollbackOn = DMPPersistenceException.class)
	public POJOCLASS updateObjectTransactional(final POJOCLASS object) throws DMPPersistenceException {

		final EntityManager entityManager = entityManagerProvider.get();

		final POJOCLASS updateObject = getObject(object, entityManager);

		BasicJPAService.LOG.debug("try to update " + className + " with id '" + object.getId() + "' transactional");

		updateObjectInternal(object, updateObject, entityManager);

		BasicJPAService.LOG.debug("passed internal object update");

		entityManager.merge(updateObject);

		BasicJPAService.LOG.debug("updated " + className + " with id '" + object.getId() + "' transactional");

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

		final EntityManager entityManager = entityManagerProvider.get();
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

		final EntityManager entityManager = entityManagerProvider.get();
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

		final EntityManager entityManager = entityManagerProvider.get();
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

		final EntityManager entityManager = entityManagerProvider.get();
		final POJOCLASS updateObject = entityManager.find(clasz, id);

		BasicJPAService.LOG.debug("try to delete " + className + " with id '" + id + "' from the database");

		prepareObjectForRemoval(updateObject);

		entityManager.remove(updateObject);

		BasicJPAService.LOG.debug("deleted " + className + " with id '" + id + "' from the database");
	}

	protected abstract void prepareObjectForRemoval(final POJOCLASS object);

	/**
	 * Tries to retrieve a POJOCLASS instance or will create a new one, if the identifier is null.<br>
	 * Created by: tgaengler
	 * 
	 * @return the requested POJOCLASS instance fresh from the DB or a new POJOCLASS instance
	 * @throws DMPPersistenceException
	 */
	protected POJOCLASS getObject(final POJOCLASS object, final EntityManager entityManager) throws DMPPersistenceException {

		final POJOCLASS updateObject;

		if (object.getId() == null) {

			BasicJPAService.LOG.debug(className + " id is null, will create a new " + className);

			updateObject = this.createObject();

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

	protected void persistObject(final POJOCLASS object) {

		final EntityManager entityManager = entityManagerProvider.get();

		BasicJPAService.LOG.debug("try to create new " + className);

		entityManager.persist(object);

		BasicJPAService.LOG.debug("created new " + className + " with id '" + object.getId() + "'");
	}

	protected POJOCLASS createNewObject() throws DMPPersistenceException {

		final POJOCLASS object;

		try {

			object = clasz.newInstance();
		} catch (final InstantiationException e) {

			e.printStackTrace();

			throw new DMPPersistenceException(e.getMessage());
		} catch (final IllegalAccessException e) {

			e.printStackTrace();

			throw new DMPPersistenceException(e.getMessage());
		}

		return object;
	}
}
