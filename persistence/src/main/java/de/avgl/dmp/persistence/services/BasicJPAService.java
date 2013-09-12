package de.avgl.dmp.persistence.services;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.services.utils.JPAUtil;

public abstract class BasicJPAService<POJOCLASS> {

	private final Class<POJOCLASS>	clasz;
	private final String			className;

	public BasicJPAService(final Class<POJOCLASS> clasz) {

		this.clasz = clasz;
		this.className = clasz.getSimpleName();
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

		JPAUtil.beginNewTransaction(entityManager);
		entityManager.persist(object);
		JPAUtil.endTransaction(entityManager);

		return object;
	}

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
	public POJOCLASS getObject(final String id) {

		final EntityManager entityManager = JPAUtil.getEntityManager();

		final POJOCLASS entity = entityManager.find(clasz, id);

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

		JPAUtil.beginNewTransaction(entityManager);

		prepareObjectForRemoval(updateObject);

		entityManager.remove(updateObject);

		JPAUtil.endTransaction(entityManager);
	}

	protected abstract void prepareObjectForRemoval(final POJOCLASS object);

}
