package de.avgl.dmp.persistence.service.schema;

import javax.persistence.EntityManager;

import com.google.inject.Inject;
import com.google.inject.Provider;

import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.schema.Attribute;
import de.avgl.dmp.persistence.model.schema.proxy.ProxyAttribute;
import de.avgl.dmp.persistence.service.AdvancedDMPJPAService;

/**
 * A persistence service for {@link Attribute}s.
 * 
 * @author tgaengler
 */
public class AttributeService extends AdvancedDMPJPAService<ProxyAttribute, Attribute> {

	/**
	 * Creates a new attribute persistence service with the given entity manager provider.
	 * 
	 * @param entityManagerProvider an entity manager provider
	 */
	@Inject
	public AttributeService(final Provider<EntityManager> entityManagerProvider) {

		super(Attribute.class, ProxyAttribute.class, entityManagerProvider);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void prepareObjectForRemoval(final Attribute object) {

		// should clear the relationship to the attribute paths
		// object.setAttributePaths(null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void updateObjectInternal(final Attribute object, final Attribute updateObject, final EntityManager entityManager)
			throws DMPPersistenceException {

		final String name = object.getName();
		// final Set<AttributePath> attributePaths = object.getAttributePaths();

		updateObject.setName(name);
		// updateObject.setAttributePaths(attributePaths);
	}

}
