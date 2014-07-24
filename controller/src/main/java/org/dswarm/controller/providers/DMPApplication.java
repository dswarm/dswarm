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

		registerClasses(DMPAppEventListener.class);
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

	private void buildGuiceBridge(final ServiceLocator serviceLocator) {
		GuiceBridge.getGuiceBridge().initializeGuiceBridge(serviceLocator);
		final GuiceIntoHK2Bridge guiceBridge = serviceLocator.getService(GuiceIntoHK2Bridge.class);
		guiceBridge.bridgeGuiceInjector(DMPInjector.getOrDefault());
	}
}
