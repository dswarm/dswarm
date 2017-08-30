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
package org.dswarm.converter.mf.stream;

import org.culturegraph.mf.framework.DefaultStreamPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.StreamPipe;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;

/**
 * @author phorn
 */
@Description("Serialises an object as JSON")
@In(StreamReceiver.class)
@Out(String.class)
public class RecordAwareJsonEncoder extends DefaultStreamPipe<ObjectReceiver<String>> {

	private final StreamPipe<ObjectReceiver<String>>	delegate;

	public RecordAwareJsonEncoder(final StreamPipe<ObjectReceiver<String>> delegate) {
		this.delegate = delegate;
	}

	@Override
	public void startRecord(final String id) {
		delegate.startRecord("");
		delegate.literal("record_id", id);
		delegate.startEntity("record_data");
	}

	@Override
	public void endRecord() {
		delegate.endEntity();
		delegate.endRecord();
	}

	@Override
	public void startEntity(final String name) {
		delegate.startEntity(name);
	}

	@Override
	public void endEntity() {
		delegate.endEntity();
	}

	@Override
	public void literal(final String name, final String value) {
		delegate.literal(name, value);
	}
}
