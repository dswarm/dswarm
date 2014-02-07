package de.avgl.dmp.persistence.service.schema;

import javax.persistence.EntityManager;

import com.google.inject.Inject;
import com.google.inject.Provider;

import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.job.Filter;
import de.avgl.dmp.persistence.model.schema.MappingAttributePathInstance;
import de.avgl.dmp.persistence.model.schema.proxy.ProxyMappingAttributePathInstance;

/**
 * A persistence service for {@link MappingAttributePathInstance}s.
 * 
 * @author tgaengler
 */
public class MappingAttributePathInstanceService extends
		AttributePathInstanceService<ProxyMappingAttributePathInstance, MappingAttributePathInstance> {

	/**
	 * Creates a new mapping attribute path instance persistence service with the given entity manager provider.
	 * 
	 * @param entityManagerProvider an entity manager provider
	 */
	@Inject
	public MappingAttributePathInstanceService(final Provider<EntityManager> entityManagerProvider) {

		super(MappingAttributePathInstance.class, ProxyMappingAttributePathInstance.class, entityManagerProvider);
	}

	/**
	 * {@inheritDoc}<br/>
	 * Clears the relationship to the input attribute paths, output attribute path, input filter and output filter.
	 */
	@Override
	protected void prepareObjectForRemoval(final MappingAttributePathInstance object) {

		super.prepareObjectForRemoval(object);

		// should clear the relationship to the filter
		object.setFilter(null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void updateObjectInternal(final MappingAttributePathInstance object, final MappingAttributePathInstance updateObject,
			final EntityManager entityManager) throws DMPPersistenceException {

		super.updateObjectInternal(object, updateObject, entityManager);

		final Filter filter = object.getFilter();

		updateObject.setFilter(filter);
	}
}
