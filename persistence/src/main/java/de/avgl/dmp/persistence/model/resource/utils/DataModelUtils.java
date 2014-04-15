package de.avgl.dmp.persistence.model.resource.utils;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.RDF;

import de.avgl.dmp.graph.json.Predicate;
import de.avgl.dmp.graph.json.ResourceNode;
import de.avgl.dmp.persistence.model.resource.DataModel;
import de.avgl.dmp.persistence.model.utils.ExtendedBasicDMPJPAObjectUtils;

/**
 * A utility class for {@link DataModel}s and related entities.
 * 
 * @author tgaengler
 */
public final class DataModelUtils extends ExtendedBasicDMPJPAObjectUtils<DataModel> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(DataModelUtils.class);

	public static String determineDataModelSchemaBaseURI(final DataModel dataModel) {

		final String dataResourceBaseURI = determineDataModelBaseURI(dataModel);

		if (dataResourceBaseURI == null) {

			return null;
		}

		if (dataResourceBaseURI.endsWith("/")) {

			return dataResourceBaseURI + "schema#";
		}

		return dataResourceBaseURI + "/schema#";
	}

	public static String determineDataModelBaseURI(final DataModel dataModel) {

		if (dataModel == null) {

			LOG.error("data model shouldn't be null at data model base uri determination");

			return null;
		}

		// create data resource base uri
		URI uri = null;
		String dataResourceName = null;

		final de.avgl.dmp.persistence.model.resource.Resource dataResource = dataModel.getDataResource();

		if (dataResource != null) {

			final JsonNode pathNode = dataResource.getAttribute("path");

			if (pathNode != null) {

				final String path = pathNode.asText();

				if (path != null) {

					dataResourceName = path.substring(path.lastIndexOf("/"), path.length());

					try {

						uri = URI.create(dataResourceName);
					} catch (final Exception e) {

						LOG.debug("couldn't create uri from data resource path", e);
					}
				} else {

					LOG.warn("The data model [" + dataModel.getId() + "] is missing the data resource path string");
				}
			} else {

				LOG.warn("The data model [" + dataModel.getId() + "] is missing the data resource path");
			}
		} else {

			LOG.warn("The data model [" + dataModel.getId() + "] is missing the data resource");
		}

		final String dataResourceBaseURI;

		if (uri != null && uri.getScheme() != null) {

			// data resource name could act as base uri

			dataResourceBaseURI = dataResourceName;
		} else {

			// create uri with help of given data resource id or data model id

			final StringBuilder sb = new StringBuilder();

			if (dataResource != null && dataResource.getId() != null) {

				// create uri from resource id

				sb.append("http://data.slub-dresden.de/resources/").append(dataResource.getId());
			}

			// TODO: this is wrong, or? - "create uri from data resource name" -> this will result in ugly uris ...

			// else if (dataResourceName != null) {
			//
			// // create uri from data resource name
			//
			// // TODO: (probably) replace whitespaces etc.
			//
			// sb.append("http://data.slub-dresden.de/resources/").append(dataResourceName);
			// }
			else if (dataModel.getId() != null) {

				// create uri from data model id

				sb.append("http://data.slub-dresden.de/datamodels/").append(dataModel.getId());
			} else {

				// create uri with random uuid

				sb.append("http://data.slub-dresden.de/datamodels/").append(UUID.randomUUID());
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
	
	public static de.avgl.dmp.graph.json.Resource mintRecordResource(final Long identifier, final DataModel dataModel, final Map<Long, de.avgl.dmp.graph.json.Resource> recordResources,
			final de.avgl.dmp.graph.json.Model model, final ResourceNode recordClassNode) {

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
		final de.avgl.dmp.graph.json.Resource recordResource = new de.avgl.dmp.graph.json.Resource(recordURI);

		if (identifier != null) {

			recordResources.put(identifier, recordResource);
		}

		// add resource type statement to model
		recordResource.addStatement(new ResourceNode(recordResource.getUri()), new Predicate(RDF.type.getURI()), recordClassNode);
		model.addResource(recordResource);

		return recordResource;
	}
}
