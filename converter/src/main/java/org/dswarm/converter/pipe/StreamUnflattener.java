/**
 * Copyright (C) 2013 – 2016 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dswarm.converter.pipe;

import org.culturegraph.mf.framework.DefaultStreamPipe;
import org.culturegraph.mf.framework.StreamReceiver;

/**
 * Reverse the effect of {@link org.culturegraph.mf.stream.pipe.StreamFlattener} The StreamFlattener is used by
 * {@link org.culturegraph.mf.morph.Metamorph} and flattens out any hierarchical data into a flat list of key-value tuples. This
 * class then constructs the flat structure produced by Metamorph and emits them in the proper hierarchy. So, one can use 'deep'
 * output names in Metamorh (e.g. &lt;data name="foo.bar%gt;), that pipe the result through an instance of StreamUnflattener and
 * continue to work with the desired hierarchical structure.
 *
 * @author Paul Horn <phorn@avantgarde-labs.de>
 */
public class StreamUnflattener extends DefaultStreamPipe<StreamReceiver> {

	/**
	 * Separates different entity levels from each other. This must match
	 * {@link org.culturegraph.mf.stream.pipe.StreamFlattener#entityMarker} but represents a regular expression, so make sure to
	 * use proper escaping!
	 */
	public static final char DEFAULT_ATTRIBUTE_DELIMITER = '.';

	/**
	 * Any entity at the root level, that value-equals this property will be discarded. The next entity will be the new root level
	 * of the resulting stream.
	 */
	public static final String DEFAULT_INITIAL_DISCARD = "";

	private final char entityMarker;
	private final String attributeDelimiter;
	private final String initialDiscard;

	/**
	 * @return the entity marker
	 * @see #DEFAULT_ATTRIBUTE_DELIMITER
	 */
	public char getEntityMarker() {
		return entityMarker;
	}

	/**
	 * @return the initial discard value
	 * @see #DEFAULT_INITIAL_DISCARD
	 */
	public String getInitialDiscard() {
		return initialDiscard;
	}

	/**
	 * Constructs an instance with a given <code>initialDiscard</code> and <code>entityMarker</code>
	 *
	 * @param initialDiscard use this <code>initialDiscard</code>
	 * @param entityMarker use this <code>entityMarker</code>
	 */
	public StreamUnflattener(final String initialDiscard, final char entityMarker) {
		this.entityMarker = entityMarker;
		this.attributeDelimiter = String.valueOf(entityMarker);
		this.initialDiscard = initialDiscard;
	}

	/**
	 * Constructs an instance with a given <code>initialDiscard</code>
	 *
	 * @param initialDiscard use this <code>initialDiscard</code>
	 */
	public StreamUnflattener(final String initialDiscard) {
		this(initialDiscard, StreamUnflattener.DEFAULT_ATTRIBUTE_DELIMITER);
	}

	/**
	 * Class Constructor. Constructs an instance with default values for <code>initialDiscard</code> and <code>entityMarker</code>
	 */
	public StreamUnflattener() {
		this(StreamUnflattener.DEFAULT_INITIAL_DISCARD);
	}

	/**
	 * Forwards <code>startRecord</code> and discards any dangling entity trees
	 *
	 * @param identifier the record identifier
	 */
	@Override
	public void startRecord(final String identifier) {
		assert !isClosed();
		getReceiver().startRecord(identifier);
	}

	/**
	 * Forwards <code>endRecord</code> and closes any dangling entity trees
	 */
	@Override
	public void endRecord() {
		assert !isClosed();

		getReceiver().endRecord();
	}

	/**
	 * Since StreamUnflattener expects a completely flat stream, startEntity is not defined.
	 *
	 * @param name the entity name
	 */
	@Override
	public void startEntity(final String name) {
		//throw new IllegalStateException(getClass().getName() + " has to be called on a flat stream, e.g. as produces by StreamFlattener");
		getReceiver().startEntity(name);
	}

	/**
	 * Since StreamUnflattener expects a completely flat stream, endEntity is not defined.
	 */
	@Override
	public void endEntity() {
		//throw new IllegalStateException(getClass().getName() + " has to be called on a flat stream, e.g. as produces by StreamFlattener");
		getReceiver().endEntity();
	}

	/**
	 * Forwards the literal and emits some entity levels, if necessary. The parameter name is split on {@link #getEntityMarker()}.
	 * The resulting array is interpreted as a path into a tree and according to the current state, some new nodes of this tree
	 * will be opened by calling <code>startEntity</code> and <code>endEntity</code>.
	 *
	 * @param name the literal name
	 * @param value the literal value
	 */
	@Override
	public void literal(final String name, final String value) {
		assert !isClosed();

		final String[] attributes = name.split(attributeDelimiter);

		if(attributes.length <= 0) {

			return;
		}

		final String attribute = attributes[attributes.length - 1];

		getReceiver().literal(attribute, value);
	}
}
