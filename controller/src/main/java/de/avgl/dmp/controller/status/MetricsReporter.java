package de.avgl.dmp.controller.status;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.elasticsearch.metrics.ElasticsearchReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public final class MetricsReporter {

	private static final Logger LOG = LoggerFactory.getLogger(MetricsReporter.class);

	private final MetricRegistry registry;
	private final String esHost;
	private final int reportIntervalInSeconds;

	@Inject
	public MetricsReporter(final MetricRegistry registry, @Named("reporting.es.host") final String esHost, @Named("reporting.interval") final Integer reportIntervalInSeconds) {
		this.registry = registry;
		this.esHost = esHost;
		this.reportIntervalInSeconds = reportIntervalInSeconds;
	}

	public void start() throws IOException {
		LOG.trace("reporting metrics to Elasticsearch at {} every {} seconds", esHost, reportIntervalInSeconds);
		final ElasticsearchReporter reporter = ElasticsearchReporter
				.forRegistry(registry).hosts(esHost).build();

		reporter.start(reportIntervalInSeconds, TimeUnit.SECONDS);
	}
}
