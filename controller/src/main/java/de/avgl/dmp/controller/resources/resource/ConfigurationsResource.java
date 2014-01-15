package de.avgl.dmp.controller.resources.resource;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.servlet.RequestScoped;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import de.avgl.dmp.controller.DMPControllerException;
import de.avgl.dmp.controller.resources.ExtendedBasicDMPResource;
import de.avgl.dmp.controller.resources.resource.utils.ConfigurationsResourceUtils;
import de.avgl.dmp.controller.status.DMPStatus;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.service.resource.ConfigurationService;

/**
 * A resource (controller service) for {@link Configuration}s.
 * 
 * @author tgaengler
 */
@RequestScoped
@Api(value = "/configurations", description = "Operations about configurations")
@Path("configurations")
public class ConfigurationsResource extends ExtendedBasicDMPResource<ConfigurationsResourceUtils, ConfigurationService, Configuration> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(ConfigurationsResource.class);

	/**
	 * Creates a new resource (controller service) for {@link Configuration}s with the provider of the component persistence
	 * service, the object mapper and metrics registry.
	 * 
	 * @param persistenceServiceProviderArg the component persistence service provider
	 * @param objectMapperArg an object mapper
	 * @param dmpStatusArg a metrics registry
	 */
	@Inject
	public ConfigurationsResource(final ConfigurationsResourceUtils pojoClassResourceUtilsArg,
			final DMPStatus dmpStatusArg) {

		super(pojoClassResourceUtilsArg, dmpStatusArg);
	}

	/**
	 * This endpoint returns a configuration as JSON representation for the provided configuration identifier.
	 * 
	 * @param id a configuration identifier
	 * @return a JSON representation of a configuration
	 */
	@ApiOperation(value = "get the configuration that matches the given id", notes = "Returns the Configuration object that matches the given id.")
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObject(@ApiParam(value = "configuration identifier", required = true) @PathParam("id") final Long id)
			throws DMPControllerException {

		return super.getObject(id);
	}

	/**
	 * This endpoint consumes a configuration as JSON representation and persists this configuration in the database.
	 * 
	 * @param jsonObjectString a JSON representation of one configuration
	 * @return the persisted configuration as JSON representation
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "create a new configuration", notes = "Returns a new Configuration object.", response = Configuration.class)
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response createObject(@ApiParam(value = "configuration identifier", required = true) final String jsonObjectString)
			throws DMPControllerException {

		return super.createObject(jsonObjectString);
	}

	/**
	 * This endpoint returns a list of all configurations as JSON representation.
	 * 
	 * @return a list of all configurations as JSON representation
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "get all configurations ", notes = "Returns a list of Configuration objects.")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObjects() throws DMPControllerException {

		return super.getObjects();
	}

	/**
	 * {@inheritDoc}<br/>
	 * Updates the name, description, resources and parameters of the configuration.
	 */
	@Override
	protected Configuration prepareObjectForUpdate(final Configuration objectFromJSON, final Configuration object) {

		super.prepareObjectForUpdate(objectFromJSON, object);

		final ObjectNode parameters = objectFromJSON.getParameters();

		if (parameters != null && parameters.size() > 0) {

			object.setParameters(parameters);
		}

		object.setResources(objectFromJSON.getResources());

		return object;
	}

	@Override
	protected String prepareObjectJSONString(final String objectJSONString) throws DMPControllerException {

		// a configuration is not a complex object

		return objectJSONString;
	}
}
