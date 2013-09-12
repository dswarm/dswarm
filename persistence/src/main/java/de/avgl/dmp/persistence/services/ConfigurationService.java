package de.avgl.dmp.persistence.services;

import de.avgl.dmp.persistence.model.resource.Configuration;

public class ConfigurationService extends BasicJPAService<Configuration> {

	public ConfigurationService() {

		super(Configuration.class);
	}

	@Override
	protected void prepareObjectForRemoval(final Configuration object) {
		// TODO Auto-generated method stub

	}

}
