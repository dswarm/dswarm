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
package org.dswarm.controller.doc;

import javax.servlet.ServletConfig;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.wordnik.swagger.config.ConfigFactory$;
import com.wordnik.swagger.config.SwaggerConfig;
import com.wordnik.swagger.jersey.config.JerseyJaxrsConfig;
import com.wordnik.swagger.model.ApiInfo;

/**
 * The configuration for the documentation generation application Swagger. It is utilised for generating the documentation of the
 * backend API.
 *
 * @author tgaengler
 * @author phorn
 */
@Singleton
public class SwaggerConfiguration extends JerseyJaxrsConfig {

	/**
	 *
	 */
	private static final long	serialVersionUID	= 1L;

	/**
	 * The version of the backend API.
	 */
	private final String		apiVersion;

	/**
	 * The base URI of the backend API.
	 */
	private final String		apiBaseUrl;

	/**
	 * Creates a new Swagger configuration with the given version and base URI of the backend API.
	 *
	 * @param apiVersion the version of the backend API
	 * @param apiBaseUrl the base URI of the backend API
	 */
	@Inject
	public SwaggerConfiguration(@Named("dswarm.api.version") final String apiVersion, @Named("dswarm.api.base-url") final String apiBaseUrl) {

		this.apiVersion = apiVersion;
		this.apiBaseUrl = apiBaseUrl;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(final ServletConfig servletConfig) {

		super.init(servletConfig);

		final ApiInfo info = new ApiInfo(
			      /* title */ "d:swarm Backend",
			/* description */ "This is the d:swarm Backend server.",
			    /* TOS Url */ "http://helloreverb.com/terms/",
			    /* Contact */ "tgaengler@avantgarde-labs.de",
			    /* license */ "Apache 2.0",
			/* license URL */ "http://www.apache.org/licenses/LICENSE-2.0.html"
		);

		final SwaggerConfig config = ConfigFactory$.MODULE$.config();

		config.setApiVersion(apiVersion);
		config.setBasePath(apiBaseUrl);
		config.setApiInfo(info);

		// Not to be confused with the swagger framework version
		// This is the swagger specification version
		config.setSwaggerVersion("1.2");
	}

}
