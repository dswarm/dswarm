package de.avgl.dmp.controller.servlet;


import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * An HTTP servlet which outputs a {@code text/plain} {@code "pong"} response.
 */
public class HeartbeatServlet extends HttpServlet {
	private static final long serialVersionUID = 3772654177231086757L;
	private static final String CONTENT_TYPE = "text/plain";
	private static final String CONTENT = "pong";
	private static final String CACHE_CONTROL = "Cache-Control";
	private static final String NO_CACHE = "must-revalidate,no-cache,no-store";
	private static final String ACCESS_CONTROL = "Access-Control-Allow-Origin";
	private static final String CORS = "*";

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setHeader(CACHE_CONTROL, NO_CACHE);
		resp.setHeader(ACCESS_CONTROL, CORS);
		resp.setContentType(CONTENT_TYPE);
		resp.setCharacterEncoding("UTF-8");
		final PrintWriter writer = resp.getWriter();
		try {
			writer.print(CONTENT);
		} finally {
			writer.close();
		}
	}
}
