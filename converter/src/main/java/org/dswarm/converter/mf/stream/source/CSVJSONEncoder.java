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
package org.dswarm.converter.mf.stream.source;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import org.culturegraph.mf.exceptions.MetafactureException;
import org.culturegraph.mf.framework.DefaultStreamPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;

import org.dswarm.common.types.Tuple;
import org.dswarm.persistence.util.DMPPersistenceUtil;

/**
 * Serialises an object as JSON. Records and entities are represented as objects unless their name ends with []. If the name ends
 * with [], an array is created.
 * 
 * @author tgaengler
 * @author phorn
 */
@Description("Serialises an object as JSON")
@In(StreamReceiver.class)
@Out(JsonNode.class)
public final class CSVJSONEncoder extends DefaultStreamPipe<ObjectReceiver<JsonNode>> {

	private List<String>				header;
	private List<Tuple<String, String>> values;
	private ArrayNode                   schemaJSON;
	private ObjectNode                  dataJSON;

	private boolean withHeader;
	private boolean firstLine;
	private boolean firstLineInitialized;

	@Override
	public void startRecord(final String id) {

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

					json.set("schema", schemaJSON);
				}
			}

			final ArrayNode dataJSONArray = new ArrayNode(DMPPersistenceUtil.getJSONFactory());

			dataJSONArray.add(dataJSON);
			json.set("data", dataJSONArray);

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
	}

	private void printHeader() {

		schemaJSON = new ArrayNode(DMPPersistenceUtil.getJSONFactory());

		for (final String headerField : header) {

			schemaJSON.add(headerField);
		}
	}

	private void printData() {

		dataJSON = new ObjectNode(DMPPersistenceUtil.getJSONFactory());

		for (final Tuple<String, String> value : values) {
			dataJSON.put(value.v1(), value.v2());
		}
	}
}
