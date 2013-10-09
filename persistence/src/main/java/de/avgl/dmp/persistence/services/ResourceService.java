package de.avgl.dmp.persistence.services;

import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.databind.node.ObjectNode;

import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.Resource;
import de.avgl.dmp.persistence.model.resource.ResourceType;

public class ResourceService extends BasicJPAService<Resource> {

	public ResourceService() {

		super(Resource.class);
	}

	@Override
	protected void prepareObjectForRemoval(final Resource object) {

		object.setConfigurations(null);
	}

	@Override
	protected void updateObjectInternal(final Resource object, final Resource updateObject, final EntityManager entityManager)
			throws DMPPersistenceException {

		final Set<Configuration> configurations = object.getConfigurations();

		updateObject.setConfigurations(configurations);

		final String description = object.getDescription();

		updateObject.setDescription(description);

		final ResourceType type = object.getType();

		updateObject.setType(type);

		final ObjectNode attributes = object.getAttributes();

		updateObject.setAttributes(attributes);

		if (object.getName() != null) {

			updateObject.setName(object.getName());
		}
	}

	@Override
	public Resource getObject(final Long id) {

		final ConfigurationService cS = new ConfigurationService();



		System.out.println("all objects: " + ToStringBuilder.reflectionToString(cS.getObjects()));

		return super.getObject(id);
	}
}
