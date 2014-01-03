package de.avgl.dmp.controller.resources.job;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Provider;
import com.google.inject.servlet.RequestScoped;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import de.avgl.dmp.controller.DMPControllerException;
import de.avgl.dmp.controller.resources.ExtendedBasicDMPResource;
import de.avgl.dmp.controller.status.DMPStatus;
import de.avgl.dmp.persistence.model.job.Component;
import de.avgl.dmp.persistence.service.job.ComponentService;

@RequestScoped
@Api(value = "/components", description = "Operations about components.")
@Path("components")
public class ComponentsResource extends ExtendedBasicDMPResource<ComponentService, Component> {

	@Inject
	public ComponentsResource(final Provider<ComponentService> componentServiceProviderArg, final ObjectMapper objectMapper, final DMPStatus dmpStatus) {

		super(Component.class, componentServiceProviderArg, objectMapper, dmpStatus);
	}

	@ApiOperation(value = "get the component that matches the given id", notes = "Returns the Component object that matches the given id.")
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObject(@ApiParam(value = "component identifier", required = true) @PathParam("id") final Long id)
			throws DMPControllerException {

		return super.getObject(id);
	}

	@ApiOperation(value = "create a new component", notes = "Returns a new Component object.", response = Component.class)
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response createObject(@ApiParam(value = "component (as JSON)", required = true) final String jsonObjectString)
			throws DMPControllerException {

		return super.createObject(jsonObjectString);
	}

	@ApiOperation(value = "get all components ", notes = "Returns a list of Component objects.")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObjects() throws DMPControllerException {

		return super.getObjects();
	}

	@Override
	protected Component prepareObjectForUpdate(final Component objectFromJSON, final Component object) {

		super.prepareObjectForUpdate(objectFromJSON, object);

		object.setFunction(objectFromJSON.getFunction());
		object.setParameterMappings(objectFromJSON.getParameterMappings());
		object.setInputComponents(objectFromJSON.getInputComponents());
		object.setOutputComponents(objectFromJSON.getOutputComponents());

		return object;
	}
}
