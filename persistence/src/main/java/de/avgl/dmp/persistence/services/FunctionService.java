package de.avgl.dmp.persistence.services;

import java.util.LinkedList;

import javax.persistence.EntityManager;

import com.google.inject.Inject;
import com.google.inject.Provider;

import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.job.Function;

public class FunctionService extends BasicDMPJPAService<Function> {

	@Inject
	public FunctionService(final Provider<EntityManager> entityManagerProvider) {

		super(Function.class, entityManagerProvider);
	}

	@Override
	protected void prepareObjectForRemoval(final Function object) {

	}

	@Override
	protected void updateObjectInternal(final Function object, final Function updateObject, final EntityManager entityManager)
			throws DMPPersistenceException {

		final String description = object.getDescription();
		final LinkedList<String> parameters = object.getParameters();

		updateObject.setDescription(description);
		updateObject.setParameters(parameters);

		super.updateObjectInternal(object, updateObject, entityManager);
	}

}
