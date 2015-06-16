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

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Optional;
import com.google.inject.Provider;
import com.google.inject.servlet.RequestScoped;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Observer;

import org.dswarm.common.DMPStatics;
import org.dswarm.common.types.Tuple;
import org.dswarm.controller.DMPControllerException;
import org.dswarm.controller.eventbus.CSVConverterEvent;
import org.dswarm.controller.eventbus.CSVConverterEventRecorder;
import org.dswarm.controller.eventbus.SchemaEvent;
import org.dswarm.controller.eventbus.SchemaEventRecorder;
import org.dswarm.controller.eventbus.XMLConverterEvent;
import org.dswarm.controller.eventbus.XMLConverterEventRecorder;
import org.dswarm.controller.eventbus.XMLSchemaEvent;
import org.dswarm.controller.eventbus.XMLSchemaEventRecorder;
import org.dswarm.controller.resources.ExtendedMediumBasicDMPResource;
import org.dswarm.controller.resources.POJOFormat;
import org.dswarm.controller.resources.resource.utils.ExportUtils;
import org.dswarm.controller.utils.DataModelUtil;
import org.dswarm.init.DMPException;
import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.dto.resource.MediumDataModelDTO;
import org.dswarm.persistence.model.proxy.RetrievalType;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.resource.UpdateFormat;
import org.dswarm.persistence.model.resource.proxy.ProxyDataModel;
import org.dswarm.persistence.model.resource.utils.ConfigurationStatics;
import org.dswarm.persistence.model.resource.utils.DataModelUtils;
import org.dswarm.persistence.model.schema.Clasz;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.model.schema.utils.ClaszUtils;
import org.dswarm.persistence.service.resource.DataModelService;
import org.dswarm.persistence.util.DMPPersistenceUtil;
import org.dswarm.persistence.util.GDMUtil;

/**
 * A resource (controller service) for {@link DataModel}s.
 *
 * @author tgaengler
 */
@RequestScoped
@Api(value = "/datamodels", description = "Operations about data models.")
@Path("datamodels")
public class DataModelsResource extends ExtendedMediumBasicDMPResource<DataModelService, ProxyDataModel, DataModel, MediumDataModelDTO> {

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
	public DataModelsResource(
			final Provider<DataModelService> persistenceServiceProviderArg,
			final Provider<ObjectMapper> objectMapperProviderArg,
			final DataModelUtil dataModelUtilArg,
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
	 * The format of the data model might either be a full or an abbreviated short variant.
	 *
	 * @param id a data model identifier
	 * @param format an enum that specifies which format to use
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
	public Response getObject(
			@ApiParam(value = "data model identifier", required = true) @PathParam("id") final String id,
			@ApiParam(value = "'short' for only uuid,name,description, 'medium' for additional configuration,resource, 'full' for the complete entity")
			@QueryParam("format") @DefaultValue("full") final POJOFormat format)
			throws DMPControllerException {

		return super.getObject(id, format);
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
	 * The format of the data model might either be a full or an abbreviated short variant.
	 *
	 * @param format an enum that specifies which format to use
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
	public Response getObjects(
			@ApiParam(value = "'short' for only uuid,name,description, 'medium' for additional configuration,resource, 'full' for the complete entity")
			@QueryParam("format") @DefaultValue("full") final POJOFormat format) throws DMPControllerException {

		return super.getObjects(format);
	}

	/**
	 * This endpoint consumes a data model as JSON representation and updates this data model in the metadata repository.
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

		// only the data model metadata would be updated
		return super.updateObject(jsonObjectString, uuid);
	}

	/**
	 * This endpoint consumes a data model uuid and updates the data model content in the datahub.
	 *
	 * @param uuid             a data model identifier
	 * @return the updated data model as JSON representation
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "update data model with given id ", notes = "Returns an updated DataModel object.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "data model was successfully updated"),
			@ApiResponse(code = 404, message = "could not find a data model for the given id"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@Timed
	@POST
	@Path("/{id}/data")
	public Response updateDataModelData(@ApiParam(value = "data model identifier", required = true) @PathParam("id") final String uuid,
			@ApiParam(value = "update format", required = false) @QueryParam("format") @DefaultValue("full") final UpdateFormat updateFormat,
			@ApiParam(value = "enable versioning", required = false) @QueryParam("enableVersioning") @DefaultValue("true") final boolean enableVersioning)
			throws DMPControllerException {

		DataModelsResource.LOG.debug("try to update {} '{}'", pojoClassName, uuid);

		final DataModel objectFromDB = retrieveObject(uuid, null);

		if (objectFromDB == null) {

			return Response.status(Status.NOT_FOUND).build();
		}

		final ProxyDataModel updatedProxyDataModel = updateDataModelContent(new ProxyDataModel(objectFromDB, RetrievalType.RETRIEVED), objectFromDB,
				updateFormat, enableVersioning);

		if (updatedProxyDataModel == null) {

			DataModelsResource.LOG.debug("couldn't update content for data model '{}'", uuid);

			throw new DMPControllerException(String.format("couldn't update content for data model '%s'", uuid));
		}

		final DataModel object = updatedProxyDataModel.getObject();

		if (object == null) {

			DataModelsResource.LOG.debug("couldn't update content for data model '{}'", uuid);

			throw new DMPControllerException(String.format("couldn't update content for data model '%s'", uuid));
		}

		DataModelsResource.LOG.debug("updated content for data model '{}'", uuid);

		if (DataModelsResource.LOG.isTraceEnabled()) {

			DataModelsResource.LOG.trace(" = '{}'", ToStringBuilder.reflectionToString(object));
		}

		// TODO: shall we return the content here? or a restricted amount of content?

		return Response.ok().build();
	}

	/**
	 * Returns the data for matched records of a given data model.
	 *
	 * @param uuid             a data model identifier
	 * @param searchRequestJSONString the search request as JSON string
	 * @param atMost the number of records that should be returned at most
	 * @return the data for matched records of a given data model
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "get the data of records that matches the search criteria and of the data model that matches the given data model uuid", notes = "Returns the data for matched records of a given data model.")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "could retrieve the data successfully for the data for matched records of a given data model"),
			@ApiResponse(code = 404, message = "could not find a data model for the given uuid"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@Timed
	@POST
	@Path("/{uuid}/records/search")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void searchRecords(
			@ApiParam(value = "data model identifier", required = true) @PathParam("uuid") final String uuid,
			@ApiParam(value = "search request (as JSON)", required = true) final String searchRequestJSONString,
			@ApiParam("number of records limit") @QueryParam("atMost") final Integer atMost,
			@Suspended final AsyncResponse asyncResponse) throws DMPControllerException {

		if (searchRequestJSONString == null) {

			final String message = "couldn't search records, because the request JSON string is null";

			DataModelsResource.LOG.error(message);

			throw new DMPControllerException(message);
		}

		final ObjectNode requestJSON;

		try {

			requestJSON = DMPPersistenceUtil.getJSON(searchRequestJSONString);
		} catch (final DMPException e) {

			final String message = "couldn't search records, because the request JSON string couldn't be deserialized";

			DataModelsResource.LOG.error(message, e);

			throw new DMPControllerException(message, e);
		}

		if (requestJSON == null) {

			final String message = "couldn't search records, because the request JSON is null";

			DataModelsResource.LOG.error(message);

			throw new DMPControllerException(message);
		}

		final Optional<String> optionalKeyAttributePathString = getStringValue(DMPStatics.KEY_ATTRIBUTE_PATH_IDENTIFIER, requestJSON);
		final Optional<String> optionalSearchValue = getStringValue(DMPStatics.SEARCH_VALUE_IDENTIFIER, requestJSON);

		if (!optionalKeyAttributePathString.isPresent() || !optionalSearchValue.isPresent()) {

			final String message = "couldn't search records, because the search request is insufficient";

			DataModelsResource.LOG.error(message);

			throw new DMPControllerException(message);
		}

		final String keyAttributePathString = optionalKeyAttributePathString.get();
		final String searchValue = optionalSearchValue.get();

		DataModelsResource.LOG.debug("try to search records for key attribute path '{}' and search value '{}' in data model with uuid '{}'",
				keyAttributePathString, searchValue, uuid);

		final Observable<Tuple<String, JsonNode>> data = dataModelUtil.searchRecords(
				keyAttributePathString, searchValue, uuid, Optional.fromNullable(atMost));

		data.subscribe(new StreamingDataObserver(uuid, objectMapperProvider.get(), pojoClassName, asyncResponse));
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
	public void getData(
			@ApiParam(value = "data model identifier", required = true) @PathParam("uuid") final String uuid,
			@ApiParam("number of records limit") @QueryParam("atMost") final Integer atMost,
			@Suspended final AsyncResponse asyncResponse) throws DMPControllerException {

		getDataInternal(uuid, atMost, asyncResponse);
	}

	/**
	 * TODO: rename endpoint to "../content" instead of "../export"
	 *
	 * @param uuid
	 * @param format serialization format the data model should be serialized in, injected from accept header field
	 * @return a single data model, serialized in exportLanguage
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "exports a selected data model from the graph DB in the requested format, .e.g., various RDF serialisation formats or XML", notes = "Returns exported data in the requested format.")
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
			@QueryParam("format") final String format) throws DMPControllerException {

		// check if uuid is present, return 404 if not
		final DataModelService persistenceService = persistenceServiceProvider.get();
		final DataModel freshDataModel = persistenceService.getObject(uuid);
		if (freshDataModel == null) {
			return Response.status(Status.NOT_FOUND).build();
		}

		// construct dataModelURI from data model uuid
		final String dataModelURI = GDMUtil.getDataModelGraphURI(uuid);

		LOG.debug("Forwarding to graph db: request to export content of data model with uuid \"{}\" to {}", uuid, format);

		final Response responseFromGraph;

		if (format != null && MediaType.APPLICATION_XML.equals(format)) {

			// build request JSON

			final ObjectNode requestJson = objectMapperProvider.get().createObjectNode();

			// record class uri
			final Optional<Schema> optionalSchema = Optional.fromNullable(freshDataModel.getSchema());

			if (optionalSchema.isPresent()) {

				final Optional<Clasz> optionalRecordClass = Optional.fromNullable(optionalSchema.get().getRecordClass());

				final String recordClassURI;

				if (optionalRecordClass.isPresent()) {

					recordClassURI = optionalRecordClass.get().getUri();
				} else {

					// fallback: bibo:Document as default record class
					recordClassURI = ClaszUtils.BIBO_DOCUMENT_URI;
				}

				requestJson.put(DMPStatics.RECORD_CLASS_URI_IDENTIFIER, recordClassURI);
			}

			// data model uri
			requestJson.put(DMPStatics.DATA_MODEL_URI_IDENTIFIER, dataModelURI);

			// root attribute path
			// TODO

			// record tag
			final Optional<Configuration> optionalConfiguration = Optional.fromNullable(freshDataModel.getConfiguration());

			if (optionalConfiguration.isPresent()) {

				final Optional<JsonNode> optionalRecordTagNode = Optional
						.fromNullable(optionalConfiguration.get().getParameter(ConfigurationStatics.RECORD_TAG));

				if (optionalRecordTagNode.isPresent()) {

					requestJson.put(DMPStatics.RECORD_TAG_IDENTIFIER, optionalRecordTagNode.get().asText());
				}
			}

			// version
			// TODO

			// original data model type
			final Optional<String> optionalOriginalDataModelType;

			switch (freshDataModel.getUuid()) {

				case DataModelUtils.MABXML_DATA_MODEL_UUID:
				case DataModelUtils.MARC21_DATA_MODEL_UUID:
				case DataModelUtils.PNX_DATA_MODEL_UUID:
				case DataModelUtils.OAI_PMH_DC_ELEMENTS_DATA_MODEL_UUID:
				case DataModelUtils.OAI_PMH_DC_TERMS_DATA_MODEL_UUID:
				case DataModelUtils.OAI_PMH_MARCXML_DATA_MODEL_UUID:

					optionalOriginalDataModelType = Optional.of(DMPStatics.XML_DATA_TYPE);

					break;
				default:

					if (optionalConfiguration.isPresent()) {

						final Configuration configuration = optionalConfiguration.get();

						final Optional<JsonNode> optionalStorageTypeNode = Optional
								.fromNullable(configuration.getParameter(ConfigurationStatics.STORAGE_TYPE));

						if (optionalStorageTypeNode.isPresent()) {

							final JsonNode storageTypeNode = optionalStorageTypeNode.get();

							final Optional<String> optionalStorageType = Optional.fromNullable(storageTypeNode.asText());

							if (optionalStorageType.isPresent()) {

								final String storageType = optionalStorageType.get();

								switch (storageType) {

									case ConfigurationStatics.XML_STORAGE_TYPE:
									case ConfigurationStatics.PNX_STORAGE_TYPE:
									case ConfigurationStatics.MABXML_STORAGE_TYPE:
									case ConfigurationStatics.MARCXML_STORAGE_TYPE:
									case ConfigurationStatics.OAI_PMH_DC_ELEMENTS_STORAGE_TYPE:
									case ConfigurationStatics.OAIPMH_DC_TERMS_STORAGE_TYPE:
									case ConfigurationStatics.OAIPMH_MARCXML_STORAGE_TYPE:

										optionalOriginalDataModelType = Optional.of(DMPStatics.XML_DATA_TYPE);

										break;
									default:

										optionalOriginalDataModelType = Optional.absent();
								}
							} else {

								optionalOriginalDataModelType = Optional.absent();
							}
						} else {

							optionalOriginalDataModelType = Optional.absent();
						}
					} else {

						optionalOriginalDataModelType = Optional.absent();
					}
			}

			if (optionalOriginalDataModelType.isPresent()) {

				requestJson.put(DMPStatics.ORIGINAL_DATA_TYPE_IDENTIFIER, optionalOriginalDataModelType.get());
			}

			final String requestJsonString = serializeObject(requestJson);

			// send the request to graph DB
			final WebTarget target = service().path("/xml/get");
			responseFromGraph = target.request(MediaType.APPLICATION_XML_TYPE).post(Entity.entity(requestJsonString, MediaType.APPLICATION_JSON));

			LOG.debug("request data from graph db with '{}' + '{}'", target.getUri(), serializeObject(requestJsonString));

			return ExportUtils.processGraphDBXMLResponseInternal(responseFromGraph);
		} else {

			// send the request to graph DB
			final WebTarget target = target("/export");
			responseFromGraph = target.queryParam("data_model_uri", dataModelURI).request().accept(format).get(Response.class);

			return ExportUtils.processGraphDBResponseInternal(responseFromGraph);
		}
	}

	/**
	 * This endpoint deletes a data model that matches the given id.
	 *
	 * @param id a data model identifier
	 * @return 204 if removal was successful, 404 if id not found, 409 if it couldn't be removed, or 500 if something else
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
	 * This endpoint deprecates a data model that matches the given id.
	 *
	 * @param id a data model identifier
	 * went wrong
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "deprecates data model that matches the given id", notes = "Returns status 200 if deprecation was successful, 404 if id not found, or 500 if something else went wrong.")
	@ApiResponses(value = { @ApiResponse(code = 204, message = "data model was successfully deprecated"),
			@ApiResponse(code = 404, message = "could not find a data model for the given id"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@POST
	@Path("/{id}/deprecate")
	@Produces(MediaType.APPLICATION_JSON)
	public void deprecateObject(@ApiParam(value = "data model identifier", required = true) @PathParam("id") final String id,
			@Suspended final AsyncResponse asyncResponse)
			throws DMPControllerException {

		final Observable<Response> responseObservable = dataModelUtil.deprecateDataModel(id);

		responseObservable.subscribe(new ResponseObserver(id, asyncResponse));
	}

	/**
	 * This endpoint deprecates a data model that matches the given id.
	 *
	 * @param id a data model identifier
	 * went wrong
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "deprecates data model that matches the given id", notes = "Returns status 200 if deprecation was successful, 404 if id not found, or 500 if something else went wrong.")
	@ApiResponses(value = { @ApiResponse(code = 204, message = "data model was successfully deprecated"),
			@ApiResponse(code = 404, message = "could not find a data model for the given id"),
			@ApiResponse(code = 500, message = "internal processing error (see body for details)") })
	@POST
	@Path("/{id}/deprecate/records")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void deprecateRecords(@ApiParam(value = "data model identifier", required = true) @PathParam("id") final String id,
			@ApiParam(value = "record URIs (as JSON array)", required = true) final String recordURIsJSONString,
			@Suspended final AsyncResponse asyncResponse)
			throws DMPControllerException {

		if (recordURIsJSONString == null) {

			final String message = "couldn't deprecate records, because the request JSON string is null";

			DataModelsResource.LOG.error(message);

			throw new DMPControllerException(message);
		}

		Collection<String> recordURIs = parseRecordURIs(recordURIsJSONString);

		final Observable<Response> responseObservable = dataModelUtil.deprecateRecords(recordURIs, id);

		responseObservable.subscribe(new ResponseObserver(id, asyncResponse));
	}

	private Collection<String> parseRecordURIs(final String recordURIsJSONString) throws DMPControllerException {

		try {
			return objectMapperProvider.get().readValue(recordURIsJSONString, new TypeReference<List<String>>() {

			});
		} catch (final IOException e) {

			final String message = "couldn't parse record URIs JSON array";

			LOG.error(message, e);

			throw new DMPControllerException(message, e);
		}
	}

	/**
	 * {@inheritDoc}<br/>
	 * The data of the data model will also be converted and persisted.
	 */
	@Override
	protected ProxyDataModel addObject(final String objectJSONString) throws DMPControllerException {

		final ProxyDataModel proxyDataModel = super.addObject(objectJSONString);

		if (proxyDataModel == null) {

			return null;
		}

		final DataModel dataModel = proxyDataModel.getObject();

		if (dataModel == null) {

			return proxyDataModel;
		}

		// versioning is disabled at data model creation, since there should be any data for this data model in the data hub
		return updateDataModel(proxyDataModel, dataModel, false);

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
		object.setDeprecated(objectFromJSON.isDeprecated());

		return object;
	}

	/**
	 * add configuration to data resource + update data model content
	 * note: this will only be called at data model creation
	 *
	 * @param proxyDataModel
	 * @param dataModel
	 * @return
	 * @throws DMPControllerException
	 */
	private ProxyDataModel updateDataModel(final ProxyDataModel proxyDataModel, final DataModel dataModel, final boolean enableVersioning)
			throws DMPControllerException {

		final ProxyDataModel newProxyDataModel = addConfigurationToDataResource(proxyDataModel, dataModel);

		final DataModel newDataModel = newProxyDataModel.getObject();

		if (newDataModel == null) {

			return proxyDataModel;
		}

		return updateDataModelContent(newProxyDataModel, newDataModel, UpdateFormat.FULL, enableVersioning);
	}

	private ProxyDataModel addConfigurationToDataResource(final ProxyDataModel proxyDataModel, final DataModel dataModel)
			throws DMPControllerException {

		if (dataModel.getConfiguration() == null) {

			final String message = String
					.format("could not add configuration to data resource, because the data model '%s' has no configuration", dataModel.getUuid());

			DataModelsResource.LOG.debug(message);

			return proxyDataModel;
		}

		if (dataModel.getDataResource() == null) {

			final String message = String
					.format("could not add configuration to data resource, because the data model '%s' has no resource", dataModel.getUuid());

			DataModelsResource.LOG.debug(message);

			return proxyDataModel;
		}

		// add configuration to data resource
		dataModel.getDataResource().addConfiguration(dataModel.getConfiguration());

		try {

			final ProxyDataModel proxyUpdatedDataModel = persistenceServiceProvider.get().updateObjectTransactional(dataModel);

			if (proxyUpdatedDataModel == null) {

				final String message = String
						.format("something went wrong, when trying to add configuration '%s' to data resource '%s' of data model '%s'",
								dataModel.getConfiguration().getUuid(), dataModel.getDataResource().getUuid(), dataModel.getUuid());

				DataModelsResource.LOG.error(message);

				throw new DMPControllerException(message);
			}

			final RetrievalType type = proxyDataModel.getType();

			return new ProxyDataModel(proxyUpdatedDataModel.getObject(), type);

		} catch (final DMPPersistenceException e) {

			final String message = String
					.format("something went wrong, when trying to add configuration '%s' to data resource '%s' of data model '%s'",
							dataModel.getConfiguration().getUuid(), dataModel.getDataResource().getUuid(), dataModel.getUuid());

			DataModelsResource.LOG.error(message, e);

			throw new DMPControllerException(message, e);
		}
	}

	private ProxyDataModel updateDataModelContent(final ProxyDataModel proxyDataModel, final DataModel dataModel, final UpdateFormat updateFormat,
			final boolean enableVersioning) throws DMPControllerException {

		// final Timer.Context context = dmpStatus.createNewConfiguration();

		DataModelsResource.LOG.debug("try to process data for data model with id '{}'", dataModel.getUuid());

		final Configuration configuration = dataModel.getConfiguration();

		if (configuration == null) {

			// dmpStatus.stop(context);

			DataModelsResource.LOG.debug("The data model has no configuration. Hence, the data of the data model cannot be processed.");

			return proxyDataModel;
		}

		final JsonNode jsStorageType = configuration.getParameters().get(ConfigurationStatics.STORAGE_TYPE);

		if (jsStorageType != null) {

			final String storageType = jsStorageType.asText();

			try {

				final SchemaEvent.SchemaType type = SchemaEvent.SchemaType.fromString(storageType);
				final SchemaEvent schemaEvent = new SchemaEvent(dataModel, type, updateFormat, enableVersioning);
				schemaEventRecorderProvider.get().convertSchema(schemaEvent);
			} catch (final IllegalArgumentException e) {

				DataModelsResource.LOG.warn("could not determine schema type", e);
			}

			switch (storageType) {
				case ConfigurationStatics.SCHEMA_STORAGE_TYPE:

					// eventBusProvider.get().post(new XMLSchemaEvent(configuration, dataModel.getDataResource()));

					final XMLSchemaEvent xmlSchemaEvent = new XMLSchemaEvent(configuration, dataModel.getDataResource());
					xmlSchemaEventRecorderProvider.get().convertConfiguration(xmlSchemaEvent);

					break;
				case ConfigurationStatics.CSV_STORAGE_TYPE:

					// eventBusProvider.get().post(new CSVConverterEvent(dataModel));

					final CSVConverterEvent csvConverterEvent = new CSVConverterEvent(dataModel, updateFormat, enableVersioning);
					csvConverterEventRecorderProvider.get().convertConfiguration(csvConverterEvent);

					break;
				case ConfigurationStatics.XML_STORAGE_TYPE:
				case ConfigurationStatics.MABXML_STORAGE_TYPE:
				case ConfigurationStatics.MARCXML_STORAGE_TYPE:
				case ConfigurationStatics.PNX_STORAGE_TYPE:
				case ConfigurationStatics.OAI_PMH_DC_ELEMENTS_STORAGE_TYPE:
				case ConfigurationStatics.OAIPMH_DC_TERMS_STORAGE_TYPE:
				case ConfigurationStatics.OAIPMH_MARCXML_STORAGE_TYPE:

					// eventBusProvider.get().post(new XMLConverterEvent(dataModel));

					final XMLConverterEvent xmlConverterEvent = new XMLConverterEvent(dataModel, updateFormat, enableVersioning);
					xmlConvertEventRecorderProvider.get().processDataModel(xmlConverterEvent);

					break;
			}
		}

		// refresh data model
		final DataModelService persistenceService = persistenceServiceProvider.get();
		final DataModel freshDataModel = persistenceService.getObject(dataModel.getUuid());
		final RetrievalType type = proxyDataModel.getType();

		return new ProxyDataModel(freshDataModel, type);
	}

	private void getDataInternal(final String uuid, final Integer atMost, final AsyncResponse asyncResponse) throws DMPControllerException {

		// final Timer.Context context = dmpStatus.getConfigurationData();

		DataModelsResource.LOG.debug("try to get data for data model with uuid '{}'", uuid);

		final Observable<Tuple<String, JsonNode>> data;

		if (atMost != null) {
			data = dataModelUtil
					.getData(uuid, Optional.fromNullable(atMost))
					.limit(atMost);
		} else {
			data = dataModelUtil.getData(uuid, Optional.absent());
		}

		data.subscribe(new StreamingDataObserver(uuid, objectMapperProvider.get(), pojoClassName, asyncResponse));
	}

	private Client client() {

		final ClientBuilder builder = ClientBuilder.newBuilder();

		return builder.register(MultiPartFeature.class).build();
	}

	private WebTarget service() {

		return client().target(graphEndpoint);
	}

	private WebTarget target() {

		return service().path(RDFResource.resourceIdentifier);
	}

	private WebTarget target(final String... path) {

		WebTarget target = target();

		for (final String p : path) {

			target = target.path(p);
		}

		return target;
	}

	private Optional<String> getStringValue(final String key, final JsonNode json) {

		final JsonNode node = json.get(key);
		final Optional<String> optionalValue;

		if (node != null) {

			optionalValue = Optional.fromNullable(node.asText());
		} else {

			optionalValue = Optional.absent();
		}

		return optionalValue;
	}

	private static final class StreamingDataObserver implements Observer<Tuple<String, JsonNode>> {

		private final ObjectMapper  objectMapper;
		private final String        pojoClassName;
		private final AsyncResponse asyncResponse;
		private final String        uuid;
		private final ObjectNode    json;

		private boolean hasData;

		public StreamingDataObserver(final String uuid, final ObjectMapper objectMapper, final String pojoClassName,
				final AsyncResponse asyncResponse) {
			this.uuid = uuid;
			this.objectMapper = objectMapper;
			this.pojoClassName = pojoClassName;
			this.asyncResponse = asyncResponse;
			json = objectMapper.createObjectNode();
		}

		@Override
		public void onCompleted() {

			if (!hasData) {

				DataModelsResource.LOG.debug("couldn't find data for data model with uuid '{}'", uuid);
				asyncResponse.resume(Response.status(Status.NOT_FOUND).build());
			} else {

				try {
					final String jsonString = serializesObject(json, objectMapper, pojoClassName);

					DataModelsResource.LOG.debug("return data for data model with uuid '{}' ", uuid);
					if (DataModelsResource.LOG.isTraceEnabled()) {
						DataModelsResource.LOG.trace("and content '{}'", jsonString);
					}

					asyncResponse.resume(buildResponse(jsonString));

				} catch (final DMPControllerException e) {

					asyncResponse.resume(e);
				}
			}
		}

		@Override
		public void onError(final Throwable e) {
			asyncResponse.resume(e);
		}

		@Override
		public void onNext(final Tuple<String, JsonNode> tuple) {
			hasData = true;
			json.set(tuple.v1(), tuple.v2());
		}
	}

	private static final class ResponseObserver implements Observer<Response> {

		private final String        uuid;
		private final AsyncResponse asyncResponse;

		private Response response;

		public ResponseObserver(final String uuidArg, final AsyncResponse asyncResponse) {

			uuid = uuidArg;
			this.asyncResponse = asyncResponse;
		}

		@Override
		public void onCompleted() {

			if (response == null) {

				DataModelsResource.LOG.debug("couldn't find data model with uuid '{}'", uuid);

				asyncResponse.resume(Response.status(Status.NOT_FOUND).build());
			} else {

				final String body = response.readEntity(String.class);

				asyncResponse.resume(buildResponse(body));
			}
		}

		@Override
		public void onError(final Throwable e) {

			asyncResponse.resume(e);
		}

		@Override
		public void onNext(final Response response) {

			this.response = response;
		}
	}
}
