package de.avgl.dmp.persistence.service.schema;

import javax.persistence.EntityManager;

import com.google.inject.Provider;

import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.schema.AttributePath;
import de.avgl.dmp.persistence.model.schema.AttributePathInstance;
import de.avgl.dmp.persistence.model.schema.proxy.ProxyAttributePathInstance;
import de.avgl.dmp.persistence.service.BasicDMPJPAService;

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
