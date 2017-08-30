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
package org.dswarm.converter.pipe;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.LinkedListMultimap;
import org.culturegraph.mf.framework.DefaultStreamPipe;
import org.culturegraph.mf.framework.StreamReceiver;

/**
 * Collapse several literals of the same name within on entity in such a way, that a following
 * {@link org.culturegraph.mf.stream.converter.JsonEncoder} will produce correct results.
 * 
 * @author Paul Horn <phorn@avantgarde-labs.de>
 */
public class StreamJsonCollapser extends DefaultStreamPipe<StreamReceiver> {

	/**
	 * Marks the begin of an (JSON) array. This must match {@link org.culturegraph.mf.stream.converter.JsonEncoder#ARRAY_MARKER}
	 */
	public static final String							ARRAY_MARKER	= "[]";

	private final LinkedListMultimap<String, String>	valueMap		= LinkedListMultimap.create();

	@Override
	public void startRecord(final String identifier) {
		valueMap.clear();
		getReceiver().startRecord(identifier);
	}

	@Override
	public void endRecord() {
		flushValues();
		getReceiver().endRecord();
	}

	@Override
	public void startEntity(final String name) {
		flushValues();
		getReceiver().startEntity(name);
	}

	@Override
	public void endEntity() {
		flushValues();
		getReceiver().endEntity();
	}

	/**
	 * Groups literals within the same entity by their name. Instead of directly forwarding every literal, they are buffered in a
	 * {@link LinkedListMultimap}. Subsequent calls to {@link StreamJsonCollapser#endEntity()} or
	 * {@link StreamJsonCollapser#endRecord()} will flush the buffered value. If some name appeared multiple times, it will result
	 * in a new array entity.
	 * 
	 * @param name literal name (may appear multiple times)
	 * @param value literal value
	 */
	@Override
	public void literal(final String name, final String value) {
		valueMap.put(name, value);
	}

	private void flushValues() {
		for (final Map.Entry<String, Collection<String>> entry : valueMap.asMap().entrySet()) {
			final String name = entry.getKey();
			final boolean isMulti = entry.getValue().size() > 1;
			if (isMulti) {
				getReceiver().startEntity(name + StreamJsonCollapser.ARRAY_MARKER);
			}
			for (final String value : entry.getValue()) {
				getReceiver().literal(name, value);
			}
			if (isMulti) {
				getReceiver().endEntity();
			}
		}

		valueMap.clear();
	}
}
