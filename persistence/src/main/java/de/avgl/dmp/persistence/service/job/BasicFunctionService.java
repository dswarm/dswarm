package de.avgl.dmp.persistence.service.job;

import java.util.LinkedList;

import javax.persistence.EntityManager;

import com.google.inject.Provider;

import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.job.Function;
import de.avgl.dmp.persistence.service.ExtendedBasicDMPJPAService;

/**
 * 
 * @author tgaengler
 *
 * @param <FUNCTIONIMPL>
 */
public abstract class BasicFunctionService<FUNCTIONIMPL extends Function> extends ExtendedBasicDMPJPAService<FUNCTIONIMPL> {

	public BasicFunctionService(final Class<FUNCTIONIMPL> clasz, final Provider<EntityManager> entityManagerProvider) {

		super(clasz, entityManagerProvider);
	}

	@Override
	protected void prepareObjectForRemoval(final FUNCTIONIMPL object) {

	}

	@Override
	protected void updateObjectInternal(final FUNCTIONIMPL object, final FUNCTIONIMPL updateObject, final EntityManager entityManager)
			throws DMPPersistenceException {

		final LinkedList<String> parameters = object.getParameters();

		updateObject.setParameters(parameters);

		super.updateObjectInternal(object, updateObject, entityManager);
	}

}
