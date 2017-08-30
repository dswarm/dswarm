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
import org.culturegraph.mf.framework.DefaultObjectPipe;
import org.culturegraph.mf.framework.ObjectReceiver;

/**
 * @author phorn
 */
public class JsonNodesOpener extends DefaultObjectPipe<String, ObjectReceiver<JsonNode>> {

	public JsonNodesOpener() {
	}

	@Override
	public void process(final String obj) {
		super.process(obj); // To change body of overridden methods use File | Settings | File Templates.
	}
}
