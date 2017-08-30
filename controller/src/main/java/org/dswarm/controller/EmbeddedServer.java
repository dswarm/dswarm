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

import java.io.IOException;
import java.net.URI;
import java.util.EnumSet;
import java.util.concurrent.CountDownLatch;

import javax.servlet.DispatcherType;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import com.typesafe.config.Config;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.StaticHttpHandler;
import org.glassfish.grizzly.servlet.FilterRegistration;
import org.glassfish.grizzly.servlet.ServletRegistration;
import org.glassfish.grizzly.servlet.WebappContext;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import org.dswarm.controller.providers.DMPInjector;

/**
 * The embedded web server for the backend API.<br/>
 * note: currently, Grizzly is utilised
 * 
 * @author phorn
 * @author tgaengler
 */
public class EmbeddedServer {

	private static final Marker		FATAL = MarkerFactory.getMarker("FATAL");
	private static final Logger		LOG						= LoggerFactory.getLogger(EmbeddedServer.class);

	private final Optional<Injector> injectorOptional;

	/**
	 * The backend web server.
	 */
	private HttpServer				httpServer;

	/**
	 * The server configuration after calling start
	 */
	private Optional<ServerConfig>	serverConfigOptional;


	public EmbeddedServer() {
		this(null, null);
	}

	public EmbeddedServer(final ServerConfig serverConfig) {
		this(null, serverConfig);
	}

	public EmbeddedServer(final Injector injector) {
		this(injector, null);
	}

	public EmbeddedServer(final Injector injector, final ServerConfig serverConfig) {
		this.injectorOptional = Optional.fromNullable(injector);
		this.serverConfigOptional = Optional.fromNullable(serverConfig);
	}

	public URI getBaseUri() {
		Preconditions.checkState(serverConfigOptional.isPresent(), "Server config was not initialized, did you forgot to call start()?");
		return serverConfigOptional.get().getBaseUri();
	}

	/**
	 * Starts the backend web server.
	 * 
	 * @return the backend web server.
	 * @throws IOException
	 */
	public HttpServer start() throws IOException {

		return start(false, injectorOptional);
	}

	public HttpServer start(final Injector injector) throws IOException {
		return start(false, Optional.fromNullable(injector));
	}

	/**
	 * Starts the backend web server.
	 *
	 * @param skipStart a flag that indicates, whether the backend web server should be really started or not
	 * @return the backend web server
	 * @throws IOException
	 */
	public HttpServer start(final boolean skipStart) throws IOException {

		return start(skipStart, injectorOptional);
	}

	public HttpServer start(final boolean skipStart, final Optional<Injector> injectorOptional) throws IOException {
		final DMPInjector dmpInjector = new DMPInjector(injectorOptional);

		if (!serverConfigOptional.isPresent()) {
			final Config config = dmpInjector.getConfig();
			serverConfigOptional = Optional.of(ServerConfig.from(config));
		}

		return start(skipStart, dmpInjector, serverConfigOptional.get());
	}

	private HttpServer start(final boolean skipStart, final DMPInjector dmpInjector, final ServerConfig serverConfig) throws IOException {

		EmbeddedServer.LOG.info("Starting backend web server (grizzly)");

		final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(serverConfig.getBaseUri(), false);
		server.getListener("grizzly").setMaxFormPostSize(Integer.MAX_VALUE);

		final WebappContext context = new WebappContext("d:swarm Backend", serverConfig.getContextPath());
		context.addListener(dmpInjector);

		final ServletRegistration servletRegistration = context.addServlet("ServletContainer", ServletContainer.class);
		servletRegistration.addMapping("/*");
		servletRegistration.setInitParameter("javax.ws.rs.Application", "org.dswarm.controller.providers.DMPApplication");
		servletRegistration.setInitParameter("jersey.config.server.response.setStatusOverSendError", "true");

		final FilterRegistration registration = context.addFilter("GuiceFilter", GuiceFilter.class);
		registration.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), "/*");

		server.getServerConfiguration().addHttpHandler(new StaticHttpHandler("src/docs/ui/docs/"), "/docs");

		context.deploy(server);

		if (!skipStart) {
			server.start();
		}

		httpServer = server;
		return server;
	}

	/**
	 * Stops the backend web server.
	 */
	public void stop() {

		EmbeddedServer.LOG.info("Shutting down backend web server (grizzly)");

		httpServer.shutdownNow();
	}

	/**
	 * Creates and starts the backend web server.
	 * 
	 * @throws IOException
	 */
	public static void main(final String[] args) throws IOException {

		final EmbeddedServer main = new EmbeddedServer();

		main.start();

		System.out.println(String.format("Jersey app with WADL available at " + "%s/application.wadl\nHit ^C to stop it...", main.getBaseUri()));

		final CountDownLatch keepAliveLatch = new CountDownLatch(1);

		Runtime.getRuntime().addShutdownHook(new Thread() {

			@Override
			public void run() {

				try {

					main.stop();
				} catch (final Exception e) {

					e.printStackTrace();
				} finally {

					keepAliveLatch.countDown();
				}
			}
		});

		final Thread keepAliveThread = new Thread(new Runnable() {

			@Override
			public void run() {

				try {

					keepAliveLatch.await();
				} catch (final InterruptedException ignore) {

					EmbeddedServer.LOG.error(EmbeddedServer.FATAL, "The backend web server execution thread was interrupted.");
				}
			}
		}, "dmp/grizzly");

		keepAliveThread.setDaemon(false);
		keepAliveThread.start();
	}
}
