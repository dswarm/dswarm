package de.avgl.dmp.persistence.model.internal.rdf.helper;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;

import de.avgl.dmp.persistence.util.DMPPersistenceUtil;


public class ResourceConverterHelper extends ConverterHelper {

	private final List<JsonNode>	objects;

	public ResourceConverterHelper(final String property) {

		super(property);
		objects = Lists.newLinkedList();
	}

	public void addObject(final JsonNode object) {

		objects.add(object);
	}

	public boolean isArray() {

		return objects.size() > 1 ? true : false;
	}

	@Override
	public ObjectNode build(final ObjectNode json) {

		final JsonNode node;

		if (isArray()) {

			final ArrayNode arrayNode = DMPPersistenceUtil.getJSONObjectMapper().createArrayNode();

			for (final JsonNode object : objects) {

				arrayNode.add(object);

			}

			node = arrayNode;
		} else {

			node = objects.get(0);
		}

		json.put(property, node);

		return json;
	}
}
