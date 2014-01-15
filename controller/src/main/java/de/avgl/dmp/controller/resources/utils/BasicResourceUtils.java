package de.avgl.dmp.controller.resources.utils;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Sets;

import de.avgl.dmp.controller.DMPControllerException;
import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.DMPObject;
import de.avgl.dmp.persistence.service.BasicJPAService;

/**
 * @author tgaengler
 * @param <POJOCLASSPERSISTENCESERVICE>
 * @param <POJOCLASS>
 * @param <POJOCLASSIDTYPE>
 */
public abstract class BasicResourceUtils<POJOCLASSPERSISTENCESERVICE extends BasicJPAService<POJOCLASS, POJOCLASSIDTYPE>, POJOCLASS extends DMPObject<POJOCLASSIDTYPE>, POJOCLASSIDTYPE> {

	private static final org.apache.log4j.Logger			LOG						= org.apache.log4j.Logger.getLogger(BasicResourceUtils.class);

	protected final Class<POJOCLASS>						pojoClass;

	protected final Class<POJOCLASSIDTYPE>					pojoClassIdType;

	protected final String									pojoClassName;

	protected final Provider<POJOCLASSPERSISTENCESERVICE>	persistenceServiceProvider;

	protected final Provider<ObjectMapper>					objectMapperProvider;

	// TODO: this might be not the best solution ...
	protected static final Set<String>						toBeSkippedJsonNodes	= Sets.newHashSet();

	static {

		// add here all identifiers for attributes that bear native JSON objects/arrays

		toBeSkippedJsonNodes.add("parameters");
		toBeSkippedJsonNodes.add("attributes");
		toBeSkippedJsonNodes.add("parameter_mappings");
		toBeSkippedJsonNodes.add("function_description");
	}

	public BasicResourceUtils(final Class<POJOCLASS> pojoClassArg, final Class<POJOCLASSIDTYPE> pojoClassIdTypeArg,
			final Provider<POJOCLASSPERSISTENCESERVICE> persistenceServiceProviderArg, final Provider<ObjectMapper> objectMapperProviderArg) {

		persistenceServiceProvider = persistenceServiceProviderArg;

		objectMapperProvider = objectMapperProviderArg;

		pojoClass = pojoClassArg;
		pojoClassName = pojoClass.getSimpleName();

		pojoClassIdType = pojoClassIdTypeArg;
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
	
	public static Set<String> getToBeSkippedJsonNodes() {
		
		return toBeSkippedJsonNodes;
	}

	public ObjectNode replaceRelevantDummyIds(final POJOCLASS object, final ObjectNode objectJSON, final Set<POJOCLASSIDTYPE> dummyIdCandidates)
			throws DMPControllerException {

		if (dummyIdCandidates == null || dummyIdCandidates.isEmpty()) {

			return objectJSON;
		}

		if (dummyIdCandidates.contains(object.getId())) {

			final POJOCLASS newObject = createObject();
			
			dummyIdCandidates.remove(object.getId());

			return replaceDummyIdInObjectJSON(objectJSON, object.getId(), newObject.getId());
		}

		// TODO: recursion in concrete pojo class resource utils

		return objectJSON;
	}

	public POJOCLASS createObject() throws DMPControllerException {

		try {

			return persistenceServiceProvider.get().createObject();
		} catch (final DMPPersistenceException e) {

			BasicResourceUtils.LOG.debug("something went wrong while " + pojoClassName + " creation");

			throw new DMPControllerException("something went wrong while " + pojoClassName + " creation\n" + e.getMessage());
		}
	}

	public List<POJOCLASS> getObjects() {

		return persistenceServiceProvider.get().getObjects();
	}
	
	protected abstract ObjectNode replaceDummyId(final JsonNode idNode, final POJOCLASSIDTYPE dummyId, final POJOCLASSIDTYPE realId, final ObjectNode objectJson);

	public void deleteObject(final POJOCLASSIDTYPE id) {

		if (id != null) {

			final POJOCLASSIDTYPE objectId = id;

			persistenceServiceProvider.get().deleteObject(objectId);
		}
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

	private JsonNode replaceDummyIdInJsonNode(final JsonNode jsonNode, final POJOCLASSIDTYPE dummyId, final POJOCLASSIDTYPE realId) {

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
}
