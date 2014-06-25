package org.dswarm.persistence.service.schema;

import javax.persistence.EntityManager;

import com.google.inject.Inject;
import com.google.inject.Provider;

import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.schema.Clasz;
import org.dswarm.persistence.model.schema.proxy.ProxyClasz;
import org.dswarm.persistence.service.AdvancedDMPJPAService;

/**
 * A persistence service for {@link Clasz}es.
 *
 * @author tgaengler
 */
public class ClaszService extends AdvancedDMPJPAService<ProxyClasz, Clasz> {

	/**
	 * Creates a new class persistence service with the given entity manager provider.
	 *
	 * @param entityManagerProvider an entity manager provider
	 */
	@Inject
	public ClaszService(final Provider<EntityManager> entityManagerProvider) {

		super(Clasz.class, ProxyClasz.class, entityManagerProvider);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void prepareObjectForRemoval(final Clasz object) {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void updateObjectInternal(final Clasz object, final Clasz updateObject, final EntityManager entityManager)
			throws DMPPersistenceException {

		final String name = object.getName();

		updateObject.setName(name);
	}

}
