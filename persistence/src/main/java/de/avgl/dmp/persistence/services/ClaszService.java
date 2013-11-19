package de.avgl.dmp.persistence.services;

import javax.persistence.EntityManager;

import com.google.inject.Inject;
import com.google.inject.Provider;

import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.job.Clasz;

public class ClaszService extends AdvancedJPAService<Clasz> {

	@Inject
	public ClaszService(final Provider<EntityManager> entityManagerProvider) {

		super(Clasz.class, entityManagerProvider);
	}

	@Override
	protected void prepareObjectForRemoval(final Clasz object) {

	}

	@Override
	protected void updateObjectInternal(final Clasz object, final Clasz updateObject, final EntityManager entityManager)
			throws DMPPersistenceException {

		final String name = object.getName();

		updateObject.setName(name);
	}

}
