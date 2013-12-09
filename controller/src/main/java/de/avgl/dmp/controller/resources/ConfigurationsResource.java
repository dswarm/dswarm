package de.avgl.dmp.controller.resources;

import javax.inject.Inject;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Provider;
import com.google.inject.servlet.RequestScoped;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import de.avgl.dmp.controller.DMPControllerException;
import de.avgl.dmp.controller.status.DMPStatus;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.service.resource.ConfigurationService;

@RequestScoped
@Api(value = "/configurations", description = "Operations about configurations")
@Path("configurations")
public class ConfigurationsResource extends BasicResource<ConfigurationService, Configuration, Long> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(ConfigurationsResource.class);

	@Inject
	public ConfigurationsResource(final Provider<ConfigurationService> configurationServiceProviderArg, final ObjectMapper objectMapper,
			final DMPStatus dmpStatus) {

		super(Configuration.class, configurationServiceProviderArg, objectMapper, dmpStatus);
	}

	@ApiOperation(value = "get the configuration that matches the given id", notes = "Returns the Configuration object that matches the given id.")
	@Override
	public Response getObject(@ApiParam(value = "configuration identifier", required = true) final Long id) throws DMPControllerException {

		return super.getObject(id);
	}

	@ApiOperation(value = "create a new configuration", notes = "Returns a new Configuration object.", response = Configuration.class)
	@Override
	public Response createObject(@ApiParam(value = "configuration identifier", required = true) final String jsonObjectString)
			throws DMPControllerException {

		return super.createObject(jsonObjectString);
	}

	@ApiOperation(value = "get all configurations ", notes = "Returns a list of Configuration objects.")
	@Override
	public Response getObjects() throws DMPControllerException {

		return super.getObjects();
	}

	@Override
	protected Configuration prepareObjectForUpdate(final Configuration objectFromJSON, final Configuration object) {
		final String name = objectFromJSON.getName();

		if (name != null) {

			object.setName(name);
		}

		final String description = objectFromJSON.getDescription();

		if (description != null) {

			object.setDescription(description);
		}

		final ObjectNode parameters = objectFromJSON.getParameters();

		if (parameters != null && parameters.size() > 0) {

			object.setParameters(parameters);
		}
		return object;
	}
}
