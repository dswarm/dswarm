package de.avgl.dmp.persistence.model.internal.helper;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;

import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

/**
 * A converter helper for RDF to JSON transformation.
 * 
 * @author tgaengler
 */
public class ConverterHelper {

	/**
	 * The property of this converter helper.
	 */
	protected final String		property;

	/**
	 * The objects of this converter helper.
	 */
	private final List<Object>	objects;

	/**
	 * Creates a new converter helper with the given property.
	 * 
	 * @param property a property
	 */
	public ConverterHelper(final String property) {

		this.property = property;
		objects = Lists.newLinkedList();
	}

	/**
	 * Adds a literal or URI to the object list.
	 * 
	 * @param object a new literal or URI
	 */
	public void addLiteralOrURI(final String object) {

		objects.add(new Object(object));
	}

	/**
	 * Adds a JSON node to the object list.
	 * 
	 * @param jsonNode a JSON node
	 */
	public void addJsonNode(final JsonNode jsonNode) {

		objects.add(new Object(jsonNode));
	}

	/**
	 * Return true, if the object list consists of more than one object; otherwise false.
	 * 
	 * @return true, if the object list consists of more than one object; otherwise false
	 */
	public boolean isArray() {

		return objects.size() > 1;
	}

	/**
	 * Serialises the property + object list to a JSON array.
	 * 
	 * @param json the JSON array that should be filled
	 * @return the filled JSON array
	 */
	public ArrayNode build(final ArrayNode json) {

		final ObjectNode objectJSONObject = DMPPersistenceUtil.getJSONObjectMapper().createObjectNode();

		if (isArray()) {

			final ArrayNode arrayNode = DMPPersistenceUtil.getJSONObjectMapper().createArrayNode();

			for (final Object object : objects) {

				if (object.isJsonNode()) {

					arrayNode.add(object.getJsonNode());

					continue;
				}

				arrayNode.add(object.getLiteralOrURI());
			}

			objectJSONObject.put(property, arrayNode);
		} else {

			final Object object = objects.get(0);

			if (object.isJsonNode()) {

				objectJSONObject.put(property, object.getJsonNode());
			} else {

				objectJSONObject.put(property, object.getLiteralOrURI());
			}
		}

		json.add(objectJSONObject);

		return json;
	}

}
