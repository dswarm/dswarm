package de.avgl.dmp.persistence.model.internal.rdf.helper;

import java.util.List;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;

import de.avgl.dmp.persistence.util.DMPPersistenceUtil;


public class LiteralConverterHelper extends ConverterHelper {

	private final List<String>	objects;

	public LiteralConverterHelper(final String property) {

		super(property);
		objects = Lists.newLinkedList();
	}

	public void addLiteral(final String object) {

		objects.add(object);
	}

	public boolean isArray() {

		return objects.size() > 1 ? true : false;
	}

	@Override
	public ObjectNode build(final ObjectNode json) {

		if (isArray()) {

			final ArrayNode arrayNode = DMPPersistenceUtil.getJSONObjectMapper().createArrayNode();

			for (final String object : objects) {

				arrayNode.add(object);

			}

			json.put(property, arrayNode);
		} else {

			json.put(property, objects.get(0));
		}

		return json;
	}
}
