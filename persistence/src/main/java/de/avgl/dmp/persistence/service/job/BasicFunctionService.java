package de.avgl.dmp.persistence.service.job;

import java.util.LinkedList;

import javax.persistence.EntityManager;

import com.google.inject.Provider;

import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.job.Function;
import de.avgl.dmp.persistence.model.job.proxy.ProxyBasicFunction;
import de.avgl.dmp.persistence.service.ExtendedBasicDMPJPAService;

/**
 * A generic persistence service for {@link Function}s.
 * 
 * @author tgaengler
 * @param <FUNCTIONIMPL> a concrete {@link Function} implementation
 */
public abstract class BasicFunctionService<PROXYFUNCTIONIMPL extends ProxyBasicFunction<FUNCTIONIMPL>, FUNCTIONIMPL extends Function> extends
		ExtendedBasicDMPJPAService<PROXYFUNCTIONIMPL, FUNCTIONIMPL> {

	/**
	 * Creates a new persistence service for the given concrete {@link Function} implementation and the entity manager provider.
	 * 
	 * @param clasz a concrete {@link Function} implementation
	 * @param entityManagerProvider an entity manager provider
	 */
	protected BasicFunctionService(final Class<FUNCTIONIMPL> clasz, final Class<PROXYFUNCTIONIMPL> proxyClasz,
			final Provider<EntityManager> entityManagerProvider) {

		super(clasz, proxyClasz, entityManagerProvider);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void prepareObjectForRemoval(final FUNCTIONIMPL object) {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void updateObjectInternal(final FUNCTIONIMPL object, final FUNCTIONIMPL updateObject, final EntityManager entityManager)
			throws DMPPersistenceException {

		final LinkedList<String> parameters = object.getParameters();

		updateObject.setParameters(parameters);

		updateObject.setFunctionDescription(object.getFunctionDescription());

		super.updateObjectInternal(object, updateObject, entityManager);
	}

}
