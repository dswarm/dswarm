package de.avgl.dmp.persistence.services;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.persistence.EntityManager;

import com.google.inject.Provider;
import com.google.inject.persist.Transactional;

import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.job.BasicDMPObject;

/**
 * 
 * @author tgaengler
 *
 * @param <POJOCLASS>
 */
public abstract class AdvancedJPAService<POJOCLASS extends BasicDMPObject> extends BasicJPAService<POJOCLASS, String> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(AdvancedJPAService.class);

	public AdvancedJPAService(final Class<POJOCLASS> clasz, final Provider<EntityManager> entityManagerProvider) {

		super(clasz, entityManagerProvider);
	}

	/**
	 * Create and persist an object of the specific class.<br>
	 * 
	 * @return the persisted object of the specific class
	 */
	@Transactional(rollbackOn = DMPPersistenceException.class)
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

	protected POJOCLASS createNewObject(final String id) throws DMPPersistenceException {

		final POJOCLASS object;

		Constructor<POJOCLASS> constructor = null;

		try {
			constructor = clasz.getConstructor(String.class);
		} catch (final SecurityException e1) {

			e1.printStackTrace();

			throw new DMPPersistenceException(e1.getMessage());
		} catch (final NoSuchMethodException e1) {
			e1.printStackTrace();

			throw new DMPPersistenceException(e1.getMessage());
		}

		if (null == constructor) {

			throw new DMPPersistenceException("couldn't find constructor to instantiate new '" + className + "' with id '" + id + "'");
		}

		try {

			object = constructor.newInstance(id);
		} catch (final InstantiationException e) {

			e.printStackTrace();

			throw new DMPPersistenceException(e.getMessage());
		} catch (final IllegalAccessException e) {

			e.printStackTrace();

			throw new DMPPersistenceException(e.getMessage());
		} catch (final IllegalArgumentException e) {

			e.printStackTrace();

			throw new DMPPersistenceException(e.getMessage());
		} catch (final InvocationTargetException e) {

			e.printStackTrace();

			throw new DMPPersistenceException(e.getMessage());
		}

		return object;
	}
}
