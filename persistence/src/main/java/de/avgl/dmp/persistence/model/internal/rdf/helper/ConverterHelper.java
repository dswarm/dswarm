package de.avgl.dmp.persistence.model.internal.rdf.helper;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;

import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

public class ConverterHelper {

	protected final String		property;

	private final List<Object>	objects;

	public ConverterHelper(final String property) {

		this.property = property;
		objects = Lists.newLinkedList();
	}

	public void addLiteralOrURI(final String object) {

		objects.add(new Object(object));
	}

	public void addJsonNode(final JsonNode jsonNode) {

		objects.add(new Object(jsonNode));
	}

	public boolean isArray() {

		return objects.size() > 1 ? true : false;
	}

	public ObjectNode build(final ObjectNode json) {

		if (isArray()) {

			final ArrayNode arrayNode = DMPPersistenceUtil.getJSONObjectMapper().createArrayNode();

			for (final Object object : objects) {

				if (object.isJsonNode()) {

					arrayNode.add(object.getJsonNode());

					continue;
				}

				arrayNode.add(object.getLiteralOrURI());
			}

			json.put(property, arrayNode);
		} else {

			final Object object = objects.get(0);

			if (object.isJsonNode()) {

				json.put(property, object.getJsonNode());
			}

			json.put(property, object.getLiteralOrURI());
		}

		return json;
	}

}
