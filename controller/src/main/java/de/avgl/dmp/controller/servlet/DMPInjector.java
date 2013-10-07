package de.avgl.dmp.controller.servlet;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;

import de.avgl.dmp.controller.guice.DMPModule;

public class DMPInjector extends GuiceServletContextListener {

	public static Injector injector = null;

	@Override
	protected Injector getInjector() {

		if (injector == null) {

			injector = Guice.createInjector(new DMPModule());
		}

		return injector;
	}
}
