package org.dswarm.controller.guice;

import java.util.Properties;

import com.google.inject.name.Names;
import com.google.inject.persist.PersistFilter;
import com.google.inject.persist.jpa.JpaPersistModule;
import com.google.inject.servlet.ServletModule;

import org.dswarm.controller.doc.SwaggerConfig;
import org.dswarm.controller.servlet.filter.MetricsFilter;
import org.dswarm.controller.utils.DMPControllerUtils;

/**
 * The Guice configuration of the servlet of the backend API. Mainly, servlets, filters and configuration properties are defined
 * here.
 *
 * @author phorn
 */
public class DMPServletModule extends ServletModule {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void configureServlets() {

		// provides and handles the entity manager of the application
		install(new JpaPersistModule("DMPApp"));

		filter("/*").through(PersistFilter.class);

		bind(String.class).annotatedWith(Names.named("ApiVersion")).toInstance("1.0.1");

		final Properties properties = DMPControllerUtils.loadProperties();

		bind(String.class).annotatedWith(Names.named("ApiBaseUrl")).toInstance(
				properties.getProperty("swagger.base_url", "http://localhost:8087/dmp"));

		serve("/api-docs").with(SwaggerConfig.class);
		filter("/*").through(MetricsFilter.class);
	}
}
