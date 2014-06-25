package org.dswarm.persistence.service.resource;

import javax.persistence.EntityManager;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.Provider;

import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.proxy.ProxyConfiguration;
import org.dswarm.persistence.service.ExtendedBasicDMPJPAService;

/**
 * A persistence service for {@link Configuration}s.
 *
 * @author tgaengler
 */
public class ConfigurationService extends ExtendedBasicDMPJPAService<ProxyConfiguration, Configuration> {

	/**
	 * Creates a new configuration persistence service with the given entity manager provider.
	 *
	 * @param entityManagerProvider an entity manager provider
	 */
	@Inject
	public ConfigurationService(final Provider<EntityManager> entityManagerProvider) {

		super(Configuration.class, ProxyConfiguration.class, entityManagerProvider);
	}

	/**
	 * {@inheritDoc}<br/>
	 * Clears the relationship to resources.
	 */
	@Override
	protected void prepareObjectForRemoval(final Configuration object) {

		// should clear the relationship to the resources
		object.setResources(null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void updateObjectInternal(final Configuration object, final Configuration updateObject, final EntityManager entityManager)
			throws DMPPersistenceException {

		super.updateObjectInternal(object, updateObject, entityManager);

		// TODO: disable resource updating for now (until resource id ref resolution is implemented)

		// final Set<Resource> resources = object.getResources();
		final ObjectNode parameters = object.getParameters();

		// updateObject.setResources(resources);
		updateObject.setParameters(parameters);
	}

}
