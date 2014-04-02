package de.avgl.dmp.persistence.model.resource.utils;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.RDF;

import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.resource.DataModel;
import de.avgl.dmp.persistence.model.utils.ExtendedBasicDMPJPAObjectUtils;

/**
 * A utility class for {@link DataModel}s and related entities.
 *
 * @author tgaengler
 */
public final class DataModelUtils extends ExtendedBasicDMPJPAObjectUtils<DataModel> {

	private static final org.apache.log4j.Logger		LOG						= org.apache.log4j.Logger.getLogger(DataModelUtils.class);

	public static String determineDataResourceSchemaBaseURI(final DataModel dataModel) {

		final String dataResourceBaseURI = determineDataResourceBaseURI(dataModel);

		if (dataResourceBaseURI == null) {

			return null;
		}

		if (dataResourceBaseURI.endsWith("/")) {

			return dataResourceBaseURI + "schema#";
		}

		return dataResourceBaseURI + "/schema#";
	}

	public static String determineDataResourceBaseURI(final DataModel dataModel) {

		// create data resource base uri
		final de.avgl.dmp.persistence.model.resource.Resource dataResource = dataModel.getDataResource();

		if (dataResource == null) {
			LOG.warn("The data model ["+ dataModel.getId() +"] is missing the data resource");
			return null;
		}

		final JsonNode path = dataResource.getAttribute("path");
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

			if (dataResource.getId() != null) {

				// create uri from resource id

				sb.append("http://data.slub-dresden.de/resources/").append(dataResource.getId());
			} else {

				// create uri from data resource name

				sb.append("http://data.slub-dresden.de/resources/").append(dataResourceName);
			}

			dataResourceBaseURI = sb.toString();
		}

		return dataResourceBaseURI;
	}

	public static Resource mintRecordResource(final Long identifier, final DataModel dataModel, final Map<Long, Resource> recordResources,
			final Model model, final String recordClassURI) {

		if (identifier != null) {

			if (recordResources.containsKey(identifier)) {

				return recordResources.get(identifier);
			}
		}

		// mint completely new uri

		final StringBuilder sb = new StringBuilder();

		if (dataModel != null) {

			// create uri from resource id and configuration id and random uuid

			sb.append("http://data.slub-dresden.de/datamodels/").append(dataModel.getId()).append("/records/");
		} else {

			// create uri from random uuid

			sb.append("http://data.slub-dresden.de/records/");
		}

		final String recordURI = sb.append(UUID.randomUUID()).toString();
		final Resource recordResource = ResourceFactory.createResource(recordURI);

		if (identifier != null) {

			recordResources.put(identifier, recordResource);
		}

		// add resource type statement to model
		model.add(recordResource, RDF.type, ResourceFactory.createResource(recordClassURI));

		return recordResource;
	}
}
