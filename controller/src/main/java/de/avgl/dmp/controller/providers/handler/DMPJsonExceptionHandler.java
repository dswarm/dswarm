package de.avgl.dmp.controller.providers.handler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import de.avgl.dmp.controller.DMPJsonException;
import de.avgl.dmp.controller.providers.BaseExceptionHandler;

/**
 * An exception handler for providing JSON exceptions at client side of the backend API
 * 
 * @author phorn
 *
 */
@Provider
public class DMPJsonExceptionHandler extends BaseExceptionHandler<DMPJsonException> {

	private static final Pattern PATTERN = Pattern.compile("Unrecognized field (\"[^\"]+?\") \\(class [\\S]+?\\), not marked as ignorable \\((\\d+) known properties: , ([^\\)]+?)\\]\\).*", Pattern.DOTALL | Pattern.MULTILINE);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Response toResponse(final DMPJsonException exception) {

		final String message = errorMessage(exception);

		final Throwable exceptionCause = exception.getCause();
		final Matcher matcher = PATTERN.matcher(exceptionCause.getMessage());

		if (matcher.matches() && matcher.groupCount() >= 3) {

			final String unknownField = matcher.group(1);
			final String numFields = matcher.group(2);
			final String availFields = matcher.group(3);

			final String clientMessage = String.format("Unknown Field %s, must use one of the %s: {%s}", unknownField, numFields, availFields);

			return createResponse(clientMessage, 400);
		}

		return createResponse(message);
	}
}
