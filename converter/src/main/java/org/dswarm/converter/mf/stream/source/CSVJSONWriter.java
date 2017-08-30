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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.culturegraph.mf.exceptions.MetafactureException;
import org.culturegraph.mf.framework.ObjectReceiver;

/**
 * @author tgaengler
 */
public class CSVJSONWriter implements ObjectReceiver<JsonNode> {

	private JsonNode	json;
	private boolean		closed;

	@Override
	public void process(final JsonNode json) {

		assert !closed;

		// TODO write incoming json

		if (this.json != null && this.json.size() > 0) {

			// add only incoming data JSON object

			final JsonNode dataJSON = this.json.get("data");

			if (dataJSON == null) {

				throw new MetafactureException("data JSON shouldn't be null");
			}

			if (!dataJSON.isArray()) {

				throw new MetafactureException("data JSON should be an array");
			}

			final ArrayNode dataJSONArray = (ArrayNode) dataJSON;

			dataJSONArray.add(json);
		} else {

			// add complete input to init complete JSON object

			this.json = json;
		}
	}

	@Override
	public void resetStream() {

		json = null;
	}

	@Override
	public void closeStream() {

		closed = true;
	}

	@Override
	public String toString() {

		if (json != null) {

			return json.toString();
		}

		return null;
	}
}
