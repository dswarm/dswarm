/**
 * Copyright (C) 2013 – 2017 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
	private final String			index;
	private final long				reportIntervalInMillis;

	@Inject
	public MetricsReporter(
			final MetricRegistry registry,
			@Named("dswarm.reporting.enabled") final boolean isEnabled,
			@Named("dswarm.reporting.interval") final long reportIntervalInMillis,
			@Named("dswarm.reporting.elasticsearch.index") final String index,
			@Named("dswarm.reporting.elasticsearch.host") final String esHost) {
		this.registry = registry;
		this.isEnabled = isEnabled;
		this.index = index;
		this.esHost = esHost;
		this.reportIntervalInMillis = reportIntervalInMillis;
	}

	public void start() throws IOException {
		if (isEnabled) {
			MetricsReporter.LOG.trace("reporting metrics to Elasticsearch at {} every {} milliseconds", esHost, reportIntervalInMillis);
			final ElasticsearchReporter reporter = ElasticsearchReporter.forRegistry(registry).hosts(esHost).index(index).build();
			reporter.start(reportIntervalInMillis, TimeUnit.MILLISECONDS);
		}
	}
}
