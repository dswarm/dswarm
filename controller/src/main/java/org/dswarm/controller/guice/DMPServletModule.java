package org.dswarm.controller.guice;

import com.google.inject.persist.PersistFilter;
import com.google.inject.servlet.ServletModule;

import org.dswarm.controller.doc.SwaggerConfiguration;
import org.dswarm.controller.servlet.filter.MetricsFilter;

/**
 * The Guice configuration of the servlet of the backend API.
 * Mainly, servlets, filters and configuration properties are defined here.
 * 
 * @author phorn
 */
public class DMPServletModule extends ServletModule {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void configureServlets() {
		serve("/api-docs").with(SwaggerConfiguration.class);
		filter("/*").through(PersistFilter.class);
		filter("/*").through(MetricsFilter.class);
	}
}
