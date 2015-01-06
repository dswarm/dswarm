/**
 * Copyright (C) 2013 – 2015 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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
package org.dswarm.controller.servlet.filter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * The filter for the metrics measuring.
 * 
 * @author phorn
 */
@Singleton
public class MetricsFilter implements Filter {

	private final ConcurrentMap<Integer, Meter>	metersByStatusCode;
	private final Meter							otherMeter;
	private final Counter						activeRequests;
	private final Timer							requestTimer;

	/**
	 * Creates a new instance of the filter.
	 */
	@SuppressWarnings("StringConcatenationMissingWhitespace")
	@Inject
	protected MetricsFilter(final MetricRegistry metricRegistry) {

		final String namePrefix = "responseCodes.";
		final String otherMetricName = namePrefix + "other";

		final Map<Integer, String> meterNamesByStatusCode = new HashMap<>(13);

		meterNamesByStatusCode.put(HttpServletResponse.SC_OK, namePrefix + "ok");
		meterNamesByStatusCode.put(HttpServletResponse.SC_CREATED, namePrefix + "created");
		meterNamesByStatusCode.put(HttpServletResponse.SC_NO_CONTENT, namePrefix + "noContent");

		meterNamesByStatusCode.put(HttpServletResponse.SC_BAD_REQUEST, namePrefix + "badRequest");
		meterNamesByStatusCode.put(HttpServletResponse.SC_NOT_FOUND, namePrefix + "notFound");
		meterNamesByStatusCode.put(HttpServletResponse.SC_METHOD_NOT_ALLOWED, namePrefix + "methodNotAllowed");
		meterNamesByStatusCode.put(HttpServletResponse.SC_NOT_ACCEPTABLE, namePrefix + "notAcceptable");
		meterNamesByStatusCode.put(HttpServletResponse.SC_REQUEST_TIMEOUT, namePrefix + "requestTimeout");
		meterNamesByStatusCode.put(HttpServletResponse.SC_CONFLICT, namePrefix + "conflict");
		meterNamesByStatusCode.put(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, namePrefix + "requestEntityTooLarge");
		meterNamesByStatusCode.put(HttpServletResponse.SC_REQUEST_URI_TOO_LONG, namePrefix + "requestUriTooLong");
		meterNamesByStatusCode.put(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, namePrefix + "unsupportedMediaType");

		meterNamesByStatusCode.put(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, namePrefix + "serverError");

		final ConcurrentMap<Integer, Meter> metersByStatusCode = new ConcurrentHashMap<>(meterNamesByStatusCode.size());
		for (final Map.Entry<Integer, String> entry : meterNamesByStatusCode.entrySet()) {
			metersByStatusCode.put(entry.getKey(), metricRegistry.meter(MetricRegistry.name(MetricsFilter.class, entry.getValue())));
		}
		this.metersByStatusCode = metersByStatusCode;

		otherMeter = metricRegistry.meter(MetricRegistry.name(MetricsFilter.class, otherMetricName));
		activeRequests = metricRegistry.counter(MetricRegistry.name(MetricsFilter.class, "activeRequests"));
		requestTimer = metricRegistry.timer(MetricRegistry.name(MetricsFilter.class, "requests"));
	}

	@Override
	public void init(final FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
		final StatusExposingServletResponse wrappedResponse = new StatusExposingServletResponse((HttpServletResponse) response);
		activeRequests.inc();
		final Timer.Context context = requestTimer.time();
		try {
			chain.doFilter(request, wrappedResponse);
		} finally {
			context.stop();
			activeRequests.dec();
			markMeterForStatusCode(wrappedResponse.getStatus());
		}
	}

	private void markMeterForStatusCode(final int status) {
		final Meter metric = metersByStatusCode.get(status);
		if (metric != null) {
			metric.mark();
		} else {
			otherMeter.mark();
		}
	}

	private static class StatusExposingServletResponse extends HttpServletResponseWrapper {

		// The Servlet spec says: calling setStatus is optional, if no status is set, the default is 200.
		private int	httpStatus	= 200;

		public StatusExposingServletResponse(final HttpServletResponse response) {
			super(response);
		}

		@Override
		public void sendError(final int sc) throws IOException {
			httpStatus = sc;
			super.sendError(sc);
		}

		@Override
		public void sendError(final int sc, final String msg) throws IOException {
			httpStatus = sc;
			super.sendError(sc, msg);
		}

		@Override
		public void setStatus(final int sc) {
			httpStatus = sc;
			super.setStatus(sc);
		}

		@Override
		public int getStatus() {
			return httpStatus;
		}
	}
}
