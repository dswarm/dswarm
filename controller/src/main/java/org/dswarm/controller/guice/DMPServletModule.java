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
package org.dswarm.controller.guice;

import com.google.inject.persist.PersistFilter;
import com.google.inject.servlet.ServletModule;

import org.dswarm.controller.doc.SwaggerConfiguration;
import org.dswarm.controller.providers.filter.ExecutionScopeFilter;

/**
 * The Guice configuration of the servlet of the backend API.
 * Mainly, servlets, filters and configuration properties are defined here.
 *
 * @author phorn
 */
public class DMPServletModule extends ServletModule {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void configureServlets() {
		serve("/api-docs").with(SwaggerConfiguration.class);
		filter("/*").through(PersistFilter.class);
		filter("/*").through(ExecutionScopeFilter.class);
	}
}
