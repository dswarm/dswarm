package de.avgl.dmp.persistence.services;

import javax.persistence.EntityManager;

import com.google.inject.Provider;

import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.job.BasicDMPJPAObject;

/**
 * 
 * @author tgaengler
 *
 * @param <POJOCLASS>
 */
public abstract class BasicDMPJPAService<POJOCLASS extends BasicDMPJPAObject> extends BasicIDJPAService<POJOCLASS> {

	public BasicDMPJPAService(final Class<POJOCLASS> clasz, final Provider<EntityManager> entityManagerProvider) {

		super(clasz, entityManagerProvider);
	}

	@Override
	protected void updateObjectInternal(final POJOCLASS object, final POJOCLASS updateObject, final EntityManager entityManager)
			throws DMPPersistenceException {

		final String name = object.getName();

		updateObject.setName(name);
	}
}
