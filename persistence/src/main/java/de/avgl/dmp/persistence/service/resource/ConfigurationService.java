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
 * @author tgaengler
 */
public class ConfigurationService extends ExtendedBasicDMPJPAService<Configuration> {

	@Inject
	public ConfigurationService(Provider<EntityManager> entityManagerProvider) {

		super(Configuration.class, entityManagerProvider);
	}

	@Override
	protected void prepareObjectForRemoval(final Configuration object) {

		// should clear the relationship to the resource
		object.setResources(null);
	}

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
