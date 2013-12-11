package de.avgl.dmp.persistence.service;

import javax.persistence.EntityManager;

import com.google.inject.Provider;

import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.ExtendedBasicDMPJPAObject;

/**
 *
 * @author tgaengler
 *
 * @param <POJOCLASS>
 */
public abstract class ExtendedBasicDMPJPAService<POJOCLASS extends ExtendedBasicDMPJPAObject> extends BasicDMPJPAService<POJOCLASS> {

	protected ExtendedBasicDMPJPAService(final Class<POJOCLASS> clasz, final Provider<EntityManager> entityManagerProvider) {

		super(clasz, entityManagerProvider);
	}

	@Override
	protected void updateObjectInternal(final POJOCLASS object, final POJOCLASS updateObject, final EntityManager entityManager)
			throws DMPPersistenceException {

		final String description = object.getDescription();

		updateObject.setDescription(description);

		super.updateObjectInternal(object, updateObject, entityManager);
	}
}
