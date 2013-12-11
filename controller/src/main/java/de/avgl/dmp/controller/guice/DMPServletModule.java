package de.avgl.dmp.controller.guice;

import java.util.concurrent.TimeUnit;

import com.google.inject.name.Names;
import com.google.inject.persist.PersistFilter;
import com.google.inject.persist.jpa.JpaPersistModule;
import com.google.inject.servlet.ServletModule;

import de.avgl.dmp.controller.doc.SwaggerConfig;
import de.avgl.dmp.controller.servlet.HeartbeatServlet;
import de.avgl.dmp.controller.servlet.MetricsServlet;
import de.avgl.dmp.controller.servlet.filter.MetricsFilter;

public class DMPServletModule extends ServletModule {

	@Override
	protected void configureServlets() {

		install(new JpaPersistModule("DMPApp"));

		filter("/*").through(PersistFilter.class);

		bind(TimeUnit.class).annotatedWith(Names.named("RateUnit")).toInstance(TimeUnit.SECONDS);
		bind(TimeUnit.class).annotatedWith(Names.named("DurationUnit")).toInstance(TimeUnit.MILLISECONDS);
		bind(String.class).annotatedWith(Names.named("AllowedOrigin")).toInstance("*");
		bind(Boolean.class).annotatedWith(Names.named("ShowSamples")).toInstance(false);

		bind(String.class).annotatedWith(Names.named("ApiVersion")).toInstance("1.0.1");
		bind(String.class).annotatedWith(Names.named("ApiBaseUrl")).toInstance("http://localhost:8087/dmp");

		serve("/_stats").with(MetricsServlet.class);
		serve("/_ping").with(HeartbeatServlet.class);

		serve("/api-docs").with(SwaggerConfig.class);

		filter("/*").through(MetricsFilter.class);
	}
}


