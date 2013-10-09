package de.avgl.dmp.controller.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.json.MetricsModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;


/**
 * A servlet which returns the metrics in a given registry as an {@code application/json} response.
 */
@Singleton
public class MetricsServlet extends HttpServlet {

	private static final long serialVersionUID = 1049773947734939602L;
	private static final String CONTENT_TYPE = "application/json";

	private final String allowedOrigin;
	private final MetricRegistry registry;
	private final ObjectMapper mapper;

	@Inject
	public MetricsServlet(final MetricRegistry registry,
						  @Named("RateUnit") final TimeUnit rateUnit,
						  @Named("DurationUnit") final TimeUnit durationUnit,
						  @Named("AllowedOrigin") final String allowedOrigin,
						  @Named("ShowSamples") final Boolean showSamples) {

		this.registry = registry;
		this.allowedOrigin = allowedOrigin;

		this.mapper = new ObjectMapper().registerModule(
				new MetricsModule(rateUnit, durationUnit, showSamples));
	}

	@Override
	protected void doGet(final HttpServletRequest req,
						 final HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType(CONTENT_TYPE);
		resp.setCharacterEncoding("UTF-8");

		if (allowedOrigin != null) {
			resp.setHeader("Access-Control-Allow-Origin", allowedOrigin);
		}
		resp.setHeader("Cache-Control", "must-revalidate,no-cache,no-store");
		resp.setStatus(HttpServletResponse.SC_OK);

		final OutputStream output = resp.getOutputStream();
		try {
			getWriter(req).writeValue(output, registry);
		} finally {
			output.close();
		}
	}

	private ObjectWriter getWriter(final HttpServletRequest request) {
		final boolean prettyPrint = Boolean.parseBoolean(request.getParameter("pretty"));
		if (prettyPrint) {
			return mapper.writerWithDefaultPrettyPrinter();
		}
		return mapper.writer();
	}
}

