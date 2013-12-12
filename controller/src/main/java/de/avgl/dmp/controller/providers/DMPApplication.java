package de.avgl.dmp.controller.providers;

import javax.inject.Inject;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.jvnet.hk2.guice.bridge.api.GuiceBridge;
import org.jvnet.hk2.guice.bridge.api.GuiceIntoHK2Bridge;

import com.wordnik.swagger.jersey.listing.ApiListingResourceJSON;
import com.wordnik.swagger.jersey.listing.JerseyApiDeclarationProvider;
import com.wordnik.swagger.jersey.listing.JerseyResourceListingProvider;

import de.avgl.dmp.controller.providers.filter.CorsResponseFilter;
import de.avgl.dmp.controller.providers.handler.ExceptionHandler;
import de.avgl.dmp.controller.providers.handler.WebApplicationExceptionHandler;
import de.avgl.dmp.controller.servlet.DMPInjector;

@SuppressWarnings("UnusedDeclaration")
class DMPApplication extends ResourceConfig {

	@Inject
	public DMPApplication(final ServiceLocator serviceLocator) {

		packages("de.avgl.dmp.controller.resources", "com.wordnik.swagger.jersey.listing");
		registerClasses(
				ExceptionHandler.class,
				WebApplicationExceptionHandler.class,
				CorsResponseFilter.class);
		register(MultiPartFeature.class);

		// swagger
		register(ApiListingResourceJSON.class);
		register(JerseyApiDeclarationProvider.class);
		register(JerseyResourceListingProvider.class);

		// initialize injectors...
		GuiceBridge.getGuiceBridge().initializeGuiceBridge(serviceLocator);

		final GuiceIntoHK2Bridge guiceBridge = serviceLocator.getService(GuiceIntoHK2Bridge.class);
		guiceBridge.bridgeGuiceInjector(DMPInjector.injector);

	}
}
