package de.avgl.dmp.controller.resources;

import javax.inject.Inject;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Provider;
import com.google.inject.servlet.RequestScoped;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import de.avgl.dmp.controller.DMPControllerException;
import de.avgl.dmp.controller.status.DMPStatus;
import de.avgl.dmp.persistence.model.job.Component;
import de.avgl.dmp.persistence.service.job.ComponentService;


@RequestScoped
@Api(value = "/components", description = "Operations about components.")
@Path("components")
public class ComponentsResource extends BasicResource<ComponentService, Component, Long> {

	@Context
	UriInfo											uri;

	@Inject
	public ComponentsResource(final Provider<ComponentService> componentServiceProviderArg, final ObjectMapper objectMapper, final DMPStatus dmpStatus) {

		super(Component.class, componentServiceProviderArg, objectMapper, dmpStatus);
	}

	@ApiOperation(value = "get the component that matches the given id", notes = "Returns the Component object that matches the given id.")
	@Override
	public Response getObject(@ApiParam(value = "component identifier", required = true) final Long id) throws DMPControllerException {

		return super.getObject(id);
	}

	@ApiOperation(value = "create a new component", notes = "Returns a new Component object.", response = Component.class)
	@Override
	public Response createObject(@ApiParam(value = "component (as JSON)", required = true) final String jsonObjectString)
			throws DMPControllerException {

		return super.createObject(jsonObjectString);
	}

	@ApiOperation(value = "get all components ", notes = "Returns a list of Component objects.")
	@Override
	public Response getObjects() throws DMPControllerException {

		return super.getObjects();
	}

	@Override
	protected Component prepareObjectForUpdate(final Component objectFromJSON, final Component object) {

		object.setName(objectFromJSON.getName());

		return object;
	}
}
