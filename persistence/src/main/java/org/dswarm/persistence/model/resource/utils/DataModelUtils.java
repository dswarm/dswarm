/**
 * Copyright (C) 2013 – 2015 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dswarm.persistence.model.resource.utils;

import java.io.File;
import java.net.URI;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.graph.json.Predicate;
import org.dswarm.graph.json.ResourceNode;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.utils.ExtendedBasicDMPJPAObjectUtils;
import org.dswarm.persistence.util.GDMUtil;

/**
 * A utility class for {@link DataModel}s and related entities.
 * 
 * @author tgaengler
 */
public final class DataModelUtils extends ExtendedBasicDMPJPAObjectUtils<DataModel> {

	private static final Logger	LOG	= LoggerFactory.getLogger(DataModelUtils.class);

	public static String determineDataModelSchemaBaseURI(final DataModel dataModel) {

		final String dataResourceBaseURI = DataModelUtils.determineDataModelBaseURI(dataModel);

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

			DataModelUtils.LOG
					.error("data model shouldn't be really null at data model base uri determination; however I'll create a uri with a random uuid instead ...");

			final StringBuilder sb = new StringBuilder();

			// create uri with random uuid

			sb.append("http://data.slub-dresden.de/datamodels/").append(UUID.randomUUID());

			return sb.toString();
		}

		// create data resource base uri
		URI uri = null;
		String dataResourceName = null;

		final org.dswarm.persistence.model.resource.Resource dataResource = dataModel.getDataResource();

		if (dataResource != null) {

			final JsonNode pathNode = dataResource.getAttribute("path");

			if (pathNode != null) {

				final String path = pathNode.asText();

				if (path != null) {

					// FIXME DD-533 "/" does not work for windows paths which are "\" (or escaped as "\\")
					if (path.contains(File.separator)) {

						// e.g. C:\DMP\datamanagement-platform\converter\target\test-classes\test_csv.csv
						// return /test_csv.csv
						dataResourceName = "/" + path.substring(path.lastIndexOf(File.separator) + 1, path.length());

					} else {

						dataResourceName = path.substring(path.lastIndexOf("/"), path.length());
					}

					try {

						uri = URI.create(dataResourceName);
					} catch (final Exception e) {

						DataModelUtils.LOG.debug("couldn't create uri from data resource path", e);
					}
				} else {

					DataModelUtils.LOG.warn("The data model [" + dataModel.getId() + "] is missing the data resource path string");
				}
			} else {

				DataModelUtils.LOG.warn("The data model [" + dataModel.getId() + "] is missing the data resource path");
			}
		} else {

			DataModelUtils.LOG.warn("The data model [" + dataModel.getId() + "] is missing the data resource");
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

	public static org.dswarm.graph.json.Resource mintRecordResource(final Long identifier, final DataModel dataModel,
			final Map<Long, org.dswarm.graph.json.Resource> recordResources, final org.dswarm.graph.json.Model model,
			final ResourceNode recordClassNode) {

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
		final org.dswarm.graph.json.Resource recordResource = new org.dswarm.graph.json.Resource(recordURI);

		if (identifier != null) {

			recordResources.put(identifier, recordResource);
		}

		// add resource type statement to model
		recordResource.addStatement(new ResourceNode(recordResource.getUri()), new Predicate(GDMUtil.RDF_type), recordClassNode);
		model.addResource(recordResource);

		return recordResource;
	}
}
