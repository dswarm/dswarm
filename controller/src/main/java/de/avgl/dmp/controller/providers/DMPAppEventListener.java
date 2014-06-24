package de.avgl.dmp.controller.providers;

import java.io.IOException;
import javax.inject.Inject;

import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.avgl.dmp.controller.status.MetricsReporter;


public class DMPAppEventListener implements ApplicationEventListener {

	private static final Logger LOG = LoggerFactory.getLogger(DMPAppEventListener.class);

	private final MetricsReporter metricsReporter;

	@Inject
	public DMPAppEventListener(final MetricsReporter metricsReporter) {
		this.metricsReporter = metricsReporter;
	}

	@Override
	public void onEvent(final ApplicationEvent event) {
		if (event.getType() == ApplicationEvent.Type.INITIALIZATION_FINISHED) {
			onStart();
		}
	}

	@Override
	public RequestEventListener onRequest(final RequestEvent requestEvent) {
		return null;
	}

	private void onStart() {
		try {
			LOG.trace("starting metrics reporting");
			metricsReporter.start();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}
}
