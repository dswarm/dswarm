package de.avgl.dmp.persistence.service.schema;

import java.util.LinkedList;

import javax.persistence.EntityManager;

import com.google.inject.Inject;
import com.google.inject.Provider;

import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.schema.Attribute;
import de.avgl.dmp.persistence.model.schema.AttributePath;
import de.avgl.dmp.persistence.service.BasicIDJPAService;

/**
 * A persistence service for {@link AttributePath}s.
 * 
 * @author tgaengler
 *
 */
public class AttributePathService extends BasicIDJPAService<AttributePath> {

	/**
	 * Creates a new attribute path persistence service with the given entity manager provider.
	 * 
	 * @param entityManagerProvider an entity manager provider
	 */
	@Inject
	public AttributePathService(final Provider<EntityManager> entityManagerProvider) {

		super(AttributePath.class, entityManagerProvider);
	}

	@Override
	protected void prepareObjectForRemoval(final AttributePath object) {

		// should clear the relationship to the attributes
		object.setAttributePath(null);
	}

	@Override
	protected void updateObjectInternal(final AttributePath object, final AttributePath updateObject, final EntityManager entityManager)
			throws DMPPersistenceException {
		
		final LinkedList<Attribute> attributes = object.getAttributePath();
		
		updateObject.setAttributePath(attributes);
	}

}
