package de.avgl.dmp.persistence.service.schema;

import java.util.Set;

import javax.persistence.EntityManager;

import com.google.inject.Inject;
import com.google.inject.Provider;

import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.schema.AttributePath;
import de.avgl.dmp.persistence.model.schema.Clasz;
import de.avgl.dmp.persistence.model.schema.Schema;
import de.avgl.dmp.persistence.service.BasicDMPJPAService;

/**
 * A persistence service for {@link Schema}s.
 * 
 * @author tgaengler
 *
 */
public class SchemaService extends BasicDMPJPAService<Schema> {

	/**
	 * Creates a new schema persistence service with the given entity manager provider.
	 * 
	 * @param entityManagerProvider an entity manager provider
	 */
	@Inject
	public SchemaService(final Provider<EntityManager> entityManagerProvider) {

		super(Schema.class, entityManagerProvider);
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

		updateObject.setAttributePaths(attributePaths);
		updateObject.setRecordClass(recordClass);

		super.updateObjectInternal(object, updateObject, entityManager);
	}

}
