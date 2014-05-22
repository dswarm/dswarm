package de.avgl.dmp.controller;

import org.glassfish.grizzly.http.server.DefaultErrorPageGenerator;
import org.glassfish.grizzly.http.server.ErrorPageGenerator;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.util.HtmlHelper;
import org.glassfish.grizzly.http.util.HttpStatus;

/**
 * Created by tgaengler on 21/05/14.
 */
public class DMPErrorPageGenerator extends DefaultErrorPageGenerator {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String generate(final Request request,
			final int status, final String reasonPhrase,
			final String description, final Throwable exception) {

		if (status == 404) {

			return null;
		}

		return super.generate(request, status, reasonPhrase, description, exception);
	}
}

