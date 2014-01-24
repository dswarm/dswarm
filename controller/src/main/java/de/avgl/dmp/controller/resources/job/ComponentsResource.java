package de.avgl.dmp.controller.resources.job;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.inject.servlet.RequestScoped;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import de.avgl.dmp.controller.DMPControllerException;
import de.avgl.dmp.controller.resources.ExtendedBasicDMPResource;
import de.avgl.dmp.controller.resources.job.utils.ComponentsResourceUtils;
import de.avgl.dmp.controller.resources.utils.ResourceUtilsFactory;
import de.avgl.dmp.controller.status.DMPStatus;
import de.avgl.dmp.persistence.model.job.Component;
import de.avgl.dmp.persistence.model.job.proxy.ProxyComponent;
import de.avgl.dmp.persistence.service.job.ComponentService;

/**
 * A resource (controller service) for {@link Component}s.
 * 
 * @author tgaengler
 */
@RequestScoped
@Api(value = "/components", description = "Operations about components.")
@Path("components")
public class ComponentsResource extends ExtendedBasicDMPResource<ComponentsResourceUtils, ComponentService, ProxyComponent, Component> {

	/**
	 * Creates a new resource (controller service) for {@link Component}s with the provider of the component persistence service,
	 * the object mapper and metrics registry.
	 * 
	 * @param componentServiceProviderArg the component persistence service provider
	 * @param objectMapperArg an object mapper
	 * @param dmpStatusArg a metrics registry
	 */
	@Inject
	public ComponentsResource(final ResourceUtilsFactory utilsFactory, final DMPStatus dmpStatusArg) throws DMPControllerException {

		super(utilsFactory.reset().get(ComponentsResourceUtils.class), dmpStatusArg);
	}

	/**
	 * This endpoint returns a component as JSON representation for the provided component identifier.
	 * 
	 * @param id a component identifier
	 * @return a JSON representation of a component
	 */
	@ApiOperation(value = "get the component that matches the given id", notes = "Returns the Component object that matches the given id.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "returns the component (as JSON) that matches the given id"),
			@ApiResponse(code = 404, message = "could not find a component for the given id"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObject(@ApiParam(value = "component identifier", required = true) @PathParam("id") final Long id)
			throws DMPControllerException {

		return super.getObject(id);
	}

	/**
	 * This endpoint consumes a component as JSON representation and persists this component in the database.
	 * 
	 * @param jsonObjectString a JSON representation of one component
	 * @return the persisted component as JSON representation
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "create a new component", notes = "Returns a new Component object.", response = Component.class)
	@ApiResponses(value = { @ApiResponse(code = 201, message = "component was successfully persisted"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response createObject(@ApiParam(value = "component (as JSON)", required = true) final String jsonObjectString)
			throws DMPControllerException {

		return super.createObject(jsonObjectString);
	}

	/**
	 * This endpoint returns a list of all components as JSON representation.
	 * 
	 * @return a list of all components as JSON representation
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "get all components ", notes = "Returns a list of Component objects.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "returns all available components (as JSON)"),
			@ApiResponse(code = 404, message = "could not find any component, i.e., there are no components available"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObjects() throws DMPControllerException {

		return super.getObjects();
	}

	/**
	 * This endpoint consumes a component as JSON representation and updates this component in the database.
	 * 
	 * @param jsonObjectString a JSON representation of one component
	 * @param id a component identifier
	 * @return the updated filter as JSON representation
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "update component with given id ", notes = "Returns an updated Component object.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "component was successfully updated"),
			@ApiResponse(code = 404, message = "could not find a component for the given id"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateObject(@ApiParam(value = "component (as JSON)", required = true) final String jsonObjectString,
			@ApiParam(value = "component identifier", required = true) @PathParam("id") final Long id) throws DMPControllerException {

		return super.updateObject(jsonObjectString, id);
	}

	/**
	 * This endpoint deletes a component that matches the given id.
	 * 
	 * @param id a component identifier
	 * @return status 204 if removal was successful, 404 if id not found, 409 if it couldn't be removed, or 500 if something else
	 *         went wrong
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "delete component that matches the given id", notes = "Returns status 204 if removal was successful, 404 if id not found, 409 if it couldn't be removed, or 500 if something else went wrong.")
	@ApiResponses(value = { @ApiResponse(code = 204, message = "component was successfully deleted"),
			@ApiResponse(code = 404, message = "could not find a component for the given id"),
			@ApiResponse(code = 409, message = "component couldn't be deleted (maybe there are some existing constraints to related objects)"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@DELETE
	@Path("/{id}")
	@Override
	public Response deleteObject(@ApiParam(value = "component identifier", required = true) @PathParam("id") final Long id)
			throws DMPControllerException {

		return super.deleteObject(id);
	}

	/**
	 * {@inheritDoc}<br/>
	 * Updates the name, description, function, parameter mappings, input components and output components of the component.
	 */
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
