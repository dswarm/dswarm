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
package org.dswarm.converter.mf.stream.converter;

import java.io.IOException;
import java.io.Reader;
import java.util.Stack;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.culturegraph.mf.exceptions.MetafactureException;
import org.culturegraph.mf.framework.DefaultObjectPipe;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.converter.mf.framework.JsonReceiver;

/**
 * @author tgaengler
 */
@Description("Reads a JSON file and passes the JSON events to a receiver.")
@In(Reader.class)
@Out(JsonReceiver.class)
public class JsonDecoder extends DefaultObjectPipe<Reader, JsonReceiver> {

	private static final Logger LOG = LoggerFactory.getLogger(JsonDecoder.class);

	private static final ObjectMapper MAPPER      = new ObjectMapper();
	private static final JsonFactory  jsonFactory = MAPPER.getFactory();

	private final Stack<String>    fieldNameStack;
	private final Stack<JsonToken> tokenStack;

	public JsonDecoder() {

		super();

		fieldNameStack = new Stack<>();
		tokenStack = new Stack<>();
	}

	@Override
	public void process(final Reader reader) {

		try {

			final JsonParser jp = jsonFactory.createParser(reader);

			jp.nextToken();

			while (jp.hasCurrentToken()) {

				final JsonToken currentToken = jp.getCurrentToken();

				switch (currentToken) {

					case START_ARRAY:

						tokenStack.push(currentToken);

						break;
					case START_OBJECT:

						final String startObjectFieldName1 = getCurrentFieldName();

						getReceiver().startObject(startObjectFieldName1);

						tokenStack.push(currentToken);

						break;
					case END_ARRAY:

						popFieldName();
						tokenStack.pop();

						if (JsonToken.FIELD_NAME.equals(tokenStack.peek())) {

							tokenStack.pop();
						}

						break;
					case END_OBJECT:

						final String endObjectFieldName = getCurrentFieldName();
						getReceiver().endObject(endObjectFieldName);

						tokenStack.pop();

						if (!tokenStack.isEmpty() && JsonToken.FIELD_NAME.equals(tokenStack.peek())) {

							tokenStack.pop();
						}

						break;
					case FIELD_NAME:

						final String currentFieldName = jp.getCurrentName();

						fieldNameStack.push(currentFieldName);
						tokenStack.push(currentToken);

						break;
					case VALUE_FALSE:
						//case VALUE_NULL:
					case VALUE_NUMBER_FLOAT:
					case VALUE_NUMBER_INT:
					case VALUE_STRING:
					case VALUE_TRUE:

						final String currentValue = jp.getValueAsString();
						final String fieldName = getCurrentFieldName();

						getReceiver().literal(fieldName, currentValue);

						if (JsonToken.FIELD_NAME.equals(tokenStack.peek())) {

							popFieldName();
							tokenStack.pop();
						}

						break;
					case VALUE_NULL:

						final String currentValue2 = jp.getValueAsString();
						final String fieldName2 = getCurrentFieldName();

						getReceiver().literal(fieldName2, currentValue2);

						if (JsonToken.FIELD_NAME.equals(tokenStack.peek())) {

							popFieldName();
						}

						tokenStack.pop();

						break;
					default:

						LOG.debug("unhandled JSON token '{}' found", currentToken);
				}

				jp.nextToken();
			}

			jp.close();
		} catch (final IOException e) {

			throw new MetafactureException(e);
		}
	}

	private String getCurrentFieldName() {

		if (!fieldNameStack.isEmpty()) {

			return fieldNameStack.peek();
		}

		return null;
	}

	private String popFieldName() {

		if (!fieldNameStack.isEmpty()) {

			return fieldNameStack.pop();
		}

		return null;
	}
}
