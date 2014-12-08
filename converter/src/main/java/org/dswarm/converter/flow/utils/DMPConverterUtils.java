package org.dswarm.converter.flow.utils;

import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.dswarm.persistence.util.DMPPersistenceUtil;

/**
 * @author tgaengler
 */
public class DMPConverterUtils {

	public static JsonNode removeRecordIdFields(final JsonNode jsonNode) {

		if (!jsonNode.isContainerNode()) {

			return jsonNode;
		}

		if (jsonNode.isArray()) {

			final ArrayNode arrayNode = DMPPersistenceUtil.getJSONObjectMapper().createArrayNode();

			for (final JsonNode entry : jsonNode) {

				final JsonNode manipulatedJSON = removeRecordIdFields(entry);
				arrayNode.add(manipulatedJSON);
			}

			return arrayNode;
		}

		final JsonNode cleanedJSON = ((ObjectNode) jsonNode).without(DMPPersistenceUtil.RECORD_ID);

		final Iterator<Map.Entry<String, JsonNode>> iter = cleanedJSON.fields();

		final ObjectNode newJSON = DMPPersistenceUtil.getJSONObjectMapper().createObjectNode();

		while(iter.hasNext()) {

			final Map.Entry<String, JsonNode> entry = iter.next();

			final JsonNode result = removeRecordIdFields(entry.getValue());

			newJSON.set(entry.getKey(), result);
		}

		return newJSON;
	}
}
