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
 * 
 * @author tgaengler
 *
 */
public class AttributePathService extends BasicIDJPAService<AttributePath> {

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
