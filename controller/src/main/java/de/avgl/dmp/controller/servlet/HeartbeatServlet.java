package de.avgl.dmp.controller.servlet;


import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;


/**
 * An HTTP servlet which outputs a {@code text/plain} {@code "pong"} response.
 */
@Singleton
public class HeartbeatServlet extends HttpServlet {
	private static final long serialVersionUID = 3772654177231086757L;
	private static final String CONTENT_TYPE = "text/plain";
	private static final String CONTENT = "pong";

	private final String allowedOrigin;

	@Inject
	public HeartbeatServlet(@Named("AllowedOrigin") String allowedOrigin) {

		this.allowedOrigin = allowedOrigin;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType(CONTENT_TYPE);
		resp.setCharacterEncoding("UTF-8");

		if (allowedOrigin != null) {
			resp.setHeader("Access-Control-Allow-Origin", allowedOrigin);
		}
		resp.setHeader("Cache-Control", "must-revalidate,no-cache,no-store");
		resp.setStatus(HttpServletResponse.SC_OK);

		final PrintWriter writer = resp.getWriter();
		try {
			writer.print(CONTENT);
		} finally {
			writer.close();
		}
	}
}
