package de.avgl.dmp.controller.servlet.filter;

import java.util.HashMap;
import java.util.Map;

import com.codahale.metrics.servlet.AbstractInstrumentedFilter;
import com.codahale.metrics.servlets.MetricsServlet;

public class InstrumentedFilterImpl extends AbstractInstrumentedFilter {

	public static final String REGISTRY_ATTRIBUTE = MetricsServlet.class.getName() + ".registry";

	private static final String NAME_PREFIX = "responseCodes.";
	private static final int OK = 200;
	private static final int CREATED = 201;
	private static final int NO_CONTENT = 204;
	private static final int BAD_REQUEST = 400;
	private static final int NOT_FOUND = 404;
	private static final int SERVER_ERROR = 500;

	/**
	 * Creates a new instance of the filter.
	 */
	public InstrumentedFilterImpl() {
		super(REGISTRY_ATTRIBUTE, createMeterNamesByStatusCode(), NAME_PREFIX + "other");
	}

	private static Map<Integer, String> createMeterNamesByStatusCode() {
		final Map<Integer, String> meterNamesByStatusCode = new HashMap<Integer, String>(6);
		meterNamesByStatusCode.put(OK, NAME_PREFIX + "ok");
		meterNamesByStatusCode.put(CREATED, NAME_PREFIX + "created");
		meterNamesByStatusCode.put(NO_CONTENT, NAME_PREFIX + "noContent");
		meterNamesByStatusCode.put(BAD_REQUEST, NAME_PREFIX + "badRequest");
		meterNamesByStatusCode.put(NOT_FOUND, NAME_PREFIX + "notFound");
		meterNamesByStatusCode.put(SERVER_ERROR, NAME_PREFIX + "serverError");
		return meterNamesByStatusCode;
	}
}
