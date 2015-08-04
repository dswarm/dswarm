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

	private final Stack<String> fieldNameStack;

	public JsonDecoder() {

		super();

		fieldNameStack = new Stack<>();
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

						final String startArrayFieldName = getCurrentFieldName();
						getReceiver().startArray(startArrayFieldName);

						break;
					case START_OBJECT:

						final String startObjectFieldName1 = getCurrentFieldName();
						getReceiver().startObject(startObjectFieldName1);

						break;
					case END_ARRAY:

						final String endArrayFieldName = popFieldName();
						getReceiver().startArray(endArrayFieldName);

						break;
					case END_OBJECT:

						final String endObjectFieldName = popFieldName();
						getReceiver().startObject(endObjectFieldName);

						break;
					case FIELD_NAME:

						final String currentFieldName = jp.getCurrentName();

						fieldNameStack.push(currentFieldName);

						break;
					case VALUE_FALSE:
					case VALUE_NULL:
					case VALUE_NUMBER_FLOAT:
					case VALUE_NUMBER_INT:
					case VALUE_STRING:
					case VALUE_TRUE:

						final String currentValue = jp.getValueAsString();
						final String fieldName = fieldNameStack.pop();

						getReceiver().literal(fieldName, currentValue);

						break;
					default:

						// TODO: throw an exception (?)

						LOG.debug("unhandled JSON token '{}' found", currentToken);

						System.out.println("unhandled JSON token '" + currentToken + "' found");
				}

				jp.nextToken();
			}

			jp.close();
		} catch (final IOException e) {

			throw new MetafactureException(e);
		}
	}

	private String getCurrentFieldName() {

		if(!fieldNameStack.isEmpty()) {

			return fieldNameStack.peek();
		}

		return null;
	}

	private String popFieldName() {

		if(!fieldNameStack.isEmpty()) {

			return fieldNameStack.pop();
		}

		return null;
	}
}
