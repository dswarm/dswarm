/**
 * Copyright (C) 2013 – 2017 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dswarm.converter.mf.stream.reader;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import javaslang.Tuple2;
import org.culturegraph.mf.framework.DefaultObjectPipe;
import org.culturegraph.mf.framework.StreamReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author phorn
 * @author tgaengler
 */
public class JsonNodeReader extends DefaultObjectPipe<Tuple2<String, JsonNode>, StreamReceiver> {

	private static final Logger		LOG	= LoggerFactory.getLogger(JsonNodeReader.class);
	private final Optional<String>	recordPrefix;
	private final AtomicInteger counter = new AtomicInteger(0);

	public JsonNodeReader() {
		this(null);
	}

	public JsonNodeReader(@Nullable final String recordPrefix) {

		this.recordPrefix = Optional.fromNullable(recordPrefix);
	}

	public AtomicInteger getCounter() {

		return counter;
	}

	@Override
	public void process(final Tuple2<String, JsonNode> tuple) {

		counter.incrementAndGet();

		final StreamReceiver receiver = getReceiver();

		receiver.startRecord(tuple._1);
		if (recordPrefix.isPresent()) {
			receiver.startEntity(recordPrefix.get());
		}

		try {
			processArrayNode(receiver, tuple._2);
		} catch (final IOException e) {
			JsonNodeReader.LOG.error(e.getMessage(), e);
		}

		if (recordPrefix.isPresent()) {
			receiver.endEntity();
		}
		receiver.endRecord();
	}

	private void processObjectNode(final StreamReceiver receiver,
	                               final JsonNode jsonNode) throws IOException {

		Preconditions.checkArgument(jsonNode.isObject(), "we need an object for a record entry");

		final Iterator<Map.Entry<String, JsonNode>> entryIterator = jsonNode.fields();

		while (entryIterator.hasNext()) {
			final Map.Entry<String, JsonNode> entry = entryIterator.next();

			processNode(receiver, entry.getKey(), entry.getValue(), checkValue(entry.getValue()));
		}
	}

	private void processArrayNode(final StreamReceiver receiver,
	                              final JsonNode jsonNode) throws IOException {

		Preconditions.checkArgument(jsonNode.isArray(), "we need an array for a record entry");

		final ArrayNode jsonArray = (ArrayNode) jsonNode;

		final Iterator<JsonNode> elements = jsonArray.elements();

		while (elements.hasNext()) {

			final JsonNode element = elements.next();

			processObjectNode(receiver, element);
		}
	}

	private void processNode(final StreamReceiver receiver,
	                         final String fieldName,
	                         final JsonNode node,
	                         final boolean isEntity) throws IOException {

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
