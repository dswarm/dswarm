package de.avgl.dmp.persistence.service;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.persistence.EntityManager;

import com.google.inject.Provider;
import com.google.inject.persist.Transactional;

import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.BasicDMPObject;

/**
 * A generic persistence service implementation for {@link BasicDMPObject}s, i.e., where the identifier will be set on object
 * creation.
 * 
 * @author tgaengler
 * @param <POJOCLASS> a concrete POJO class
 */
public abstract class AdvancedJPAService<POJOCLASS extends BasicDMPObject> extends BasicJPAService<POJOCLASS, String> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(AdvancedJPAService.class);

	/**
	 * Creates a new persistence service for the given concrete POJO class and the entity manager provider.
	 * 
	 * @param clasz a concrete POJO class
	 * @param entityManagerProvider an entity manager provider
	 */
	protected AdvancedJPAService(final Class<POJOCLASS> clasz, final Provider<EntityManager> entityManagerProvider) {

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
	public POJOCLASS createObject(final String id) throws DMPPersistenceException {

		final POJOCLASS existingObject = getObject(id);

		final POJOCLASS object;

		if (null == existingObject) {

			object = createNewObject(id);

			persistObject(object);
		} else {

			AdvancedJPAService.LOG.debug(className + " with id '" + id
					+ "' exists already in the database, will return the existing object, instead creating a new one");

			object = existingObject;
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

			AdvancedJPAService.LOG.error("something went wrong while " + className + "object creation", e);

			throw new DMPPersistenceException(e.getMessage());
		}

		return object;
	}
}
