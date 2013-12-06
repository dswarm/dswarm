package de.avgl.dmp.persistence.service.job;

import javax.persistence.EntityManager;

import com.google.inject.Inject;
import com.google.inject.Provider;

import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.job.Filter;
import de.avgl.dmp.persistence.service.BasicDMPJPAService;

/**
 * 
 * @author tgaengler
 *
 */
public class FilterService extends BasicDMPJPAService<Filter> {

	@Inject
	public FilterService(final Provider<EntityManager> entityManagerProvider) {

		super(Filter.class, entityManagerProvider);
	}

	@Override
	protected void prepareObjectForRemoval(final Filter object) {

	}

	@Override
	protected void updateObjectInternal(final Filter object, final Filter updateObject, final EntityManager entityManager)
			throws DMPPersistenceException {

		final String expression = object.getExpression();

		updateObject.setExpression(expression);
		
		super.updateObjectInternal(object, updateObject, entityManager);
	}

}
