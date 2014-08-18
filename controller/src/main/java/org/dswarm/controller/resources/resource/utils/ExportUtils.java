package org.dswarm.controller.resources.resource.utils;

import java.io.InputStream;
import java.util.List;

import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;

import org.dswarm.controller.DMPControllerException;

/**
 * @author reichert
 */
public class ExportUtils {

	public static final String	CONTENT_DISPOSITION	= "Content-Disposition";

	/**
	 * Process a response received from graph db and prepare a new response to be sent to a requester by "forwarding" the graph db
	 * resonse's content and Content-Disposition header
	 * 
	 * @param responseFromGraph response received from graph db
	 * @return a new response to be sent to a requester by "forwarding" the graph db resonse's content and Content-Disposition
	 *         header
	 * @throws DMPControllerException for two reasons<br />
	 *             in case the status code of the response received from graph db is not 200 or 406<br />
	 *             in case the Content-Disposition is not as expected
	 */
	public static Response processGraphDBResponseInternal(final Response responseFromGraph) throws DMPControllerException {

		Response responseToRequester;

		switch (responseFromGraph.getStatus()) {

			case HttpStatus.SC_OK:
				final InputStream result = responseFromGraph.readEntity(InputStream.class);

				List<String> contentDispositionList = responseFromGraph.getStringHeaders().get(ExportUtils.CONTENT_DISPOSITION);
				if (contentDispositionList == null || contentDispositionList.size() != 1) {
					throw new DMPControllerException("Couldn't export data from database. Database endpoint did not provide a valid file.");
				}
				final String contenDispositionValue = contentDispositionList.get(0);

				responseToRequester = Response.ok(result, responseFromGraph.getMediaType())
						.header(ExportUtils.CONTENT_DISPOSITION, contenDispositionValue).build();
				break;

			case HttpStatus.SC_NOT_ACCEPTABLE:
				responseToRequester = Response.status(HttpStatus.SC_NOT_ACCEPTABLE).build();
				break;

			default:
				// TODO forward GE HTTP Response, e.g. if the requested format is not supported
				throw new DMPControllerException("Couldn't export data from database. Received status code '" + responseFromGraph.getStatus()
						+ "' from database endpoint.");

		}
		return responseToRequester;
	}

}
