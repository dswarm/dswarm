package de.avgl.dmp.persistence.model.resource.utils;

import java.net.URI;

import com.fasterxml.jackson.databind.JsonNode;

import de.avgl.dmp.persistence.model.resource.DataModel;

/**
 * A utility class for {@link DataModel}s and related entities.
 *
 * @author tgaengler
 */
public final class DataModelUtils {

	public static String determineDataResourceSchemaBaseURI(final DataModel dataModel) {

		final String dataResourceBaseURI = determineDataResourceBaseURI(dataModel);

		if(dataResourceBaseURI == null) {

			return null;
		}

		if(dataResourceBaseURI.endsWith("/")) {

			return dataResourceBaseURI + "schema#";
		}

		return dataResourceBaseURI + "/schema#";
	}

	public static String determineDataResourceBaseURI(final DataModel dataModel) {

		// create data resource base uri
		final JsonNode path = dataModel.getDataResource().getAttribute("path");
		final String dataResourceFilePath = path == null ? "/UnknownResource" : path.asText();
		final String dataResourceName = dataResourceFilePath.substring(dataResourceFilePath.lastIndexOf("/"), dataResourceFilePath.length());

		final String dataResourceBaseURI;

		URI uri = null;

		try {

			uri = URI.create(dataResourceName);
		} catch (final Exception e) {

			e.printStackTrace();
		}

		if (uri != null && uri.getScheme() != null) {

			// data resource name could act as base uri

			dataResourceBaseURI = dataResourceName;
		} else {

			// create uri with help of given data resource id

			final StringBuilder sb = new StringBuilder();

			if (dataModel.getDataResource().getId() != null) {

				// create uri from resource id

				sb.append("http://data.slub-dresden.de/resources/").append(dataModel.getDataResource().getId());
			} else {

				// create uri from data resource name

				sb.append("http://data.slub-dresden.de/resources/").append(dataResourceName);
			}

			dataResourceBaseURI = sb.toString();
		}

		return dataResourceBaseURI;
	}
}
