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
package org.dswarm.converter.mf.framework;

import org.culturegraph.mf.framework.DefaultSender;
import org.culturegraph.mf.framework.Receiver;

/**
 * Default implementation for {@link JsonPipe}s which simply
 * does nothing.
 *
 * @param <R> receiver type of the downstream module
 *
 * @author tgaengler
 */
public class DefaultJsonPipe<R extends Receiver> extends DefaultSender<R> implements JsonPipe<R> {

	@Override
	public void startObject(final String name) {

		// Default implementation does nothing
	}

	@Override
	public void endObject(final String name) {

		// Default implementation does nothing
	}

	@Override
	public void startArray(final String name) {

		// Default implementation does nothing
	}

	@Override
	public void endArray(final String name) {

		// Default implementation does nothing
	}

	@Override
	public void literal(final String name, final String value) {

		// Default implementation does nothing
	}
}
