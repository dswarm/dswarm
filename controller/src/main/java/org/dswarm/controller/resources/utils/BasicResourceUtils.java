package org.dswarm.controller.resources.utils;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.controller.DMPControllerException;
import org.dswarm.controller.DMPJsonException;
import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.DMPObject;
import org.dswarm.persistence.model.proxy.ProxyDMPObject;
import org.dswarm.persistence.service.BasicJPAService;

/**
 * @author tgaengler
 * @param <POJOCLASSPERSISTENCESERVICE>
 * @param <POJOCLASS>
 * @param <POJOCLASSIDTYPE>
 */
public abstract class BasicResourceUtils<POJOCLASSPERSISTENCESERVICE extends BasicJPAService<PROXYPOJOCLASS, POJOCLASS, POJOCLASSIDTYPE>, PROXYPOJOCLASS extends ProxyDMPObject<POJOCLASS, POJOCLASSIDTYPE>, POJOCLASS extends DMPObject<POJOCLASSIDTYPE>, POJOCLASSIDTYPE> {

	private static final Logger								LOG						= LoggerFactory.getLogger(BasicResourceUtils.class);

	protected final Class<POJOCLASS>						pojoClass;

	protected final Class<POJOCLASSIDTYPE>					pojoClassIdType;

	protected final String									pojoClassName;

	protected final Provider<POJOCLASSPERSISTENCESERVICE>	persistenceServiceProvider;

	protected final Provider<ObjectMapper>					objectMapperProvider;

	protected final ResourceUtilsFactory					utilsFactory;

	// TODO: this might be not the best solution ...
	protected final Set<String>								toBeSkippedJsonNodes	= Sets.newHashSet();

	private Set<POJOCLASSIDTYPE>							processedObjectIds;

	private Set<POJOCLASSIDTYPE>							dummyIdCandidates;

	public BasicResourceUtils(final Class<POJOCLASS> pojoClassArg, final Class<POJOCLASSIDTYPE> pojoClassIdTypeArg,
			final Provider<POJOCLASSPERSISTENCESERVICE> persistenceServiceProviderArg, final Provider<ObjectMapper> objectMapperProviderArg,
			final ResourceUtilsFactory utilsFactoryArg) {

		persistenceServiceProvider = persistenceServiceProviderArg;

		objectMapperProvider = objectMapperProviderArg;

		utilsFactory = utilsFactoryArg;

		pojoClass = pojoClassArg;
		pojoClassName = pojoClass.getSimpleName();

		pojoClassIdType = pojoClassIdTypeArg;

		// add here all identifiers for attributes that bear native JSON objects/arrays

		toBeSkippedJsonNodes.add("resource_attributes");
		toBeSkippedJsonNodes.add("parameters");
		toBeSkippedJsonNodes.add("function_description");
		toBeSkippedJsonNodes.add("parameter_mappings");
	}

	/**
	 * Gets the concrete POJO class of this resource (controller service).
	 * 
	 * @return the concrete POJO class
	 */
	public Class<POJOCLASS> getClasz() {

		return pojoClass;
	}

	public String getClaszName() {

		return pojoClassName;
	}

	public Class<POJOCLASSIDTYPE> getIdType() {

		return pojoClassIdType;
	}

	public ObjectMapper getObjectMapper() {

		return objectMapperProvider.get();
	}

	public POJOCLASSPERSISTENCESERVICE getPersistenceService() {

		return persistenceServiceProvider.get();
	}

	public Set<String> getToBeSkippedJsonNodes() {

		return toBeSkippedJsonNodes;
	}

	public JsonNode replaceRelevantDummyIds(final POJOCLASS object, final JsonNode jsonNode, final Set<POJOCLASSIDTYPE> dummyIdCandidates)
			throws DMPControllerException {

		if (hasObjectAlreadyBeenProcessed(object.getId())) {

			return jsonNode;
		}

		if (areDummyIdCandidatesEmpty(dummyIdCandidates)) {

			addProcessedObjectId(object.getId());

			return jsonNode;
		}

		if (dummyIdCandidates.contains(object.getId())) {

			return createNewObjectForDummyId(object, jsonNode, dummyIdCandidates);
		}

		addProcessedObjectId(object.getId());

		return jsonNode;
	}

	public List<POJOCLASS> getObjects() {

		return persistenceServiceProvider.get().getObjects();
	}

	public void deleteObject(final POJOCLASSIDTYPE id) {

		if (id != null) {

			final POJOCLASSIDTYPE objectId = id;

			persistenceServiceProvider.get().deleteObject(objectId);
		}
	}

	public POJOCLASS deserializeObjectJSONString(final String objectJSONString) throws DMPControllerException {

		POJOCLASS objectFromJSON = null;

		try {

			objectFromJSON = objectMapperProvider.get().readValue(objectJSONString, pojoClass);
		} catch (final JsonMappingException je) {

			throw new DMPJsonException("something went wrong while deserializing the " + pojoClassName + " JSON string", je);
		} catch (final IOException e) {

			BasicResourceUtils.LOG.debug("something went wrong while deserializing the " + pojoClassName + " JSON string");

			throw new DMPControllerException("something went wrong while deserializing the " + pojoClassName + " JSON string.\n" + e.getMessage());
		}

		if (objectFromJSON == null) {

			throw new DMPControllerException("deserialized " + pojoClassName + " is null");
		}

		return objectFromJSON;
	}

	public String serializeObject(final Object object) throws DMPControllerException {

		String objectJSONString = null;

		try {

			objectJSONString = objectMapperProvider.get().writeValueAsString(object);
		} catch (final JsonProcessingException e) {

			BasicResourceUtils.LOG.debug("couldn't serialize enhanced " + pojoClassName + " JSON.");

			throw new DMPControllerException("couldn't serialize enhanced " + pojoClassName + " JSON.\n" + e.getMessage());
		}

		if (objectJSONString == null) {

			BasicResourceUtils.LOG.debug("couldn't serialize enhanced " + pojoClassName + " JSON correctly.");

			throw new DMPControllerException("couldn't serialize enhanced " + pojoClassName + " JSON correctly.\n");
		}

		return objectJSONString;
	}

	public String prepareObjectJSONString(final String objectJSONString) throws DMPControllerException {

		if (objectJSONString == null) {

			BasicResourceUtils.LOG.debug("the " + pojoClassName + " JSON string shouldn't be null");

			throw new DMPControllerException("the " + pojoClassName + " JSON string shouldn't be null");
		}

		if (objectJSONString.trim().isEmpty()) {

			BasicResourceUtils.LOG.debug("the " + pojoClassName + " JSON string shouldn't be empty");

			throw new DMPControllerException("the " + pojoClassName + " JSON string shouldn't be empty");
		}

		final Class<? extends JsonNode> jsonClasz;

		if (objectJSONString.trim().startsWith("{")) {

			jsonClasz = ObjectNode.class;
		} else if (objectJSONString.trim().startsWith("[")) {

			jsonClasz = ArrayNode.class;
		} else {

			BasicResourceUtils.LOG.debug("the " + pojoClassName + " JSON string doesn't starts with '{' or '['");

			throw new DMPControllerException("the " + pojoClassName + " JSON string doesn't starts with '{' or '['");
		}

		final JsonNode jsonNode;

		try {

			jsonNode = getObjectMapper().readValue(objectJSONString, jsonClasz);
		} catch (final JsonMappingException je) {

			throw new DMPJsonException("something went wrong while deserializing the " + pojoClassName + " JSON string", je);
		} catch (final IOException e) {

			BasicResourceUtils.LOG.debug("something went wrong while deserializing the " + pojoClassName + " JSON string");

			throw new DMPControllerException("something went wrong while deserializing the " + pojoClassName + " JSON string.\n" + e.getMessage());
		}

		if (jsonNode == null) {

			throw new DMPControllerException("deserialized " + pojoClassName + " is null");
		}

		enhanceJSONNode(jsonNode);

		final String enhancedObjectJSONString = serializeObject(jsonNode);

		final POJOCLASS object = deserializeObjectJSONString(enhancedObjectJSONString);

		// mint real ids for already set dummy ids
		replaceRelevantDummyIds(object, jsonNode, dummyIdCandidates);

		return serializeObject(jsonNode);
	}

	/**
	 * Creates and persists a new object into the database.
	 * 
	 * @param objectFromJSON the new object
	 * @param persistenceService the related persistence service
	 * @return the persisted object
	 * @throws DMPPersistenceException
	 */
	public PROXYPOJOCLASS createObject(final POJOCLASS objectFromJSON, final POJOCLASSPERSISTENCESERVICE persistenceService)
			throws DMPPersistenceException {

		return persistenceService.createObjectTransactional();
	}

	public PROXYPOJOCLASS createNewObject(final POJOCLASS object) throws DMPControllerException {

		PROXYPOJOCLASS newObject = null;

		try {

			newObject = createObject(object, persistenceServiceProvider.get());
		} catch (final DMPPersistenceException e) {

			BasicResourceUtils.LOG.debug("something went wrong while " + pojoClassName + " creation");

			throw new DMPControllerException("something went wrong while " + pojoClassName + " creation\n" + e.getMessage());
		}

		if (newObject == null) {

			throw new DMPControllerException("couldn't create new persistent " + pojoClassName + " for '" + object.toString() + "'");
		}

		return newObject;
	}

	public JsonNode processDummyId(final JsonNode jsonNode, final POJOCLASSIDTYPE objectId, final POJOCLASSIDTYPE newObjectId,
			final Set<POJOCLASSIDTYPE> dummyIdCandidates) {

		final JsonNode enhancedJsonNode = replaceDummyIdInJsonNode(jsonNode, objectId, newObjectId);

		// remove processed dummy id

		dummyIdCandidates.remove(objectId);

		addProcessedObjectId(objectId);

		return enhancedJsonNode;
	}

	public boolean hasObjectAlreadyBeenProcessed(final POJOCLASSIDTYPE objectId) {

		return processedObjectIds != null && processedObjectIds.contains(objectId);

	}

	protected abstract ObjectNode replaceDummyId(final JsonNode idNode, final POJOCLASSIDTYPE dummyId, final POJOCLASSIDTYPE realId,
			final ObjectNode objectJson);

	protected boolean areDummyIdCandidatesEmpty(final Set<POJOCLASSIDTYPE> dummyIdCandidates) {

		return dummyIdCandidates == null || dummyIdCandidates.isEmpty();

	}

	protected boolean checkObject(final POJOCLASS object, final Set<POJOCLASSIDTYPE> dummyIdCandidates) {

		return hasObjectAlreadyBeenProcessed(object.getId()) || areDummyIdCandidatesEmpty(dummyIdCandidates);

	}

	protected JsonNode createNewObjectForDummyId(final POJOCLASS object, final JsonNode jsonNode, final Set<POJOCLASSIDTYPE> dummyIdCandidates)
			throws DMPControllerException {

		final PROXYPOJOCLASS proxyNewObject = createNewObject(object);

		if (proxyNewObject == null) {

			throw new DMPControllerException("couldn't create new " + pojoClassName + " for dummy id '" + object.getId() + "'");
		}

		final POJOCLASS newObject = proxyNewObject.getObject();

		if (newObject == null) {

			throw new DMPControllerException("couldn't create new " + pojoClassName + " for dummy id '" + object.getId() + "'");
		}

		final JsonNode enhancedJsonNode = processDummyId(jsonNode, object.getId(), newObject.getId(), dummyIdCandidates);

		return enhancedJsonNode;
	}

	protected JsonNode enhanceJSONNode(final JsonNode jsonNode) {

		if (jsonNode == null || NullNode.class.isInstance(jsonNode)) {

			return jsonNode;
		}

		if (ObjectNode.class.isInstance(jsonNode)) {

			final ObjectNode objectNode = (ObjectNode) jsonNode;

			enhanceObjectJSON(objectNode);
		} else if (ArrayNode.class.isInstance(jsonNode)) {

			final ArrayNode arrayNode = (ArrayNode) jsonNode;

			for (final JsonNode entryNode : arrayNode) {

				enhanceJSONNode(entryNode);
			}
		}

		return jsonNode;
	}

	protected abstract void checkObjectId(final JsonNode idNode);

	protected abstract ObjectNode addDummyId(final ObjectNode objectJSON);

	protected JsonNode enhanceObjectJSON(final ObjectNode objectJSON) {

		final JsonNode idNode = objectJSON.get("id");

		if (idNode == null || NullNode.class.isInstance(idNode)) {

			// add dummy id to object, if it hasn't one before

			addDummyId(objectJSON);
		} else {

			// check id and add it to the dummy id candidates, if it is a dummy id

			checkObjectId(idNode);
		}

		final Iterator<Entry<String, JsonNode>> iter = objectJSON.fields();

		while (iter.hasNext()) {

			final Entry<String, JsonNode> nodeEntry = iter.next();

			if (toBeSkippedJsonNodes.contains(nodeEntry.getKey())) {

				// TODO: note this still may pick up ids from resource->attributes etc., because those keys are currently not
				// included in upper object to-be-skipped-nodes

				// skip such nodes

				continue;
			}

			enhanceJSONNode(nodeEntry.getValue());
		}

		return objectJSON;
	}

	protected void addDummyIdCandidate(final POJOCLASSIDTYPE dummyId) {

		if (dummyIdCandidates == null) {

			dummyIdCandidates = Sets.newCopyOnWriteArraySet();
		}

		dummyIdCandidates.add(dummyId);
	}

	protected JsonNode replaceDummyIdInJsonNode(final JsonNode jsonNode, final POJOCLASSIDTYPE dummyId, final POJOCLASSIDTYPE realId) {

		if (jsonNode == null || NullNode.class.isInstance(jsonNode)) {

			return jsonNode;
		}

		if (ObjectNode.class.isInstance(jsonNode)) {

			final ObjectNode objectNode = (ObjectNode) jsonNode;

			replaceDummyIdInObjectJSON(objectNode, dummyId, realId);
		} else if (ArrayNode.class.isInstance(jsonNode)) {

			final ArrayNode arrayNode = (ArrayNode) jsonNode;

			for (final JsonNode entryNode : arrayNode) {

				replaceDummyIdInJsonNode(entryNode, dummyId, realId);
			}
		}

		return jsonNode;
	}

	protected void addProcessedObjectId(final POJOCLASSIDTYPE objectId) {

		if (processedObjectIds == null) {

			processedObjectIds = Sets.newCopyOnWriteArraySet();
		}

		processedObjectIds.add(objectId);
	}

	private ObjectNode replaceDummyIdInObjectJSON(final ObjectNode objectJSON, final POJOCLASSIDTYPE dummyId, final POJOCLASSIDTYPE realId) {

		final JsonNode idNode = objectJSON.get("id");

		if (idNode != null) {

			replaceDummyId(idNode, dummyId, realId, objectJSON);
		}

		final Iterator<Entry<String, JsonNode>> iter = objectJSON.fields();

		while (iter.hasNext()) {

			final Entry<String, JsonNode> nodeEntry = iter.next();

			if (toBeSkippedJsonNodes.contains(nodeEntry.getKey())) {

				// skip such nodes

				continue;
			}

			replaceDummyIdInJsonNode(nodeEntry.getValue(), dummyId, realId);
		}

		return objectJSON;
	}
}
