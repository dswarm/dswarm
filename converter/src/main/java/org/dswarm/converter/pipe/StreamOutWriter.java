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

import org.culturegraph.mf.framework.DefaultStreamPipe;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;

/**
 * Leaves the event stream untouched but writes it to stdout, for debugging. The {@link StreamReceiver} may be {@code null}. In
 * this case {@code StreamOutWriter} behaves as a sink, just logging.
 * 
 * @author phorn
 */
@Description("dumps events on stdout")
@In(StreamReceiver.class)
@Out(StreamReceiver.class)
public final class StreamOutWriter extends DefaultStreamPipe<StreamReceiver> {

	@Override
	public void startRecord(final String identifier) {
		assert !isClosed();
		System.out.println("start record = " + identifier);
		if (null != getReceiver()) {
			getReceiver().startRecord(identifier);
		}
	}

	@Override
	public void endRecord() {
		assert !isClosed();
		System.out.println("end record");
		if (null != getReceiver()) {
			getReceiver().endRecord();
		}
	}

	@Override
	public void startEntity(final String name) {
		assert !isClosed();
		System.out.println("start entity = " + name);
		if (null != getReceiver()) {
			getReceiver().startEntity(name);
		}
	}

	@Override
	public void endEntity() {
		assert !isClosed();
		System.out.println("end entity");
		if (null != getReceiver()) {
			getReceiver().endEntity();
		}

	}

	@Override
	public void literal(final String name, final String value) {
		assert !isClosed();
		System.out.println("start literal = " + name + " : " + value);
		if (null != getReceiver()) {
			getReceiver().literal(name, value);
		}
	}

	@Override
	protected void onResetStream() {
		System.out.println("reset stream");
	}

	@Override
	protected void onCloseStream() {
		System.out.println("close stream");
	}

}
