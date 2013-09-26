package de.avgl.dmp.persistence.services;

import javax.persistence.EntityManager;

import com.fasterxml.jackson.databind.node.ObjectNode;

import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.resource.Configuration;

public class ConfigurationService extends BasicJPAService<Configuration> {

	public ConfigurationService() {

		super(Configuration.class);
	}

	@Override
	protected void prepareObjectForRemoval(final Configuration object) {

		// should clear the relationship to the resource
		object.setResources(null);
	}

	@Override
	protected void updateObjectInternal(final Configuration object, final Configuration updateObject, final EntityManager entityManager) throws DMPPersistenceException {
		
		final ObjectNode parameters = object.getParameters();
		
		updateObject.setParameters(parameters);
	}

}
