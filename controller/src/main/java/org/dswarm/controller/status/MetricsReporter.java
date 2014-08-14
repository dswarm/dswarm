package org.dswarm.controller.status;

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

	private static final Logger		LOG	= LoggerFactory.getLogger(MetricsReporter.class);

	private final MetricRegistry	registry;
	private final boolean			isEnabled;
	private final String			esHost;
	private final long				reportIntervalInMillis;

	@Inject
	public MetricsReporter(
			final MetricRegistry registry,
			@Named("dswarm.reporting.enabled") final boolean isEnabled,
			@Named("dswarm.reporting.interval") final long reportIntervalInMillis,
			@Named("dswarm.reporting.elasticsearch.host") final String esHost) {
		this.registry = registry;
		this.isEnabled = isEnabled;
		this.esHost = esHost;
		this.reportIntervalInMillis = reportIntervalInMillis;
	}

	public void start() throws IOException {
		if (isEnabled) {
			MetricsReporter.LOG.trace("reporting metrics to Elasticsearch at {} every {} milliseconds", esHost, reportIntervalInMillis);
			final ElasticsearchReporter reporter = ElasticsearchReporter.forRegistry(registry).hosts(esHost).build();
			reporter.start(reportIntervalInMillis, TimeUnit.MILLISECONDS);
		}
	}
}
