package org.dswarm.persistence.service.resource;

import java.util.Set;

import javax.persistence.EntityManager;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.Provider;

import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.Resource;
import org.dswarm.persistence.model.resource.ResourceType;
import org.dswarm.persistence.model.resource.proxy.ProxyResource;
import org.dswarm.persistence.service.ExtendedBasicDMPJPAService;

/**
 * A persistence service for {@link Resource}s.
 *
 * @author tgaengler
 */
public class ResourceService extends ExtendedBasicDMPJPAService<ProxyResource, Resource> {

	/**
	 * Creates a new resource persistence service with the given entity manager provider.
	 *
	 * @param entityManagerProvider an entity manager provider
	 */
	@Inject
	public ResourceService(final Provider<EntityManager> entityManagerProvider) {

		super(Resource.class, ProxyResource.class, entityManagerProvider);
	}

	/**
	 * {@inheritDoc}<br/>
	 * Clears the relationship to configurations.
	 */
	@Override
	protected void prepareObjectForRemoval(final Resource object) {

		object.setConfigurations(null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void updateObjectInternal(final Resource object, final Resource updateObject, final EntityManager entityManager)
			throws DMPPersistenceException {

		super.updateObjectInternal(object, updateObject, entityManager);

		final Set<Configuration> configurations = object.getConfigurations();

		updateObject.setConfigurations(configurations);

		final ResourceType type = object.getType();

		updateObject.setType(type);

		final ObjectNode attributes = object.getAttributes();

		updateObject.setAttributes(attributes);
	}

	@Override
	public Resource getObject(final Long id) {

		return super.getObject(id);
	}
}
