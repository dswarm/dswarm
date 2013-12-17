package de.avgl.dmp.persistence.service.resource;

import java.util.Set;

import javax.persistence.EntityManager;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.Provider;

import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.Resource;
import de.avgl.dmp.persistence.model.resource.ResourceType;
import de.avgl.dmp.persistence.service.ExtendedBasicDMPJPAService;

/**
 * @author tgaengler
 */
public class ResourceService extends ExtendedBasicDMPJPAService<Resource> {

	private final Provider<ConfigurationService>	configurationServiceProvider;

	@Inject
	public ResourceService(final Provider<EntityManager> entityManagerProvider, final Provider<ConfigurationService> configurationServiceProvider) {

		super(Resource.class, entityManagerProvider);
		this.configurationServiceProvider = configurationServiceProvider;
	}

	@Override
	protected void prepareObjectForRemoval(final Resource object) {

		object.setConfigurations(null);
	}

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

		// final ConfigurationService cS = configurationServiceProvider.get();
		//
		//
		//
		// System.out.println("all objects: " + ToStringBuilder.reflectionToString(cS.getObjects()));

		return super.getObject(id);
	}
}
