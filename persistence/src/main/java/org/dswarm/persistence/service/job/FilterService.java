package org.dswarm.persistence.service.job;

import javax.persistence.EntityManager;

import com.google.inject.Inject;
import com.google.inject.Provider;

import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.job.Filter;
import org.dswarm.persistence.model.job.proxy.ProxyFilter;
import org.dswarm.persistence.service.BasicDMPJPAService;

/**
 * A persistence service for {@link Filter}s.
 * 
 * @author tgaengler
 */
public class FilterService extends BasicDMPJPAService<ProxyFilter, Filter> {

	/**
	 * Creates a new filter persistence service with the given entity manager provider.
	 * 
	 * @param entityManagerProvider an entity manager provider
	 */
	@Inject
	public FilterService(final Provider<EntityManager> entityManagerProvider) {

		super(Filter.class, ProxyFilter.class, entityManagerProvider);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void prepareObjectForRemoval(final Filter object) {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void updateObjectInternal(final Filter object, final Filter updateObject, final EntityManager entityManager)
			throws DMPPersistenceException {

		final String expression = object.getExpression();

		updateObject.setExpression(expression);

		super.updateObjectInternal(object, updateObject, entityManager);
	}

}
