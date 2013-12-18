package de.avgl.dmp.controller.resources;

import java.util.Iterator;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Optional;
import com.google.common.collect.Iterators;
import com.google.common.eventbus.EventBus;
import com.google.inject.Provider;
import com.google.inject.servlet.RequestScoped;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import de.avgl.dmp.controller.DMPControllerException;
import de.avgl.dmp.controller.eventbus.CSVConverterEvent;
import de.avgl.dmp.controller.eventbus.ConverterEvent;
import de.avgl.dmp.controller.eventbus.SchemaEvent;
import de.avgl.dmp.controller.eventbus.XMLSchemaEvent;
import de.avgl.dmp.controller.status.DMPStatus;
import de.avgl.dmp.controller.utils.InternalSchemaDataUtil;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.DataModel;
import de.avgl.dmp.persistence.model.types.Tuple;
import de.avgl.dmp.persistence.service.resource.DataModelService;

/**
 * @author tgaengler
 */
@RequestScoped
@Api(value = "/datamodels", description = "Operations about data models.")
@Path("datamodels")
public class DataModelsResource extends ExtendedBasicDMPResource<DataModelService, DataModel> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(DataModelsResource.class);

	private final Provider<EventBus>				eventBusProvider;

	private final InternalSchemaDataUtil			schemaDataUtil;

	@Inject
	public DataModelsResource(final Provider<DataModelService> dataModelServiceProviderArg, final ObjectMapper objectMapper,
			final DMPStatus dmpStatus, final Provider<EventBus> eventBusProviderArg, final InternalSchemaDataUtil schemaDataUtilArg) {

		super(DataModel.class, dataModelServiceProviderArg, objectMapper, dmpStatus);

		eventBusProvider = eventBusProviderArg;
		schemaDataUtil = schemaDataUtilArg;
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

	/**
	 * Returns the data for a given data model.
	 * 
	 * @param id the data model identifier
	 * @param atMost
	 * @return
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "get the data of the data model that matches the given data model id", notes = "Returns the data of the data model that matches the given data model id.")
	@GET
	@Path("/{id}/data")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getData(@ApiParam(value = "data model identifier", required = true) @PathParam("id") final Long id,
			@ApiParam("number of records limit") @QueryParam("atMost") final Integer atMost) throws DMPControllerException {

		// final Timer.Context context = dmpStatus.getConfigurationData();

		LOG.debug("try to get data for data model with id '" + id + "'");

		final Optional<Iterator<Tuple<String, JsonNode>>> data = schemaDataUtil.getData(id, Optional.fromNullable(atMost));

		if (!data.isPresent()) {

			LOG.debug("couldn't find data for data model with id '" + id + "'");

			// dmpStatus.stop(context);
			return Response.status(Status.NOT_FOUND).build();
		}

		// temp
		final Iterator<Tuple<String, JsonNode>> tupleIterator;
		if (atMost != null) {
			tupleIterator = Iterators.limit(data.get(), atMost);
		} else {
			tupleIterator = data.get();
		}

		final ObjectNode json = objectMapper.createObjectNode();
		while (tupleIterator.hasNext()) {
			final Tuple<String, JsonNode> tuple = data.get().next();
			json.put(tuple.v1(), tuple.v2());
		}

		final String jsonString;

		try {

			jsonString = objectMapper.writeValueAsString(json);
		} catch (final JsonProcessingException e) {

			// dmpStatus.stop(context);
			throw new DMPControllerException("couldn't transform data to JSON string.\n" + e.getMessage());
		}

		LOG.debug("return data for data model with id '" + id + "' and content '" + jsonString + "'");

		// dmpStatus.stop(context);
		return buildResponse(jsonString);
	}

	@Override
	protected DataModel addObject(final String objectJSONString) throws DMPControllerException {

		final DataModel dataModel = super.addObject(objectJSONString);

		if (dataModel == null) {

			return dataModel;
		}

		// final Timer.Context context = dmpStatus.createNewConfiguration();

		DataModelsResource.LOG.debug("try to process data for data model with id '" + dataModel.getId() + "'");

		final Configuration configuration = dataModel.getConfiguration();

		if (configuration == null) {

			// dmpStatus.stop(context);

			DataModelsResource.LOG.debug("The data model has no configuration. Hence, the data of the data model cannot be processed.");
			
			return dataModel;
		}

		final JsonNode jsStorageType = configuration.getParameters().get("storage_type");
		if (jsStorageType != null) {
			final String storageType = jsStorageType.asText();
			try {
				final SchemaEvent.SchemaType type = SchemaEvent.SchemaType.fromString(storageType);
				eventBusProvider.get().post(new SchemaEvent(dataModel.getDataResource(), configuration, type));
			} catch (final IllegalArgumentException e) {
				DataModelsResource.LOG.warn("could not determine schema type", e);
			}

			switch (storageType) {
				case "schema":

					eventBusProvider.get().post(new XMLSchemaEvent(configuration, dataModel.getDataResource()));
					break;
				case "csv":

					eventBusProvider.get().post(new CSVConverterEvent(configuration, dataModel.getDataResource()));
					break;
				case "xml":

					eventBusProvider.get().post(new ConverterEvent(dataModel));
					break;
			}
		}

		return dataModel;
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
