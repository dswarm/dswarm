package de.avgl.dmp.persistence.service.schema;

import javax.persistence.EntityManager;

import com.google.inject.Inject;
import com.google.inject.Provider;

import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.schema.Attribute;
import de.avgl.dmp.persistence.service.AdvancedJPAService;

/**
 * 
 * @author tgaengler
 *
 */
public class AttributeService extends AdvancedJPAService<Attribute> {

	@Inject
	public AttributeService(final Provider<EntityManager> entityManagerProvider) {

		super(Attribute.class, entityManagerProvider);
	}

	@Override
	protected void prepareObjectForRemoval(final Attribute object) {

		// should clear the relationship to the attribute paths
		// object.setAttributePaths(null);
	}

	@Override
	protected void updateObjectInternal(final Attribute object, final Attribute updateObject, final EntityManager entityManager)
			throws DMPPersistenceException {

		final String name = object.getName();
		// final Set<AttributePath> attributePaths = object.getAttributePaths();

		updateObject.setName(name);
		// updateObject.setAttributePaths(attributePaths);
	}

}
