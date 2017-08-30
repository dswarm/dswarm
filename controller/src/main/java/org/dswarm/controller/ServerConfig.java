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
package org.dswarm.controller;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import com.google.common.base.Preconditions;
import com.typesafe.config.Config;


public class ServerConfig {

	/**
	 * The name of the property for the hostname or ip of the backend web server.
	 */
	private static final String		HTTP_HOST_PROPERTY		= "dswarm.http.host";

	/**
	 * The name of the property for the port of the backend web server.
	 */
	private static final String		HTTP_PORT_PROPERTY		= "dswarm.http.port";

	/**
	 * The name of the property for the context path of the backend API at the backend web server.
	 */
	private static final String		CONTEXT_PATH_PROPERTY	= "dswarm.http.context-path";

	private final int port;
	private final String contextPath;
	private final String host;
	private final URI baseUri;

	public ServerConfig(final int port, final String host, final String contextPath) {
		this.port = port;
		this.contextPath = contextPath;
		this.host = host;
		baseUri = UriBuilder.fromUri("http://0.0.0.0/").host(host).port(port).path(contextPath).build();
	}

	public static ServerConfig from(final Config config) {
		final String host = config.getString(HTTP_HOST_PROPERTY);

		final int port = config.getInt(HTTP_PORT_PROPERTY);
		Preconditions.checkArgument(port >= 1024, "the port must be greater than or equal to 1024");

		final String contextPath = config.getString(CONTEXT_PATH_PROPERTY);
		Preconditions.checkArgument("/".equals(contextPath.substring(0, 1)), "The context path must start with a '/'");

		return new ServerConfig(port, host, contextPath);
	}

	/**
	 * Gets the base URI of the backend API.
	 *
	 * @return the base URI
	 */
	public URI getBaseUri() {
		return baseUri;
	}

	/**
	 * Gets the context path of the backend API at the backend webserver.
	 *
	 * @return the context path of the backend API at the backend webserver
	 */
	public String getContextPath() {
		return contextPath;
	}

	public int getPort() {
		return port;
	}

	public String getHost() {
		return host;
	}
}
