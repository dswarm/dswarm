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

import de.avgl.dmp.persistence.model.types.Tuple;
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
	private List<Tuple<String, String>>	values		= null;
	private ArrayNode		schemaJSON				= null;
	private ObjectNode		dataJSON				= null;

	private boolean			withHeader				= false;
	private boolean			firstLine				= false;
	private boolean			firstLineInitialized	= false;
	private boolean			withLimit				= false;
	private int				limit					= -1;
	private int				count					= 0;

	public CSVJSONEncoder() {

	}

	public CSVJSONEncoder(final int limit) {

		this.limit = limit;
		this.withLimit = true;
	}

	@Override
	public void startRecord(final String id) {

		if (withLimit) {

			if (count == limit) {

				return;
			}

			count++;
		}

		if (firstLine) {

			firstLine = false;
		}

		if (!firstLineInitialized) {

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

		if (withLimit) {

			if (count == limit) {

				return;
			}
		}

		// TODO: workaround (?)

		endEntity();

		if (dataJSON == null) {

			throw new MetafactureException("there is no data for printing");
		}

		ObjectNode json = new ObjectNode(DMPPersistenceUtil.getJSONFactory());

		if (!firstLine) {

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

			values.add(Tuple.tuple(name, value));
		} else {

			throw new MetafactureException("name and value are null");
		}
	}

	public void withHeader() {

		withHeader = true;

		if (withLimit) {

			// increase limit for header row

			limit++;
		}
	}

	private void printHeader() {

		schemaJSON = new ArrayNode(DMPPersistenceUtil.getJSONFactory());

		for (final String headerField : header) {

			schemaJSON.add(headerField);
		}
	}

	private void printData() {

		dataJSON = new ObjectNode(DMPPersistenceUtil.getJSONFactory());

		for (Tuple<String, String> value : values) {
			dataJSON.put(value.v1(), value.v2());
		}
	}
}
