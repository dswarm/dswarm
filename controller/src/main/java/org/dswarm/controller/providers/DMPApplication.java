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
package org.dswarm.controller.providers;

import javax.inject.Inject;

import com.wordnik.swagger.jersey.listing.ApiListingResourceJSON;
import com.wordnik.swagger.jersey.listing.JerseyApiDeclarationProvider;
import com.wordnik.swagger.jersey.listing.JerseyResourceListingProvider;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.jvnet.hk2.guice.bridge.api.GuiceBridge;
import org.jvnet.hk2.guice.bridge.api.GuiceIntoHK2Bridge;

import org.dswarm.controller.providers.filter.CorsResponseFilter;
import org.dswarm.controller.providers.handler.DMPJsonExceptionHandler;
import org.dswarm.controller.providers.handler.DMPMorphDefExceptionHandler;
import org.dswarm.controller.providers.handler.ExceptionHandler;
import org.dswarm.controller.providers.handler.WebApplicationExceptionHandler;

/**
 * The configuration for the backend API. Packages with (web) resources, API feature classes (e.g. {@link MultiPartFeature}) etc.
 * can be registered here.
 *
 * @author phorn
 * @author tgaengler
 */
@SuppressWarnings("UnusedDeclaration")
class DMPApplication extends ResourceConfig {

	/**
	 * Creates a new backend API configuration with the given service locator (H2K service registry).
	 *
	 * @param serviceLocator a H2K service registry
	 */
	@Inject
	public DMPApplication(final ServiceLocator serviceLocator) {
		setApplicationName("d:swarm");

		registerDMPResources();
		registerSwaggerResources();
		buildGuiceBridge(serviceLocator);

		registerClasses(
				DMPAppEventListener.class,
				InstrumentedMetricsEventListener.class);
	}

	private void registerDMPResources() {
		packages("org.dswarm.controller.resources");
		registerClasses(DMPJsonExceptionHandler.class, DMPMorphDefExceptionHandler.class, ExceptionHandler.class,
				WebApplicationExceptionHandler.class, CorsResponseFilter.class, MultiPartFeature.class);
	}

	private void registerSwaggerResources() {
		packages("com.wordnik.swagger.jersey.listing");
		registerClasses(ApiListingResourceJSON.class, JerseyApiDeclarationProvider.class, JerseyResourceListingProvider.class);
	}

	private static void buildGuiceBridge(final ServiceLocator serviceLocator) {
		GuiceBridge.getGuiceBridge().initializeGuiceBridge(serviceLocator);
		final GuiceIntoHK2Bridge guiceBridge = serviceLocator.getService(GuiceIntoHK2Bridge.class);
		guiceBridge.bridgeGuiceInjector(DMPInjector.getOrDefault());
	}
}
