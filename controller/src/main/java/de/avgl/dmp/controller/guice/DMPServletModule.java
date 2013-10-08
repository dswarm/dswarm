package de.avgl.dmp.controller.guice;

import java.util.concurrent.TimeUnit;

import com.google.inject.name.Names;
import com.google.inject.servlet.ServletModule;

import de.avgl.dmp.controller.servlet.HeartbeatServlet;
import de.avgl.dmp.controller.servlet.MetricsServlet;
import de.avgl.dmp.controller.servlet.filter.MetricsFilter;

public class DMPServletModule extends ServletModule {

	@Override
	protected void configureServlets() {

		bind(TimeUnit.class).annotatedWith(Names.named("RateUnit")).toInstance(TimeUnit.SECONDS);
		bind(TimeUnit.class).annotatedWith(Names.named("DurationUnit")).toInstance(TimeUnit.MILLISECONDS);
		bind(String.class).annotatedWith(Names.named("AllowedOrigin")).toInstance("*");
		bind(Boolean.class).annotatedWith(Names.named("ShowSamples")).toInstance(false);

		serve("/_stats").with(MetricsServlet.class);
		serve("/_ping").with(HeartbeatServlet.class);

		filter("/*").through(MetricsFilter.class);
	}
}


