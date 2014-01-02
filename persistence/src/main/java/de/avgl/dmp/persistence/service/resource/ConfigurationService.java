package de.avgl.dmp.persistence.service.resource;

import java.util.Set;

import javax.persistence.EntityManager;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.Provider;

import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.Resource;
import de.avgl.dmp.persistence.service.ExtendedBasicDMPJPAService;

/**
 * A persistence service for {@link Configuration}s.
 * 
 * @author tgaengler
 */
public class ConfigurationService extends ExtendedBasicDMPJPAService<Configuration> {

	/**
	 * Creates a new configuration persistence service with the given entity manager provider.
	 * 
	 * @param entityManagerProvider an entity manager provider
	 */
	@Inject
	public ConfigurationService(final Provider<EntityManager> entityManagerProvider) {

		super(Configuration.class, entityManagerProvider);
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

		final Set<Resource> resources = object.getResources();
		final ObjectNode parameters = object.getParameters();

		updateObject.setResources(resources);
		updateObject.setParameters(parameters);
	}

}
