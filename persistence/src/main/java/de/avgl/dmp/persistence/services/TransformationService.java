package de.avgl.dmp.persistence.services;

import javax.persistence.EntityManager;

import com.google.inject.Inject;
import com.google.inject.Provider;

import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.job.Transformation;

public class TransformationService extends BasicFunctionService<Transformation> {

	@Inject
	public TransformationService(final Provider<EntityManager> entityManagerProvider) {

		super(Transformation.class, entityManagerProvider);
	}

	@Override
	protected void prepareObjectForRemoval(final Transformation object) {

		// TODO
	}

	@Override
	protected void updateObjectInternal(final Transformation object, final Transformation updateObject, final EntityManager entityManager)
			throws DMPPersistenceException {

		// TODO

		super.updateObjectInternal(object, updateObject, entityManager);
	}

}
