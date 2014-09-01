package org.dswarm.persistence.service.internal.graph;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.graph.json.Resource;
import org.dswarm.graph.json.util.Util;
import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.internal.Model;
import org.dswarm.persistence.model.internal.gdm.GDMModel;
import org.dswarm.persistence.model.internal.helper.AttributePathHelper;
import org.dswarm.persistence.model.proxy.RetrievalType;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.resource.proxy.ProxyDataModel;
import org.dswarm.persistence.model.schema.Attribute;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.Clasz;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.model.schema.proxy.ProxyAttribute;
import org.dswarm.persistence.model.schema.proxy.ProxyAttributePath;
import org.dswarm.persistence.model.schema.proxy.ProxyClasz;
import org.dswarm.persistence.model.schema.proxy.ProxySchema;
import org.dswarm.persistence.model.schema.utils.SchemaUtils;
import org.dswarm.persistence.service.InternalModelService;
import org.dswarm.persistence.service.resource.DataModelService;
import org.dswarm.persistence.service.schema.AttributePathService;
import org.dswarm.persistence.service.schema.AttributeService;
import org.dswarm.persistence.service.schema.ClaszService;
import org.dswarm.persistence.service.schema.SchemaService;
import org.dswarm.persistence.util.DMPPersistenceUtil;
import org.dswarm.persistence.util.GDMUtil;

/**
 * A internal model service implementation for RDF triples.<br/>
 * Currently, the Neo4j database is utilised.
 *
 * @author tgaengler
 */
@Singleton
public class InternalGDMGraphService implements InternalModelService {

	private static final Logger						LOG								= LoggerFactory.getLogger(InternalGDMGraphService.class);

	private static final String						resourceIdentifier				= "gdm";

	/**
	 * The data model persistence service.
	 */
	private final Provider<DataModelService>		dataModelService;

	/**
	 * The schema persistence service.
	 */
	private final Provider<SchemaService>			schemaService;

	/**
	 * The class persistence service.
	 */
	private final Provider<ClaszService>			classService;

	private final Provider<AttributePathService>	attributePathService;

	private final Provider<AttributeService>		attributeService;

	private final String							graphEndpoint;

	/**
	 * /** Creates a new internal triple service with the given data model persistence service, schema persistence service, class
	 * persistence service and the endpoint to access the graph database.
	 *
	 * @param dataModelService the data model persistence service
	 * @param schemaService the schema persistence service
	 * @param classService the class persistence service
	 * @param attributePathService the attribute path persistence service
	 * @param attributeService the attribute persistence service
	 * @param graphEndpointArg the endpoint to access the graph database
	 */
	@Inject
	public InternalGDMGraphService(final Provider<DataModelService> dataModelService, final Provider<SchemaService> schemaService,
			final Provider<ClaszService> classService, final Provider<AttributePathService> attributePathService,
			final Provider<AttributeService> attributeService, @Named("dswarm.db.graph.endpoint") final String graphEndpointArg) {

		this.dataModelService = dataModelService;
		this.schemaService = schemaService;
		this.classService = classService;
		this.attributePathService = attributePathService;
		this.attributeService = attributeService;

		graphEndpoint = graphEndpointArg;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createObject(final Long dataModelId, final Object model) throws DMPPersistenceException {

		if (dataModelId == null) {

			throw new DMPPersistenceException("data model id shouldn't be null");
		}

		if (model == null) {

			throw new DMPPersistenceException("model that should be added to DB shouldn't be null");
		}

		if (!GDMModel.class.isInstance(model)) {

			throw new DMPPersistenceException("this service can only process GDM models");
		}

		final GDMModel gdmModel = (GDMModel) model;

		final org.dswarm.graph.json.Model realModel = gdmModel.getModel();

		if (realModel == null) {

			throw new DMPPersistenceException("real model that should be added to DB shouldn't be null");
		}

		final String resourceGraphURI = GDMUtil.getDataModelGraphURI(dataModelId);

		final DataModel dataModel = addRecordClass(dataModelId, gdmModel.getRecordClassURI());

		final DataModel finalDataModel;

		if (dataModel != null) {

			finalDataModel = dataModel;
		} else {

			finalDataModel = getDataModel(dataModelId);
		}

		if (finalDataModel.getSchema() != null) {

			if (finalDataModel.getSchema().getRecordClass() != null) {

				final String recordClassURI = finalDataModel.getSchema().getRecordClass().getUri();

				final Set<Resource> recordResources = GDMUtil.getRecordResources(recordClassURI, realModel);

				if (recordResources != null) {

					final LinkedHashSet<String> recordURIs = Sets.newLinkedHashSet();

					for (final Resource recordResource : recordResources) {

						recordURIs.add(recordResource.getUri());
					}

					gdmModel.setRecordURIs(recordURIs);
				}
			}
		}

		addAttributePaths(finalDataModel, gdmModel.getAttributePaths());

		writeGDMToDB(realModel, resourceGraphURI);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Optional<Map<String, Model>> getObjects(final Long dataModelId, final Optional<Integer> atMost) throws DMPPersistenceException {

		if (dataModelId == null) {

			throw new DMPPersistenceException("data model id shouldn't be null");
		}

		final String resourceGraphURI = GDMUtil.getDataModelGraphURI(dataModelId);

		// retrieve record class uri from data model schema
		final DataModel dataModel = dataModelService.get().getObject(dataModelId);

		if (dataModel == null) {

			InternalGDMGraphService.LOG.debug("couldn't find data model '" + dataModelId + "' to retrieve record class from");

			throw new DMPPersistenceException("couldn't find data model '" + dataModelId + "' to retrieve record class from");
		}

		final Schema schema = dataModel.getSchema();

		if (schema == null) {

			InternalGDMGraphService.LOG.debug("couldn't find schema in data model '" + dataModelId + "'");

			throw new DMPPersistenceException("couldn't find schema in data model '" + dataModelId + "'");
		}

		final Clasz recordClass = schema.getRecordClass();

		if (recordClass == null) {

			InternalGDMGraphService.LOG.debug("couldn't find record class in schema '" + schema.getId() + "' of data model '" + dataModelId + "'");

			throw new DMPPersistenceException("couldn't find record class in schema '" + schema.getId() + "' of data model '" + dataModelId + "'");
		}

		final String recordClassUri = recordClass.getUri();

		final org.dswarm.graph.json.Model model = readGDMFromDB(recordClassUri, resourceGraphURI);

		if (model == null) {

			InternalGDMGraphService.LOG.debug("couldn't find model for data model '" + dataModelId + "' in database");

			return Optional.absent();
		}

		if (model.size() <= 0) {

			InternalGDMGraphService.LOG.debug("model is empty for data model '" + dataModelId + "' in database");

			return Optional.absent();
		}

		final Set<Resource> recordResources = GDMUtil.getRecordResources(recordClassUri, model);

		if (recordResources == null || recordResources.isEmpty()) {

			InternalGDMGraphService.LOG.debug("couldn't find records for record class'" + recordClassUri + "' in data model '" + dataModelId + "'");

			throw new DMPPersistenceException("couldn't find records for record class'" + recordClassUri + "' in data model '" + dataModelId + "'");
		}

		final Map<String, Model> modelMap = Maps.newLinkedHashMap();

		int i = 0;

		for (final Resource recordResource : recordResources) {

			if (atMost.isPresent()) {

				if (i >= atMost.get()) {

					break;
				}
			}

			final org.dswarm.graph.json.Model recordModel = new org.dswarm.graph.json.Model();
			recordModel.addResource(recordResource);

			final Model rdfModel = new GDMModel(recordModel, recordResource.getUri());

			modelMap.put(recordResource.getUri(), rdfModel);

			i++;
		}

		return Optional.of(modelMap);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteObject(final Long dataModelId) throws DMPPersistenceException {

		if (dataModelId == null) {

			throw new DMPPersistenceException("data model id shouldn't be null");
		}

		final String resourceGraphURI = GDMUtil.getDataModelGraphURI(dataModelId);

		// TODO: delete DataModel object from DB here as well?

		// dataset.begin(ReadWrite.WRITE);
		// dataset.removeNamedModel(resourceGraphURI);
		// dataset.commit();
		// dataset.end();

		// TODO

	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Optional<Schema> getSchema(final Long dataModelId) throws DMPPersistenceException {

		if (dataModelId == null) {

			throw new DMPPersistenceException("data model id shouldn't be null");
		}

		final DataModel dataModel = dataModelService.get().getObject(dataModelId);

		if (dataModel == null) {

			InternalGDMGraphService.LOG.debug("couldn't find data model '" + dataModelId + "' to retrieve it's schema");

			throw new DMPPersistenceException("couldn't find data model '" + dataModelId + "' to retrieve it's schema");
		}

		final Schema schema = dataModel.getSchema();

		if (schema == null) {

			InternalGDMGraphService.LOG.debug("couldn't find schema in data model '" + dataModelId + "'");

			return Optional.absent();
		}

		return Optional.of(schema);
	}

	/**
	 * Adds the record class to the schema of the data model.
	 *
	 * @param dataModelId the identifier of the data model
	 * @param recordClassUri the identifier of the record class
	 * @throws DMPPersistenceException
	 */
	private DataModel addRecordClass(final Long dataModelId, final String recordClassUri) throws DMPPersistenceException {

		// (try) add record class uri to schema
		final DataModel dataModel = getSchemaInternal(dataModelId);
		final Schema schema = dataModel.getSchema();

		final boolean result = SchemaUtils.addRecordClass(schema, recordClassUri, classService);

		if(!result) {

			return dataModel;
		}

		return updateDataModel(dataModel);
	}

	private DataModel addAttributePaths(final DataModel dataModel, final Set<AttributePathHelper> attributePathHelpers)
			throws DMPPersistenceException {

		final Schema schema = dataModel.getSchema();

		if (schema.getId() == 3) {

			// mabxml schema is already there

			return dataModel;
		}

		final boolean result = SchemaUtils.addAttributePaths(schema, attributePathHelpers, attributePathService, attributeService);

		if(!result) {

			return dataModel;
		}

		return updateDataModel(dataModel);
	}

	private DataModel updateDataModel(final DataModel dataModel) throws DMPPersistenceException {
		final ProxyDataModel proxyUpdatedDataModel = dataModelService.get().updateObjectTransactional(dataModel);

		if (proxyUpdatedDataModel == null) {

			throw new DMPPersistenceException("couldn't update data model");
		}

		return proxyUpdatedDataModel.getObject();
	}

	private DataModel getSchemaInternal(final Long dataModelId) throws DMPPersistenceException {

		final DataModel dataModel = getDataModel(dataModelId);

		final Schema schema;

		if (dataModel.getSchema() == null) {

			final Configuration configuration = dataModel.getConfiguration();

			boolean isMabXml = false;

			if (configuration != null) {

				final JsonNode storageTypeJsonNode = configuration.getParameter("storage_type");

				if (storageTypeJsonNode != null) {

					final String storageType = storageTypeJsonNode.asText();

					if (storageType != null && storageType.equals("mabxml")) {

						isMabXml = true;
					}
				}
			}

			if (!isMabXml) {

				// create new schema
				final ProxySchema proxySchema = schemaService.get().createObjectTransactional();

				if (proxySchema != null) {

					schema = proxySchema.getObject();
				} else {

					schema = null;
				}
			} else {

				// assign existing mabxml schema to data resource

				schema = schemaService.get().getObject((long) 3);
			}

			dataModel.setSchema(schema);
		}

		return updateDataModel(dataModel);
	}

	private DataModel getDataModel(final Long dataModelId) {

		final DataModel dataModel = dataModelService.get().getObject(dataModelId);

		if (dataModel == null) {

			InternalGDMGraphService.LOG.debug("couldn't find data model '" + dataModelId + "'");

			return null;
		}

		return dataModel;
	}

	private void writeGDMToDB(final org.dswarm.graph.json.Model model, final String resourceGraphUri) throws DMPPersistenceException {

		final WebTarget target = target("/put");

		final ObjectMapper objectMapper = Util.getJSONObjectMapper();

		byte[] bytes = null;

		try {

			bytes = objectMapper.writeValueAsBytes(model);
		} catch (final JsonProcessingException e) {

			throw new DMPPersistenceException("couldn't serialise model to JSON");
		}

		// Construct a MultiPart with two body parts
		final MultiPart multiPart = new MultiPart();
		multiPart.bodyPart(bytes, MediaType.APPLICATION_OCTET_STREAM_TYPE).bodyPart(resourceGraphUri, MediaType.TEXT_PLAIN_TYPE);

		// POST the request
		final Response response = target.request("multipart/mixed").post(Entity.entity(multiPart, "multipart/mixed"));

		if (response.getStatus() != 200) {

			throw new DMPPersistenceException("Couldn't store GDM data into database. Received status code '" + response.getStatus()
					+ "' from database endpoint.");
		}
	}

	private org.dswarm.graph.json.Model readGDMFromDB(final String recordClassUri, final String resourceGraphUri) throws DMPPersistenceException {

		final WebTarget target = target("/get");

		final ObjectMapper objectMapper = DMPPersistenceUtil.getJSONObjectMapper();
		final ObjectNode requestJson = objectMapper.createObjectNode();

		requestJson.put("record_class_uri", recordClassUri);
		requestJson.put("resource_graph_uri", resourceGraphUri);

		String requestJsonString;

		try {

			requestJsonString = objectMapper.writeValueAsString(requestJson);
		} catch (final JsonProcessingException e) {

			throw new DMPPersistenceException("something went wrong, while creating the request JSON string for the read-gdm-from-db request");
		}

		// POST the request
		final Response response = target.request(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON)
				.post(Entity.entity(requestJsonString, MediaType.APPLICATION_JSON));

		if (response.getStatus() != 200) {

			throw new DMPPersistenceException("Couldn't read GDM data from database. Received status code '" + response.getStatus()
					+ "' from database endpoint.");
		}

		final String body = response.readEntity(String.class);

		final ObjectMapper gdmObjectMapper = Util.getJSONObjectMapper();

		final org.dswarm.graph.json.Model model;

		try {

			model = gdmObjectMapper.readValue(body, org.dswarm.graph.json.Model.class);
		} catch (final JsonParseException e) {

			throw new DMPPersistenceException("something went wrong, while parsing the JSON string");
		} catch (final JsonMappingException e) {

			throw new DMPPersistenceException("something went wrong, while mapping the JSON string");
		} catch (final IOException e) {

			throw new DMPPersistenceException("something went wrong, while processing the JSON string");
		}

		return model;
	}

	private Client client() {

		final ClientBuilder builder = ClientBuilder.newBuilder();

		return builder.register(MultiPartFeature.class).build();
	}

	private WebTarget target() {

		return client().target(graphEndpoint).path(InternalGDMGraphService.resourceIdentifier);
	}

	private WebTarget target(final String... path) {

		WebTarget target = target();

		for (final String p : path) {

			target = target.path(p);
		}

		return target;
	}
}
