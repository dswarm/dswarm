/**
 * Copyright (C) 2013 – 2015 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dswarm.controller.resources.resource;

import java.util.Iterator;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Optional;
import com.google.common.collect.Iterators;
import com.google.inject.Provider;
import com.google.inject.servlet.RequestScoped;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.controller.DMPControllerException;
import org.dswarm.controller.eventbus.CSVConverterEvent;
import org.dswarm.controller.eventbus.CSVConverterEventRecorder;
import org.dswarm.controller.eventbus.SchemaEvent;
import org.dswarm.controller.eventbus.SchemaEventRecorder;
import org.dswarm.controller.eventbus.XMLConverterEvent;
import org.dswarm.controller.eventbus.XMLConverterEventRecorder;
import org.dswarm.controller.eventbus.XMLSchemaEvent;
import org.dswarm.controller.eventbus.XMLSchemaEventRecorder;
import org.dswarm.controller.resources.ExtendedBasicDMPResource;
import org.dswarm.controller.resources.resource.utils.ExportUtils;
import org.dswarm.controller.utils.DataModelUtil;
import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.proxy.RetrievalType;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.resource.proxy.ProxyDataModel;
import org.dswarm.persistence.model.types.Tuple;
import org.dswarm.persistence.service.resource.DataModelService;
import org.dswarm.persistence.util.GDMUtil;

/**
 * A resource (controller service) for {@link DataModel}s.
 *
 * @author tgaengler
 */
@RequestScoped
@Api(value = "/datamodels", description = "Operations about data models.")
@Path("datamodels")
public class DataModelsResource extends ExtendedBasicDMPResource<DataModelService, ProxyDataModel, DataModel> {

	private static final Logger LOG = LoggerFactory.getLogger(DataModelsResource.class);

	/**
	 * The data model util
	 */
	private final DataModelUtil dataModelUtil;

	private final Provider<SchemaEventRecorder>       schemaEventRecorderProvider;
	private final Provider<XMLSchemaEventRecorder>    xmlSchemaEventRecorderProvider;
	private final Provider<CSVConverterEventRecorder> csvConverterEventRecorderProvider;
	private final Provider<XMLConverterEventRecorder> xmlConvertEventRecorderProvider;

	// this is likely to be http://localhost:7474/graph
	private final String graphEndpoint;

	/**
	 * Creates a new resource (controller service) for {@link DataModel}s with the provider of the data model persistence service,
	 * the object mapper, metrics registry, event bus provider and data model util.
	 *
	 * @param persistenceServiceProviderArg
	 * @param objectMapperProviderArg
	 * @param dataModelUtilArg                     the data model util
	 * @param schemaEventRecorderProviderArg
	 * @param xmlSchemaEventRecorderProviderArg
	 * @param csvConverterEventRecorderProviderArg
	 * @param xmlConverterEventRecorderProviderArg
	 * @param graphEndpointArg
	 * @throws DMPControllerException
	 */
	@Inject
	public DataModelsResource(final javax.inject.Provider<DataModelService> persistenceServiceProviderArg,
			final javax.inject.Provider<ObjectMapper> objectMapperProviderArg, final DataModelUtil dataModelUtilArg,
			final Provider<SchemaEventRecorder> schemaEventRecorderProviderArg,
			final Provider<XMLSchemaEventRecorder> xmlSchemaEventRecorderProviderArg,
			final Provider<CSVConverterEventRecorder> csvConverterEventRecorderProviderArg,
			final Provider<XMLConverterEventRecorder> xmlConverterEventRecorderProviderArg,
			@Named("dswarm.db.graph.endpoint") final String graphEndpointArg) throws DMPControllerException {

		super(DataModel.class, persistenceServiceProviderArg, objectMapperProviderArg);

		dataModelUtil = dataModelUtilArg;
		schemaEventRecorderProvider = schemaEventRecorderProviderArg;
		xmlSchemaEventRecorderProvider = xmlSchemaEventRecorderProviderArg;
		csvConverterEventRecorderProvider = csvConverterEventRecorderProviderArg;
		xmlConvertEventRecorderProvider = xmlConverterEventRecorderProviderArg;
		graphEndpoint = graphEndpointArg;
	}

	/**
	 * This endpoint returns a data model as JSON representation for the provided data model identifier.
	 *
	 * @param id a data model identifier
	 * @return a JSON representation of a data model
	 */
	@ApiOperation(value = "get the data model that matches the given id", notes = "Returns the DataModel object that matches the given id.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "returns the data model (as JSON) that matches the given id"),
			@ApiResponse(code = 404, message = "could not find a data model for the given id"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObject(@ApiParam(value = "data model identifier", required = true) @PathParam("id") final String id)
			throws DMPControllerException {

		return super.getObject(id);
	}

	/**
	 * This endpoint consumes a data model as JSON representation and persists this data model in the database, i.e., the data
	 * resource of this data model will be processed re. the parameters in the configuration of the data model. Thereby, the
	 * schema of the data will be created as well.
	 *
	 * @param jsonObjectString a JSON representation of one data model
	 * @return the persisted data model as JSON representation
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "create a new data model", notes = "Returns a new DataModel object. The data resource of this data model will be processed re. the parameters in the configuration of the data model. Thereby, the schema of the data will be created as well. ", response = DataModel.class)
	@ApiResponses(value = { @ApiResponse(code = 201, message = "data model was successfully persisted"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response createObject(@ApiParam(value = "data model (as JSON)", required = true) final String jsonObjectString)
			throws DMPControllerException {

		return super.createObject(jsonObjectString);
	}

	/**
	 * This endpoint returns a list of all data models as JSON representation.
	 *
	 * @return a list of all data models as JSON representation
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "get all data models ", notes = "Returns a list of DataModel objects.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "returns all available data models (as JSON)"),
			@ApiResponse(code = 404, message = "could not find any data model, i.e., there are no data models available"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObjects() throws DMPControllerException {

		return super.getObjects();
	}

	/**
	 * This endpoint consumes a data model as JSON representation and updates this data model in the database.
	 *
	 * @param jsonObjectString a JSON representation of one data model
	 * @param uuid             a data model identifier
	 * @return the updated data model as JSON representation
	 * @throws DMPControllerException
	 */
	@Override
	@ApiOperation(value = "update data model with given id ", notes = "Returns an updated DataModel object.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "data model was successfully updated"),
			@ApiResponse(code = 404, message = "could not find a data model for the given id"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateObject(@ApiParam(value = "data model (as JSON)", required = true) final String jsonObjectString,
			@ApiParam(value = "data model identifier", required = true) @PathParam("id") final String uuid) throws DMPControllerException {

		return super.updateObject(jsonObjectString, uuid);
	}

	/**
	 * Returns the data for a given data model.
	 *
	 * @param uuid   the data model identifier
	 * @param atMost the number of records that should be returned at most
	 * @return the data for a given data model
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "get the data of the data model that matches the given data model uuid", notes = "Returns the data of the data model that matches the given data model uuid.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "could retrieve the data successfully for the data model that matches the given uuid"),
			@ApiResponse(code = 404, message = "could not find a data model for the given uuid or data for this data model"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@Timed
	@GET
	@Path("/{uuid}/data")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getData(@ApiParam(value = "data model identifier", required = true) @PathParam("uuid") final String uuid,
			@ApiParam("number of records limit") @QueryParam("atMost") final Integer atMost) throws DMPControllerException {

		// final Timer.Context context = dmpStatus.getConfigurationData();

		DataModelsResource.LOG.debug("try to get data for data model with uuid '" + uuid + "'");

		final Optional<Iterator<Tuple<String, JsonNode>>> data = dataModelUtil.getData(uuid, Optional.fromNullable(atMost));

		if (!data.isPresent()) {

			DataModelsResource.LOG.debug("couldn't find data for data model with uuid '" + uuid + "'");

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

		final ObjectNode json = objectMapperProvider.get().createObjectNode();
		while (tupleIterator.hasNext()) {
			final Tuple<String, JsonNode> tuple = data.get().next();
			json.set(tuple.v1(), tuple.v2());
		}

		final String jsonString = serializeObject(json);

		DataModelsResource.LOG.debug("return data for data model with uuid '" + uuid + "' ");
		DataModelsResource.LOG.trace("and content '" + jsonString + "'");

		// dmpStatus.stop(context);
		return buildResponse(jsonString);
	}

	/**
	 * @param uuid
	 * @param format serialization format the data model should be serialized in, injected from accept header field
	 * @return a single data model, serialized in exportLanguage
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "exports a selected data model from the graph DB in the requested RDF serialisation format", notes = "Returns exported data in the requested RDF serialisation format.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "export was successfully processed"),
			@ApiResponse(code = 404, message = "could not find a data model for the given uuid"),
			@ApiResponse(code = 406, message = "requested export format is not supported"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@Timed
	@GET
	@Path("/{uuid}/export")
	// SR TODO removing of @Produces should result in accepting any requested format (accept header?) Is this what we want as a
	// proxy endpoint - let the graph endpoint decide which formats are accepted
	// @Produces({ MediaTypeUtil.N_QUADS, MediaTypeUtil.RDF_XML, MediaTypeUtil.TRIG, MediaTypeUtil.TURTLE, MediaTypeUtil.N3 })
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response exportForDownload(@ApiParam(value = "data model identifier", required = true) @PathParam("uuid") final String uuid,
			@QueryParam("format") String format) throws DMPControllerException {

		// check if uuid is present, return 404 if not
		final DataModelService persistenceService = persistenceServiceProvider.get();
		final DataModel freshDataModel = persistenceService.getObject(uuid);
		if (freshDataModel == null) {
			return Response.status(Status.NOT_FOUND).build();
		}

		// construct dataModelURI from data model uuid
		final String dataModelURI = GDMUtil.getDataModelGraphURI(uuid);

		LOG.debug("Forwarding to graph db: request to export rdf of datamodel with uuid \"" + uuid + "\" to " + format);

		// send the request to graph DB
		final WebTarget target = target("/export");
		final Response responseFromGraph = target.queryParam("data_model_uri", dataModelURI).request().accept(format).get(Response.class);

		return ExportUtils.processGraphDBResponseInternal(responseFromGraph);
	}

	/**
	 * This endpoint deletes a data model that matches the given id.
	 *
	 * @param id a data model identifier
	 * @return status 204 if removal was successful, 404 if id not found, 409 if it couldn't be removed, or 500 if something else
	 * went wrong
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "delete data model that matches the given id", notes = "Returns status 204 if removal was successful, 404 if id not found, 409 if it couldn't be removed, or 500 if something else went wrong.")
	@ApiResponses(value = { @ApiResponse(code = 204, message = "data model was successfully deleted"),
			@ApiResponse(code = 404, message = "could not find a data model for the given id"),
			@ApiResponse(code = 409, message = "data model couldn't be deleted (maybe there are some existing constraints to related objects)"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@DELETE
	@Path("/{id}")
	@Override
	public Response deleteObject(@ApiParam(value = "data model identifier", required = true) @PathParam("id") final String id)
			throws DMPControllerException {

		return super.deleteObject(id);
	}

	/**
	 * {@inheritDoc}<br/>
	 * The data of the data model will also be converted and persisted.
	 */
	@Override
	protected ProxyDataModel addObject(final String objectJSONString) throws DMPControllerException {

		ProxyDataModel proxyDataModel = super.addObject(objectJSONString);

		if (proxyDataModel == null) {

			return proxyDataModel;
		}

		DataModel dataModel = proxyDataModel.getObject();

		if (dataModel == null) {

			return proxyDataModel;
		}

		if (dataModel.getConfiguration() != null) {

			// add configuration to data resource
			dataModel.getDataResource().addConfiguration(dataModel.getConfiguration());

			try {

				final ProxyDataModel proxyUpdatedDataModel = persistenceServiceProvider.get().updateObjectTransactional(dataModel);

				if (proxyUpdatedDataModel == null) {

					DataModelsResource.LOG.error("something went wrong, when trying to add configuration to data resource of data model '"
							+ dataModel.getUuid() + "'");

					proxyDataModel = null;
				} else {

					final RetrievalType type = proxyDataModel.getType();

					proxyDataModel = new ProxyDataModel(proxyUpdatedDataModel.getObject(), type);
				}
			} catch (final DMPPersistenceException e) {

				DataModelsResource.LOG.error("something went wrong, when trying to add configuration to data resource of data model '"
						+ dataModel.getUuid() + "'");

				proxyDataModel = null;
			}
		}

		if (proxyDataModel == null) {

			return proxyDataModel;
		}

		dataModel = proxyDataModel.getObject();

		if (dataModel == null) {

			return proxyDataModel;
		}

		// final Timer.Context context = dmpStatus.createNewConfiguration();

		DataModelsResource.LOG.debug("try to process data for data model with id '" + dataModel.getUuid() + "'");

		final Configuration configuration = dataModel.getConfiguration();

		if (configuration == null) {

			// dmpStatus.stop(context);

			DataModelsResource.LOG.debug("The data model has no configuration. Hence, the data of the data model cannot be processed.");

			return proxyDataModel;
		}

		final JsonNode jsStorageType = configuration.getParameters().get("storage_type");
		if (jsStorageType != null) {
			final String storageType = jsStorageType.asText();

			try {
				final SchemaEvent.SchemaType type = SchemaEvent.SchemaType.fromString(storageType);
				final SchemaEvent schemaEvent = new SchemaEvent(dataModel, type);
				schemaEventRecorderProvider.get().convertSchema(schemaEvent);
			} catch (final IllegalArgumentException e) {
				DataModelsResource.LOG.warn("could not determine schema type", e);
			}

			switch (storageType) {
				case "schema":

					// eventBusProvider.get().post(new XMLSchemaEvent(configuration, dataModel.getDataResource()));

					final XMLSchemaEvent xmlSchemaEvent = new XMLSchemaEvent(configuration, dataModel.getDataResource());
					xmlSchemaEventRecorderProvider.get().convertConfiguration(xmlSchemaEvent);

					break;
				case "csv":

					// eventBusProvider.get().post(new CSVConverterEvent(dataModel));

					final CSVConverterEvent csvConverterEvent = new CSVConverterEvent(dataModel);
					csvConverterEventRecorderProvider.get().convertConfiguration(csvConverterEvent);

					break;
				case "xml":
				case "mabxml":
				case "marc21":

					// eventBusProvider.get().post(new XMLConverterEvent(dataModel));

					final XMLConverterEvent xmlConverterEvent = new XMLConverterEvent(dataModel);
					xmlConvertEventRecorderProvider.get().processDataModel(xmlConverterEvent);

					break;
			}
		}

		// refresh data model
		final DataModelService persistenceService = persistenceServiceProvider.get();
		final DataModel freshDataModel = persistenceService.getObject(dataModel.getUuid());
		final RetrievalType type = proxyDataModel.getType();

		proxyDataModel = new ProxyDataModel(freshDataModel, type);

		return proxyDataModel;
	}

	/**
	 * {@inheritDoc}<br/>
	 * Updates the name, description, resource, configuration and schema of the data model.
	 */
	@Override
	protected DataModel prepareObjectForUpdate(final DataModel objectFromJSON, final DataModel object) {

		super.prepareObjectForUpdate(objectFromJSON, object);

		object.setDataResource(objectFromJSON.getDataResource());
		object.setConfiguration(objectFromJSON.getConfiguration());
		object.setSchema(objectFromJSON.getSchema());

		return object;
	}

	private Client client() {

		final ClientBuilder builder = ClientBuilder.newBuilder();

		return builder.register(MultiPartFeature.class).build();
	}

	private WebTarget target() {

		return client().target(graphEndpoint).path(RDFResource.resourceIdentifier);
	}

	private WebTarget target(final String... path) {

		WebTarget target = target();

		for (final String p : path) {

			target = target.path(p);
		}

		return target;
	}

}
