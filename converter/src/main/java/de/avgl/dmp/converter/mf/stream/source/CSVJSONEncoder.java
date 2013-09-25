package de.avgl.dmp.converter.mf.stream.source;

import java.util.List;

import org.culturegraph.mf.exceptions.MetafactureException;
import org.culturegraph.mf.framework.DefaultStreamPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;

import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

/**
 * Serialises an object as JSON. Records and entities are represented as objects unless their name ends with []. If the name ends
 * with [], an array is created.
 * 
 * @author tgaengler
 */
@Description("Serialises an object as JSON")
@In(StreamReceiver.class)
@Out(JsonNode.class)
public final class CSVJSONEncoder extends DefaultStreamPipe<ObjectReceiver<JsonNode>> {

	private List<String>	header					= null;
	private List<String>	values					= null;
	private ArrayNode		schemaJSON				= null;
	private ObjectNode		dataJSON				= null;
	private ObjectNode		json					= null;

	private boolean			withHeader				= false;
	private boolean			firstLine				= false;
	private boolean			firstLineInitialized	= false;

	public CSVJSONEncoder() {

	}

	@Override
	public void startRecord(final String id) {

		if (firstLine) {

			firstLine = false;
		}

		if (firstLineInitialized == false) {

			firstLine = true;
			firstLineInitialized = true;

			if (withHeader) {

				header = Lists.newLinkedList();
			}
		}

		// TODO: workaround (?)

		startEntity(id);
	}

	@Override
	public void endRecord() {

		// TODO: workaround (?)

		endEntity();

		if (dataJSON == null) {

			throw new MetafactureException("there is no data for printing");
		}

		json = new ObjectNode(DMPPersistenceUtil.getJSONFactory());

		if (firstLine == false) {

			json = dataJSON;
		} else {

			if (withHeader) {

				if (schemaJSON != null) {

					json.put("schema", schemaJSON);
				}
			}
			
			final ArrayNode dataJSONArray = new ArrayNode(DMPPersistenceUtil.getJSONFactory());

			dataJSONArray.add(dataJSON);
			json.put("data", dataJSONArray);
			
		}

		getReceiver().process(json);
	}

	@Override
	public void startEntity(final String name) {

		values = Lists.newLinkedList();
	}

	@Override
	public void endEntity() {

		if (firstLine) {

			if (withHeader) {

				printHeader();
			}
		}

		// write record

		printData();
	}

	@Override
	public void literal(final String name, final String value) {

		if (firstLine) {

			if (withHeader) {

				if (name != null) {

					header.add(name);
				} else {

					throw new MetafactureException("couldn't write header column, because it is null");
				}
			}
		}

		// collect values
		if (value != null) {

			values.add(value);
		} else {

			throw new MetafactureException("name and value are null");
		}
	}

	public void withHeader() {

		withHeader = true;
	}

	private void printHeader() {

		schemaJSON = new ArrayNode(DMPPersistenceUtil.getJSONFactory());

		for (final String headerField : header) {

			schemaJSON.add(headerField);
		}
	}

	private void printData() {

		dataJSON = new ObjectNode(DMPPersistenceUtil.getJSONFactory());

		int i = 0;

		for (final String headerField : header) {

			final String value = values.get(i);

			if (value == null) {

				continue;
			}

			dataJSON.put(headerField, value);
		}
	}

	// public static final String ARRAY_MARKER = "[]";
	//
	//
	//
	// @Override
	// public void startRecord(final String id) {
	// final StringBuffer buffer = writer.getBuffer();
	// buffer.delete(0, buffer.length());
	// startGroup(id);
	// }
	//
	// @Override
	// public void endRecord() {
	// endGroup();
	// try {
	// jsonGenerator.flush();
	// } catch (IOException e) {
	// throw new MetafactureException(e);
	// }
	// }
	//
	// @Override
	// public void startEntity(final String name) {
	// startGroup(name);
	// }
	//
	// @Override
	// public void endEntity() {
	// endGroup();
	// }
	//
	// @Override
	// public void literal(final String name, final String value) {
	// try {
	// final JsonStreamContext ctx = jsonGenerator.getOutputContext();
	// if (ctx.inObject()) {
	// jsonGenerator.writeFieldName(name);
	// }
	// if (value == null) {
	// jsonGenerator.writeNull();
	// } else {
	// jsonGenerator.writeString(value);
	// }
	// } catch (JsonGenerationException e) {
	// throw new MetafactureException(e);
	// }
	// catch (IOException e) {
	// throw new MetafactureException(e);
	// }
	// }
	//
	// private void startGroup(final String name) {
	// try {
	// final JsonStreamContext ctx = jsonGenerator.getOutputContext();
	// if (name.endsWith(ARRAY_MARKER)) {
	// if (ctx.inObject()) {
	// jsonGenerator.writeFieldName(name.substring(0, name.length() - ARRAY_MARKER.length()));
	// }
	// jsonGenerator.writeStartArray();
	// } else {
	// if (ctx.inObject()) {
	// jsonGenerator.writeFieldName(name);
	// }
	// jsonGenerator.writeStartObject();
	// }
	// } catch (JsonGenerationException e) {
	// throw new MetafactureException(e);
	// }
	// catch (IOException e) {
	// throw new MetafactureException(e);
	// }
	// }
	//
	// private void endGroup() {
	// try {
	// final JsonStreamContext ctx = jsonGenerator.getOutputContext();
	// if (ctx.inObject()) {
	// jsonGenerator.writeEndObject();
	// } else if (ctx.inArray()) {
	// jsonGenerator.writeEndArray();
	// }
	// } catch (JsonGenerationException e) {
	// throw new MetafactureException(e);
	// }
	// catch (IOException e) {
	// throw new MetafactureException(e);
	// }
	// }

}
