package de.avgl.dmp.persistence.service;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import com.google.inject.Provider;
import com.google.inject.persist.Transactional;

import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.BasicDMPJPAObject;
import de.avgl.dmp.persistence.model.AdvancedDMPJPAObject;
import de.avgl.dmp.persistence.model.schema.AttributePath;

/**
 * A generic persistence service implementation for {@link AdvancedDMPJPAObject}s, i.e., where the identifier will be set on object
 * creation.
 * 
 * @author tgaengler
 * @param <POJOCLASS> a concrete POJO class
 */
public abstract class AdvancedDMPJPAService<POJOCLASS extends BasicDMPJPAObject> extends BasicDMPJPAService<POJOCLASS> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(AdvancedDMPJPAService.class);

	/**
	 * Creates a new persistence service for the given concrete POJO class and the entity manager provider.
	 * 
	 * @param clasz a concrete POJO class
	 * @param entityManagerProvider an entity manager provider
	 */
	protected AdvancedDMPJPAService(final Class<POJOCLASS> clasz, final Provider<EntityManager> entityManagerProvider) {

		super(clasz, entityManagerProvider);
	}

	/**
	 * Create and persist an object of the specific class with the given identifier.<br>
	 * 
	 * @param id the identifier of the object
	 * @return the persisted object of the specific class
	 */
	@Transactional(rollbackOn = DMPPersistenceException.class)
	public POJOCLASS createObjectTransactional(final String id) throws DMPPersistenceException {

		return createObject(id);
	}
	
	/**
	 * Create and persist an object of the specific class with the given identifier.<br>
	 * 
	 * @param id the identifier of the object
	 * @return the persisted object of the specific class
	 */
	public POJOCLASS createObject(final String uri) throws DMPPersistenceException {

		final POJOCLASS existingObject = getObjectByUri(uri);

		final POJOCLASS object;

		if (null == existingObject) {

			object = createNewObject(uri);

			persistObject(object);
		} else {

			AdvancedDMPJPAService.LOG.debug(className + " with uri '" + uri
					+ "' exists already in the database, will return the existing object, instead creating a new one");

			object = existingObject;
		}

		return object;
	}
	
	public POJOCLASS getObjectByUri(final String uri) {
		
		final EntityManager entityManager = acquire();

		return getObjectByUri(uri, entityManager);
	}
	
	private POJOCLASS getObjectByUri(final String uri, final EntityManager entityManager) {
		
		final POJOCLASS object;
		
		final String queryString = "from " + className + " where uri = '" + uri + "'";
		final TypedQuery<POJOCLASS> query = entityManager.createQuery(queryString, clasz);

		try {
			
			object = query.getSingleResult();
		} catch (final NoResultException e) {
			
			// TODO: maybe log something here
			
			return null;
		}
		
		return object;
	}

	/**
	 * Creates a new object of the concrete POJO class with the given identifier.
	 * 
	 * @param id an object identifier
	 * @return the new instance of the concrete POJO class
	 * @throws DMPPersistenceException if something went wrong at object creation
	 */
	private POJOCLASS createNewObject(final String id) throws DMPPersistenceException {

		final POJOCLASS object;

		Constructor<POJOCLASS> constructor = null;

		try {
			constructor = clasz.getConstructor(String.class);
		} catch (final SecurityException | NoSuchMethodException e1) {

			e1.printStackTrace();

			throw new DMPPersistenceException(e1.getMessage());
		}

		if (null == constructor) {

			throw new DMPPersistenceException("couldn't find constructor to instantiate new '" + className + "' with id '" + id + "'");
		}

		try {

			object = constructor.newInstance(id);
		} catch (final InstantiationException | InvocationTargetException | IllegalArgumentException | IllegalAccessException e) {

			AdvancedDMPJPAService.LOG.error("something went wrong while " + className + "object creation", e);

			throw new DMPPersistenceException(e.getMessage());
		}

		return object;
	}
}
