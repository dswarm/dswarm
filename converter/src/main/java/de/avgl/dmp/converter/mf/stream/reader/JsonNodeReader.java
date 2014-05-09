package de.avgl.dmp.converter.mf.stream.reader;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.Nullable;

import org.culturegraph.mf.framework.DefaultObjectPipe;
import org.culturegraph.mf.framework.StreamReceiver;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import de.avgl.dmp.persistence.model.types.Tuple;

/**
 * @author phorn
 * @author tgaengler
 */
public class JsonNodeReader extends DefaultObjectPipe<Iterator<Tuple<String, JsonNode>>, StreamReceiver> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(JsonNodeReader.class);
	private final Optional<String>					recordPrefix;

	public JsonNodeReader() {
		this(null);
	}

	public JsonNodeReader(@Nullable final String recordPrefix) {

		this.recordPrefix = Optional.fromNullable(recordPrefix);
	}

	@Override
	public void process(final Iterator<Tuple<String, JsonNode>> obj) {
		final StreamReceiver receiver = getReceiver();
		while (obj.hasNext()) {
			final Tuple<String, JsonNode> tuple = obj.next();

			receiver.startRecord(tuple.v1());
			if (recordPrefix.isPresent()) {
				receiver.startEntity(recordPrefix.get());
			}

			try {
				processArrayNode(receiver, tuple.v2());
			} catch (final IOException e) {
				JsonNodeReader.LOG.error(e.getMessage(), e);
			}

			if (recordPrefix.isPresent()) {
				receiver.endEntity();
			}
			receiver.endRecord();
		}
	}

	private void processObjectNode(final StreamReceiver receiver, final JsonNode jsonNode) throws IOException {
		Preconditions.checkArgument(jsonNode.isObject(), "we need an object for a record entry");

		final Iterator<Map.Entry<String, JsonNode>> entryIterator = jsonNode.fields();

		while (entryIterator.hasNext()) {
			final Map.Entry<String, JsonNode> entry = entryIterator.next();

			processNode(receiver, entry.getKey(), entry.getValue(), checkValue(entry.getValue()));
		}
	}

	private void processArrayNode(final StreamReceiver receiver, final JsonNode jsonNode) throws IOException {
		Preconditions.checkArgument(jsonNode.isArray(), "we need an array for a record entry");

		final ArrayNode jsonArray = (ArrayNode) jsonNode;

		final Iterator<JsonNode> elements = jsonArray.elements();

		while (elements.hasNext()) {

			final JsonNode element = elements.next();

			processObjectNode(receiver, element);
		}
	}

	private void processNode(final StreamReceiver receiver, final String fieldName, final JsonNode node, final boolean isEntity) throws IOException {

		// System.out.println("is entity = '" + isEntity + "'");

		switch (node.getNodeType()) {
			case OBJECT:

				// System.out.println("field '" + fieldName + "' value is an object");

				processObjectNode(receiver, node);

				break;

			case ARRAY:

				// System.out.println("field '" + fieldName + "' value is an array");

				if (isEntity) {

					// System.out.println("start entity for field '" + fieldName + "' via array");

					receiver.startEntity(fieldName);
				}

				for (final JsonNode arrayNode : node) {

					processNode(receiver, fieldName, arrayNode, checkValue(arrayNode));
				}

				if (isEntity) {

					// System.out.println("end entity for field '" + fieldName + "' via array");

					receiver.endEntity();
				}

				break;

			case STRING:

				// System.out.println("write literal: key = '" + fieldName + "' + value = '" + node.textValue() + "'");

				receiver.literal(fieldName, node.textValue());
				break;

			case NUMBER:

				// System.out.println("write literal: key = '" + fieldName + "' + value = '" + String.valueOf(node.numberValue())
				// + "'");

				receiver.literal(fieldName, String.valueOf(node.numberValue()));
				break;

			case BINARY:

				// System.out.println("write literal: key = '" + fieldName + "' + value = '" + new String(node.binaryValue(),
				// "UTF-8") + "'");

				receiver.literal(fieldName, new String(node.binaryValue(), "UTF-8"));
				break;

			case BOOLEAN:

				// System.out.println("write literal: key = '" + fieldName + "' + value = '" + String.valueOf(node.booleanValue())
				// + "'");

				receiver.literal(fieldName, String.valueOf(node.booleanValue()));
				break;

			case MISSING: // fall through
			case NULL: // fall through
			case POJO: // fall through
			default:
				break;

		}
	}

	private boolean checkValue(final JsonNode jsonNode) {

		return jsonNode.isArray() && jsonNode.elements().hasNext() && jsonNode.elements().next().isObject();
	}
}
