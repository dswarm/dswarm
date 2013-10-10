package de.avgl.dmp.controller.servlet;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;

import de.avgl.dmp.controller.guice.DMPModule;
import de.avgl.dmp.controller.guice.DMPServletModule;
import de.avgl.dmp.persistence.PersistenceModule;

public class DMPInjector extends GuiceServletContextListener {

	public static Injector injector = null;

	@Override
	protected Injector getInjector() {

		if (injector == null) {

			injector = Guice.createInjector(
					new PersistenceModule(),
					new DMPModule(),
					new DMPServletModule());
		}

		return injector;
	}
}
