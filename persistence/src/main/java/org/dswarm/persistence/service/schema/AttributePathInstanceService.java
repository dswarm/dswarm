package org.dswarm.persistence.service.schema;

import javax.persistence.EntityManager;

import com.google.inject.Provider;

import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.AttributePathInstance;
import org.dswarm.persistence.model.schema.proxy.ProxyAttributePathInstance;
import org.dswarm.persistence.service.BasicDMPJPAService;

/**
 * A generic persistence service for {@link AttributePathInstance}s.
 *
 * @author tgaengler
 * @param <ATTRIBUTEPATHIMPL> a concrete {@link AttributePathInstance} implementation
 */
public abstract class AttributePathInstanceService<PROXYATTRIBUTEPATHIMPL extends ProxyAttributePathInstance<ATTRIBUTEPATHIMPL>, ATTRIBUTEPATHIMPL extends AttributePathInstance>
		extends BasicDMPJPAService<PROXYATTRIBUTEPATHIMPL, ATTRIBUTEPATHIMPL> {

	/**
	 * Creates a new persistence service for the given concrete {@link AttributePathInstance} implementation and the entity
	 * manager provider.
	 *
	 * @param clasz a concrete {@link AttributePathInstance} implementation
	 * @param entityManagerProvider an entity manager provider
	 */
	protected AttributePathInstanceService(final Class<ATTRIBUTEPATHIMPL> clasz, final Class<PROXYATTRIBUTEPATHIMPL> proxyClasz,
			final Provider<EntityManager> entityManagerProvider) {

		super(clasz, proxyClasz, entityManagerProvider);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void prepareObjectForRemoval(final ATTRIBUTEPATHIMPL object) {

		object.setAttributePath(null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void updateObjectInternal(final ATTRIBUTEPATHIMPL object, final ATTRIBUTEPATHIMPL updateObject, final EntityManager entityManager)
			throws DMPPersistenceException {

		final AttributePath attributePath = object.getAttributePath();

		updateObject.setAttributePath(attributePath);

		super.updateObjectInternal(object, updateObject, entityManager);
	}

}
