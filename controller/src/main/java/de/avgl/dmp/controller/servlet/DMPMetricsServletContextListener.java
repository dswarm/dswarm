package de.avgl.dmp.controller.servlet;

import java.util.concurrent.TimeUnit;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.servlets.MetricsServlet;

public class DMPMetricsServletContextListener extends MetricsServlet.ContextListener {

	@Override
	protected MetricRegistry getMetricRegistry() {
		if (DMPInjector.injector != null) {
			return DMPInjector.injector.getInstance(MetricRegistry.class);
		}
		return new MetricRegistry();
	}

	@Override
	protected TimeUnit getRateUnit() {
		return TimeUnit.SECONDS;
	}

	@Override
	protected TimeUnit getDurationUnit() {
		return TimeUnit.MILLISECONDS;
	}

	@Override
	protected String getAllowedOrigin() {
		return "*";
	}
}
