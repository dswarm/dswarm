package org.dswarm.persistence.service.schema;

import java.util.Set;

import javax.persistence.EntityManager;

import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.Clasz;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.model.schema.proxy.ProxySchema;
import org.dswarm.persistence.service.BasicDMPJPAService;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * A persistence service for {@link Schema}s.
 * 
 * @author tgaengler
 */
public class SchemaService extends BasicDMPJPAService<ProxySchema, Schema> {

	/**
	 * Creates a new schema persistence service with the given entity manager provider.
	 * 
	 * @param entityManagerProvider an entity manager provider
	 */
	@Inject
	public SchemaService(final Provider<EntityManager> entityManagerProvider) {

		super(Schema.class, ProxySchema.class, entityManagerProvider);
	}

	/**
	 * {@inheritDoc}<br>
	 * Clear the relationship to the attribute paths + record class.
	 */
	@Override
	protected void prepareObjectForRemoval(final Schema object) {

		// should clear the relationship to the attribute paths + record class
		object.setAttributePaths(null);
		object.setRecordClass(null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void updateObjectInternal(final Schema object, final Schema updateObject, final EntityManager entityManager)
			throws DMPPersistenceException {

		final Set<AttributePath> attributePaths = object.getAttributePaths();
		final Clasz recordClass = object.getRecordClass();

		// if (attributePaths != null) {
		//
		// for (final AttributePath attributePath : attributePaths) {
		//
		// final AttributePath managedAttributePath = entityManager.merge(attributePath);
		// updateObject.addAttributePath(managedAttributePath);
		// }
		//
		// } else {
		//
		updateObject.setAttributePaths(attributePaths);
		// }

		updateObject.setRecordClass(recordClass);

		super.updateObjectInternal(object, updateObject, entityManager);
	}

}
