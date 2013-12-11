package de.avgl.dmp.converter.mf.stream.reader;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.Nullable;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Optional;
import org.culturegraph.mf.framework.DefaultObjectPipe;
import org.culturegraph.mf.framework.StreamReceiver;

import de.avgl.dmp.persistence.model.types.Tuple;

import static com.google.common.base.Preconditions.checkArgument;

public class JsonNodeReader extends DefaultObjectPipe<Iterator<Tuple<String,JsonNode>>, StreamReceiver> {

	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(JsonNodeReader.class);
	private final Optional<String> recordPrefix;

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
				processObjectNode(receiver, tuple.v2());
			} catch (final IOException e) {
				LOG.error(e.getMessage(), e);
			}

			if (recordPrefix.isPresent()) {
				receiver.endEntity();
			}
			receiver.endRecord();
		}
	}


	private void processObjectNode(final StreamReceiver receiver, final JsonNode jsonNode) throws IOException {
		checkArgument(jsonNode.isObject(), "we need an object for a record entry");

		final Iterator<Map.Entry<String, JsonNode>> entryIterator = jsonNode.fields();

		while (entryIterator.hasNext()) {
			final Map.Entry<String, JsonNode> entry = entryIterator.next();

			processNode(receiver, entry.getKey(), entry.getValue());
		}
	}

	private void processNode(final StreamReceiver receiver, final String fieldName, final JsonNode node) throws IOException {
		switch (node.getNodeType()) {
			case OBJECT:
				receiver.startEntity(fieldName);
				processObjectNode(receiver, node);
				receiver.endEntity();
				break;

			case ARRAY:
				for (final JsonNode arrayNode : node) {
					processNode(receiver, fieldName, arrayNode);
				}

				break;

			case STRING:
				receiver.literal(fieldName, node.textValue());
				break;

			case NUMBER:
				receiver.literal(fieldName, String.valueOf(node.numberValue()));
				break;

			case BINARY:
				receiver.literal(fieldName, new String(node.binaryValue(), "UTF-8"));
				break;

			case BOOLEAN:
				receiver.literal(fieldName, String.valueOf(node.booleanValue()));
				break;

			case MISSING:		// fall through
			case NULL:			// fall through
			case POJO:			// fall through
			default:
				break;

		}
	}
}
