package de.avgl.dmp.controller.servlet;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;

import de.avgl.dmp.controller.guice.DMPModule;
import de.avgl.dmp.controller.guice.DMPServletModule;
import de.avgl.dmp.persistence.PersistenceModule;

/**
 * The Guice injector for the backend API. Register here all Guice configuration that should be recognized when the backend API is
 * running.
 *
 * @author phorn
 */
public class DMPInjector extends GuiceServletContextListener {

	/**
	 * The Guice injector.
	 */
	@SuppressWarnings("StaticNonFinalField")
	public static Injector	injector;

	/**
	 * Gets the Guice injector.
	 */
	@Override
	protected Injector getInjector() {

		if (DMPInjector.injector == null) {

			DMPInjector.injector = Guice.createInjector(new PersistenceModule(), new DMPModule(), new DMPServletModule());
		}

		return DMPInjector.injector;
	}
}
