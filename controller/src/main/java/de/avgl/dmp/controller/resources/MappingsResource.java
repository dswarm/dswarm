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
import de.avgl.dmp.persistence.model.job.Mapping;
import de.avgl.dmp.persistence.service.job.MappingService;


@RequestScoped
@Api(value = "/mappings", description = "Operations about mappings.")
@Path("mappings")
public class MappingsResource extends BasicResource<MappingService, Mapping, Long> {

	@Context
	UriInfo											uri;

	@Inject
	public MappingsResource(final Provider<MappingService> mappingServiceProviderArg, final ObjectMapper objectMapper, final DMPStatus dmpStatus) {

		super(Mapping.class, mappingServiceProviderArg, objectMapper, dmpStatus);
	}

	@ApiOperation(value = "get the mapping that matches the given id", notes = "Returns the Mapping object that matches the given id.")
	@Override
	public Response getObject(@ApiParam(value = "mapping identifier", required = true) final Long id) throws DMPControllerException {

		return super.getObject(id);
	}

	@ApiOperation(value = "create a new mapping", notes = "Returns a new Mapping object.", response = Mapping.class)
	@Override
	public Response createObject(@ApiParam(value = "mapping (as JSON)", required = true) final String jsonObjectString)
			throws DMPControllerException {

		return super.createObject(jsonObjectString);
	}

	@ApiOperation(value = "get all mappings ", notes = "Returns a list of Mapping objects.")
	@Override
	public Response getObjects() throws DMPControllerException {

		return super.getObjects();
	}

	@Override
	protected Mapping prepareObjectForUpdate(final Mapping objectFromJSON, final Mapping object) {

		object.setName(objectFromJSON.getName());
		object.setTransformation(objectFromJSON.getTransformation());
		object.setInputFilter(objectFromJSON.getInputFilter());
		object.setOutputFilter(objectFromJSON.getOutputFilter());
		object.setInputAttributePaths(objectFromJSON.getInputAttributePaths());
		object.setOutputAttributePath(objectFromJSON.getOutputAttributePath());

		return object;
	}
}
