package de.avgl.dmp.persistence.model.schema.utils;

import de.avgl.dmp.persistence.model.schema.Schema;
import de.avgl.dmp.persistence.model.utils.BasicDMPJPAObjectUtils;

/**
 * @author tgaengler
 */
public final class SchemaUtils extends BasicDMPJPAObjectUtils<Schema> {

	public static String determineRelativeURIPart(final String uri) {

		final String lastPartDelimiter;

		if (uri.lastIndexOf("#") > 0) {

			lastPartDelimiter = "#";
		} else if (uri.lastIndexOf("/") > 0) {

			lastPartDelimiter = "/";
		} else {

			lastPartDelimiter = null;
		}

		final String relativeURIPart;

		if (lastPartDelimiter != null) {

			relativeURIPart = uri.substring(uri.lastIndexOf(lastPartDelimiter) + 1, uri.length());
		} else {

			relativeURIPart = uri;
		}

		return relativeURIPart;
	}

}
