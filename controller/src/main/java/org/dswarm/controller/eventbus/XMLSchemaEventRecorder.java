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
package org.dswarm.controller.eventbus;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.Resource;
import org.dswarm.persistence.service.schema.SchemaService;

@Singleton
public class XMLSchemaEventRecorder {

	private final SchemaService	schemaService;

	@Inject
	public XMLSchemaEventRecorder(final SchemaService schemaService/* , final EventBus eventBus */) {

		this.schemaService = schemaService;

		// eventBus.register(this);
	}

	// @Subscribe
	public void convertConfiguration(final XMLSchemaEvent event) {
		final Configuration configuration = event.getConfiguration();
		final Resource resource = event.getResource();

		final String filename = resource.getAttribute("path").asText();

		// TODO: fixme, if needed
	}
}
