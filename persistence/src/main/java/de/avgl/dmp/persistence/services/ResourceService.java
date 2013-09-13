package de.avgl.dmp.persistence.services;

import java.util.Set;

import javax.persistence.EntityManager;

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
		// TODO Auto-generated method stub

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
}
