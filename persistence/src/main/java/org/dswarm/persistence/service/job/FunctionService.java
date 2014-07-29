package org.dswarm.persistence.service.job;

import javax.persistence.EntityManager;

import com.google.inject.Inject;
import com.google.inject.Provider;

import org.dswarm.persistence.model.job.Function;
import org.dswarm.persistence.model.job.proxy.ProxyFunction;

/**
 * A persistence service for {@link Function}s.
 * 
 * @author tgaengler
 */
public class FunctionService extends BasicFunctionService<ProxyFunction, Function> {

	/**
	 * Creates a new function persistence service with the given entity manager provider.
	 * 
	 * @param entityManagerProvider an entity manager provider
	 */
	@Inject
	public FunctionService(final Provider<EntityManager> entityManagerProvider) {

		super(Function.class, ProxyFunction.class, entityManagerProvider);
	}
}
