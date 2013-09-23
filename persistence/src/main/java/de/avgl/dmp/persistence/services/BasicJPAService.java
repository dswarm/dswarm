package de.avgl.dmp.persistence.services;

import java.util.Collections;
import java.util.List;

import javax.persistence.CacheRetrieveMode;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.builder.ToStringBuilder;

import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.DMPJPAObject;
import de.avgl.dmp.persistence.services.utils.JPAUtil;

public abstract class BasicJPAService<POJOCLASS extends DMPJPAObject> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(BasicJPAService.class);

	private final Class<POJOCLASS>					clasz;
	private final String							className;

	public BasicJPAService(final Class<POJOCLASS> clasz) {

		this.clasz = clasz;
		this.className = clasz.getSimpleName();
	}

	public Class<POJOCLASS> getClasz() {

		return clasz;
	}

	/**
	 * Create and persist an object of the specific class.<br>
	 * 
	 * @return the persisted object of the specific class
	 */
	public POJOCLASS createObject() throws DMPPersistenceException {

		final POJOCLASS object;

		try {

			object = clasz.newInstance();
		} catch (InstantiationException e) {

			e.printStackTrace();

			throw new DMPPersistenceException(e.getMessage());
		} catch (IllegalAccessException e) {

			e.printStackTrace();

			throw new DMPPersistenceException(e.getMessage());
		}

		final EntityManager entityManager = JPAUtil.getEntityManager();

		LOG.debug("try to create new " + className);

		JPAUtil.beginNewTransaction(entityManager);

		entityManager.persist(object);

		JPAUtil.endTransaction(entityManager);

		LOG.debug("created new " + className + " with id '" + object.getId() + "'");

		return object;
	}

	/**
	 * Updates a given instance of the specific class and writes this object persistent to the DB afterwards.<br>
	 * Created by: tgaengler
	 * 
	 * @param object the to be updated instance of the specific class
	 */
	public POJOCLASS updateObjectTransactional(final POJOCLASS object) throws DMPPersistenceException {

		final EntityManager entityManager = JPAUtil.getEntityManager();
		final POJOCLASS updateObject = getObject(object, entityManager);

		LOG.debug("try to update " + className + " with id '" + object.getId() + "' transactional");

		JPAUtil.beginNewTransaction(entityManager);
		updateObjectInternal(object, updateObject, entityManager);
		entityManager.merge(updateObject);
		JPAUtil.endTransaction(entityManager);

		LOG.debug("updated " + className + " with id '" + object.getId() + "' transactional");

		return updateObject;
	}

	/**
	 * Updates a given instance of the specific class without writing this object persistent right now, i.e., the process that
	 * calls this method needs to ensure that the update will be written persistent.<br>
	 * Created by: tgaengler
	 * 
	 * @param object the to be updated instance of the specific class
	 */
	public POJOCLASS updateObject(final POJOCLASS object) throws DMPPersistenceException {

		final EntityManager entityManager = JPAUtil.getEntityManager();
		final POJOCLASS updateObject = getObject(object, entityManager);

		LOG.debug("try to update " + className + " with id '" + object.getId() + "' non-transactional");

		updateObjectInternal(object, updateObject, entityManager);

		LOG.debug("updated " + className + " with id '" + object.getId() + "' non-transactional");

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

		final EntityManager entityManager = JPAUtil.getEntityManager();

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
	public POJOCLASS getObject(final Long id) {

		final EntityManager entityManager = JPAUtil.getEntityManager();

		LOG.debug("try to find " + className + " with id '" + id + "' in the database");

		final POJOCLASS entity = entityManager.find(clasz, id,
				Collections.<String, Object> singletonMap("javax.persistence.cache.retrieveMode", CacheRetrieveMode.BYPASS));

		if (entity != null) {

			LOG.debug("found " + className + " with id '" + id + "' in the database = '" + ToStringBuilder.reflectionToString(entity) + "'");

			// refresh entity to ensure that the most current, persistent version will be utilised
			entityManager.refresh(entity);
		} else {

			LOG.debug("couldn't find " + className + " with id '" + id + "' in the database");
		}

		return entity;
	}

	/**
	 * Deletes an instance of the specific class permanently from the DB by a given identifier.<br>
	 * Created by: tgaengler
	 * 
	 * @param id the identifier of the to be deleted instance of the specific class
	 */
	public void deleteObject(final Long id) {

		final EntityManager entityManager = JPAUtil.getEntityManager();
		final POJOCLASS updateObject = entityManager.find(clasz, id);

		LOG.debug("try to delete " + className + " with id '" + id + "' from the database");

		JPAUtil.beginNewTransaction(entityManager);

		prepareObjectForRemoval(updateObject);

		entityManager.remove(updateObject);

		JPAUtil.endTransaction(entityManager);

		LOG.debug("deleted " + className + " with id '" + id + "' from the database");
	}

	protected abstract void prepareObjectForRemoval(final POJOCLASS object);

	/**
	 * Tries to retrieve a {@link EDObject} instance or will create a new one, if the identifier is null.<br>
	 * Created by: tgaengler
	 * 
	 * @param edObject the {@link EDObject} instance that should be fetched from the DB
	 * @param entityManager the {@link EntityManager} instance that should handle this retrieval process
	 * @return the requested {@link EDObject} instance fresh from the DB or a new {@link EDObject} instance
	 * @throws Exception
	 */
	private POJOCLASS getObject(final POJOCLASS object, final EntityManager entityManager) throws DMPPersistenceException {

		final POJOCLASS updateObject;

		if (object.getId() == null) {

			updateObject = this.createObject();

		} else {

			updateObject = entityManager.find(clasz, object.getId(),
					Collections.<String, Object> singletonMap("javax.persistence.cache.retrieveMode", CacheRetrieveMode.BYPASS));
		}

		return updateObject;
	}
}
