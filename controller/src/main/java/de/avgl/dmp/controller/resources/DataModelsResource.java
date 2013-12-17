package de.avgl.dmp.controller.resources;

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
import de.avgl.dmp.controller.status.DMPStatus;
import de.avgl.dmp.persistence.model.resource.DataModel;
import de.avgl.dmp.persistence.service.resource.DataModelService;

/**
 * @author tgaengler
 */
@RequestScoped
@Api(value = "/datamodels", description = "Operations about data models.")
@Path("datamodels")
public class DataModelsResource extends ExtendedBasicDMPResource<DataModelService, DataModel> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(DataModelsResource.class);

	@Inject
	public DataModelsResource(final Provider<DataModelService> dataModelServiceProviderArg, final ObjectMapper objectMapper, final DMPStatus dmpStatus) {

		super(DataModel.class, dataModelServiceProviderArg, objectMapper, dmpStatus);
	}

	@ApiOperation(value = "get the data model that matches the given id", notes = "Returns the DataModel object that matches the given id.")
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObject(@ApiParam(value = "data model identifier", required = true) @PathParam("id") final Long id)
			throws DMPControllerException {

		return super.getObject(id);
	}

	@ApiOperation(value = "create a new data model", notes = "Returns a new DataModel object.", response = DataModel.class)
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response createObject(@ApiParam(value = "data model (as JSON)", required = true) final String jsonObjectString)
			throws DMPControllerException {

		return super.createObject(jsonObjectString);
	}

	@ApiOperation(value = "get all data models ", notes = "Returns a list of DataModel objects.")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObjects() throws DMPControllerException {

		return super.getObjects();
	}

	@Override
	protected DataModel prepareObjectForUpdate(final DataModel objectFromJSON, final DataModel object) {

		super.prepareObjectForUpdate(objectFromJSON, object);
		
		object.setDataResource(objectFromJSON.getDataResource());
		object.setConfiguration(objectFromJSON.getConfiguration());
		object.setSchema(objectFromJSON.getSchema());

		return object;
	}
}
