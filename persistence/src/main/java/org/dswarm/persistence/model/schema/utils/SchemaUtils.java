package org.dswarm.persistence.model.schema.utils;

import com.google.common.net.UrlEscapers;

import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.model.utils.BasicDMPJPAObjectUtils;

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

	public static String determineSchemaURI(final Long schemaId) {

		return "http://data.slub-dresden.de/schemas/" + schemaId + "/";
	}

	public static String mintAttributeURI(final String attributeName, final String schemaBaseURI) {

		final String attributeNameURLEncoded = UrlEscapers.urlFormParameterEscaper().escape(attributeName);

		return schemaBaseURI + attributeNameURLEncoded;
	}

}
