package de.avgl.dmp.controller.servlet.filter;

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

import static com.codahale.metrics.MetricRegistry.name;

@Singleton
public class MetricsFilter implements Filter {

	private final ConcurrentMap<Integer, Meter> metersByStatusCode;
	private final Meter otherMeter;
	private final Counter activeRequests;
	private final Timer requestTimer;

	/**
	 * Creates a new instance of the filter.
	 */
	@Inject
	protected MetricsFilter(MetricRegistry metricRegistry) {

		final String NAME_PREFIX = "responseCodes.";
		final String otherMetricName = NAME_PREFIX + "other";

		final Map<Integer, String> meterNamesByStatusCode = new HashMap<Integer, String>(13);

		meterNamesByStatusCode.put(HttpServletResponse.SC_OK, NAME_PREFIX + "ok");
		meterNamesByStatusCode.put(HttpServletResponse.SC_CREATED, NAME_PREFIX + "created");
		meterNamesByStatusCode.put(HttpServletResponse.SC_NO_CONTENT, NAME_PREFIX + "noContent");

		meterNamesByStatusCode.put(HttpServletResponse.SC_BAD_REQUEST, NAME_PREFIX + "badRequest");
		meterNamesByStatusCode.put(HttpServletResponse.SC_NOT_FOUND, NAME_PREFIX + "notFound");
		meterNamesByStatusCode.put(HttpServletResponse.SC_METHOD_NOT_ALLOWED, NAME_PREFIX + "methodNotAllowed");
		meterNamesByStatusCode.put(HttpServletResponse.SC_NOT_ACCEPTABLE, NAME_PREFIX + "notAcceptable");
		meterNamesByStatusCode.put(HttpServletResponse.SC_REQUEST_TIMEOUT, NAME_PREFIX + "requestTimeout");
		meterNamesByStatusCode.put(HttpServletResponse.SC_CONFLICT, NAME_PREFIX + "conflict");
		meterNamesByStatusCode.put(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, NAME_PREFIX + "requestEntityTooLarge");
		meterNamesByStatusCode.put(HttpServletResponse.SC_REQUEST_URI_TOO_LONG, NAME_PREFIX + "requestUriTooLong");
		meterNamesByStatusCode.put(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, NAME_PREFIX + "unsupportedMediaType");

		meterNamesByStatusCode.put(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, NAME_PREFIX + "serverError");


		final ConcurrentMap<Integer, Meter> metersByStatusCode = new ConcurrentHashMap<Integer, Meter>(meterNamesByStatusCode.size());
		for (Map.Entry<Integer, String> entry : meterNamesByStatusCode.entrySet()) {
			metersByStatusCode.put(entry.getKey(),
					metricRegistry.meter(name(MetricsFilter.class, entry.getValue())));
		}
		this.metersByStatusCode = metersByStatusCode;

		this.otherMeter = metricRegistry.meter(name(MetricsFilter.class, otherMetricName));
		this.activeRequests = metricRegistry.counter(name(MetricsFilter.class, "activeRequests"));
		this.requestTimer = metricRegistry.timer(name(MetricsFilter.class, "requests"));
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {}

	@Override
	public void destroy() {}

	@Override
	public void doFilter(ServletRequest request,
						 ServletResponse response,
						 FilterChain chain) throws IOException, ServletException {
		final StatusExposingServletResponse wrappedResponse =
				new StatusExposingServletResponse((HttpServletResponse) response);
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

	private void markMeterForStatusCode(int status) {
		final Meter metric = metersByStatusCode.get(status);
		if (metric != null) {
			metric.mark();
		} else {
			otherMeter.mark();
		}
	}

	private static class StatusExposingServletResponse extends HttpServletResponseWrapper {
		// The Servlet spec says: calling setStatus is optional, if no status is set, the default is 200.
		private int httpStatus = 200;

		public StatusExposingServletResponse(HttpServletResponse response) {
			super(response);
		}

		@Override
		public void sendError(int sc) throws IOException {
			httpStatus = sc;
			super.sendError(sc);
		}

		@Override
		public void sendError(int sc, String msg) throws IOException {
			httpStatus = sc;
			super.sendError(sc, msg);
		}

		@Override
		public void setStatus(int sc) {
			httpStatus = sc;
			super.setStatus(sc);
		}

		public int getStatus() {
			return httpStatus;
		}
	}
}
