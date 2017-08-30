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

import org.culturegraph.mf.framework.LifeCycle;
import org.culturegraph.mf.framework.Receiver;

/**
 * Interface for objects which receive JSON events.
 * This is just a combination of the XXX interfaces
 * and the metastream {@link LifeCycle} interface.
 *
 * @see JsonPipe
 *
 * @author tgaengler
 */
public interface JsonReceiver extends Receiver {

	// TODO: add methods that should be provided by such a receiver

	void startObject(final String name/* add parameters as needed */);

	void endObject(final String name/* add parameters as needed */);

	void startArray(final String name/* add parameters as needed */);

	void endArray(final String name/* add parameters as needed */);

	void literal(final String name, final String value);
}
