package org.dswarm.controller.servlet;

import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;
import com.fasterxml.jackson.module.guice.ObjectMapperModule;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;

import org.dswarm.controller.guice.DMPModule;
import org.dswarm.controller.guice.DMPServletModule;
import org.dswarm.persistence.PersistenceModule;

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

			DMPInjector.injector = Guice.createInjector(new ObjectMapperModule().registerModule(new PersistenceModule.DmpDeserializerModule())
					.registerModule(new JaxbAnnotationModule()).registerModule(new Hibernate4Module()), new PersistenceModule(), new DMPModule(),
					new DMPServletModule());
		}

		return DMPInjector.injector;
	}
}
