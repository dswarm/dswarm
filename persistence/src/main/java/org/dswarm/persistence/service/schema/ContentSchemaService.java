package org.dswarm.persistence.service.schema;

import java.util.LinkedList;

import javax.persistence.EntityManager;

import com.google.inject.Inject;
import com.google.inject.Provider;

import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.ContentSchema;
import org.dswarm.persistence.model.schema.proxy.ProxyContentSchema;
import org.dswarm.persistence.service.BasicDMPJPAService;

/**
 * A persistence service for {@link org.dswarm.persistence.model.schema.ContentSchema}s.
 * 
 * @author tgaengler
 */
public class ContentSchemaService extends BasicDMPJPAService<ProxyContentSchema, ContentSchema> {

	/**
	 * Creates a new content schema persistence service with the given entity manager provider.
	 * 
	 * @param entityManagerProvider an entity manager provider
	 */
	@Inject
	public ContentSchemaService(final Provider<EntityManager> entityManagerProvider) {

		super(ContentSchema.class, ProxyContentSchema.class, entityManagerProvider);
	}

	/**
	 * {@inheritDoc}<br>
	 * Clear the relationship to the key attribute paths + value attribute path.
	 */
	@Override
	protected void prepareObjectForRemoval(final ContentSchema object) {

		// should clear the relationship to the key attribute paths + value attribute path
		object.setKeyAttributePaths(null);
		object.setValueAttributePath(null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void updateObjectInternal(final ContentSchema object, final ContentSchema updateObject, final EntityManager entityManager)
			throws DMPPersistenceException {

		final LinkedList<AttributePath> keyAttributePaths = object.getKeyAttributePaths();
		final AttributePath valueAttributePath = object.getValueAttributePath();

		updateObject.setKeyAttributePaths(keyAttributePaths);

		updateObject.setValueAttributePath(valueAttributePath);

		super.updateObjectInternal(object, updateObject, entityManager);
	}

}
