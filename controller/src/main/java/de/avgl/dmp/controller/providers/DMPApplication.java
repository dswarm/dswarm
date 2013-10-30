package de.avgl.dmp.controller.providers;

import javax.inject.Inject;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.jvnet.hk2.guice.bridge.api.GuiceBridge;
import org.jvnet.hk2.guice.bridge.api.GuiceIntoHK2Bridge;

import de.avgl.dmp.controller.servlet.DMPInjector;

@SuppressWarnings("UnusedDeclaration")
public class DMPApplication extends ResourceConfig {

	@Inject
	public DMPApplication(final ServiceLocator serviceLocator) {

		packages("de.avgl.dmp.controller.resources");
		register(MultiPartFeature.class);
		register(ExceptionHandler.class);
		register(CorsResponseFilter.class);

		// initialize injectors...
		GuiceBridge.getGuiceBridge().initializeGuiceBridge(serviceLocator);

		final GuiceIntoHK2Bridge guiceBridge = serviceLocator.getService(GuiceIntoHK2Bridge.class);
		guiceBridge.bridgeGuiceInjector(DMPInjector.injector);

	}
}
