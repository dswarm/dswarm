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

						getReceiver().startArray(fieldNameStack.peek());

						break;
					case START_OBJECT:

						getReceiver().startObject(fieldNameStack.peek());

						break;
					case END_ARRAY:

						getReceiver().startArray(fieldNameStack.pop());

						break;
					case END_OBJECT:

						getReceiver().startObject(fieldNameStack.pop());

						break;
					case FIELD_NAME:

						fieldNameStack.push(jp.getCurrentName());

						break;
					case VALUE_FALSE:
					case VALUE_NULL:
					case VALUE_NUMBER_FLOAT:
					case VALUE_NUMBER_INT:
					case VALUE_STRING:
					case VALUE_TRUE:

						final String currentValue = jp.getValueAsString();

						getReceiver().literal(fieldNameStack.pop(), currentValue);

						break;
					default:

						// TODO: throw an exception (?)

						LOG.debug("unhandled JSON token '{}' found", currentToken);
				}

				jp.nextToken();
			}

			jp.close();
		} catch (final IOException e) {

			throw new MetafactureException(e);
		}
	}
}
