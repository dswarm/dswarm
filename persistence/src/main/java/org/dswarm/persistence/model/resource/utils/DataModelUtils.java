/**
 * Copyright (C) 2013 – 2017 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.model.utils.ExtendedBasicDMPJPAObjectUtils;

/**
 * A utility class for {@link DataModel}s and related entities.
 *
 * @author tgaengler
 */
public final class DataModelUtils extends ExtendedBasicDMPJPAObjectUtils<DataModel> {

	private static final Logger LOG = LoggerFactory.getLogger(DataModelUtils.class);

	public static final String BIBRM_CONTRACT_DATA_MODEL_UUID = "DataModel-7e170c22-1371-4836-9a09-515524a1a8d5";
	public static final String BIBO_DOCUMENT_DATA_MODEL_UUID = "DataModel-cf998267-392a-4d87-a33a-88dd1bffb016";
	public static final String MABXML_DATA_MODEL_UUID = "DataModel-4f399d11-81ae-45af-b2f4-645aa177ab85";
	public static final String FOAF_PERSON_DATA_MODEL_UUID = "DataModel-23451d9d-adf6-4352-90f8-4f17cccf5d36";
	public static final String PNX_DATA_MODEL_UUID = "DataModel-a65018b1-d27b-4125-9eff-5f6fd860079d";
	public static final String MARCXML_DATA_MODEL_UUID = "DataModel-326d3380-258e-43fd-83d2-6a87daa8480a";
	public static final String PICAPLUSXML_DATA_MODEL_UUID = "DataModel-81109aec-8b2c-4744-80d1-a35839e9eadc";
	public static final String PICAPLUSXML_GLOBAL_DATA_MODEL_UUID = "DataModel-06f5ad75-3519-44c7-9fbd-783974de9351";
	public static final String FINC_SOLR_DATA_MODEL_UUID = "5fddf2c5-916b-49dc-a07d-af04020c17f7";
	public static final String OAI_PMH_DC_ELEMENTS_DATA_MODEL_UUID = "DataModel-fbf2e242-0a6b-4306-9264-d0ff420398b1";
	public static final String OAI_PMH_DC_TERMS_DATA_MODEL_UUID = "DataModel-324e9d95-d06d-4bee-9a7e-a492ad8f0880";
	public static final String OAI_PMH_MARCXML_DATA_MODEL_UUID = "DataModel-2ee8ce76-8f17-49b7-a63c-596f88a30ee5";
	public static final String SRU_11_PICAPLUSXML_GLOBAL_DATA_MODEL_UUID = "DataModel-da360000-c95e-457a-8289-c9a6eb74efd4";
	public static final String OAI_PMH_DC_ELEMENTS_AND_EDM_DATA_MODEL_UUID = "DataModel-502401d8-7293-4f12-9e31-4ce2943c222c";
	public static final String UBL_INTERMEDIATE_FORMAT_DATA_MODEL_UUID = "DataModel-490cb323-df46-455a-923b-9b06ca12b3aa";
	public static final String SPRINGER_JOURNALS_DATA_MODEL_UUID = "DataModel-76c9b549-e5ea-4988-8a5b-ba32e3c4c8b1";

	private static final Collection<String> dataModelsToInbuiltSchemata = new ArrayList<>();

	static {

		dataModelsToInbuiltSchemata.add(BIBRM_CONTRACT_DATA_MODEL_UUID);
		dataModelsToInbuiltSchemata.add(BIBO_DOCUMENT_DATA_MODEL_UUID);
		dataModelsToInbuiltSchemata.add(MABXML_DATA_MODEL_UUID);
		dataModelsToInbuiltSchemata.add(FOAF_PERSON_DATA_MODEL_UUID);
		dataModelsToInbuiltSchemata.add(PNX_DATA_MODEL_UUID);
		dataModelsToInbuiltSchemata.add(MARCXML_DATA_MODEL_UUID);
		dataModelsToInbuiltSchemata.add(PICAPLUSXML_DATA_MODEL_UUID);
		dataModelsToInbuiltSchemata.add(PICAPLUSXML_GLOBAL_DATA_MODEL_UUID);
		dataModelsToInbuiltSchemata.add(FINC_SOLR_DATA_MODEL_UUID);
		dataModelsToInbuiltSchemata.add(OAI_PMH_DC_ELEMENTS_DATA_MODEL_UUID);
		dataModelsToInbuiltSchemata.add(OAI_PMH_DC_TERMS_DATA_MODEL_UUID);
		dataModelsToInbuiltSchemata.add(OAI_PMH_MARCXML_DATA_MODEL_UUID);
		dataModelsToInbuiltSchemata.add(SRU_11_PICAPLUSXML_GLOBAL_DATA_MODEL_UUID);
		dataModelsToInbuiltSchemata.add(OAI_PMH_DC_ELEMENTS_AND_EDM_DATA_MODEL_UUID);
		dataModelsToInbuiltSchemata.add(UBL_INTERMEDIATE_FORMAT_DATA_MODEL_UUID);
		dataModelsToInbuiltSchemata.add(SPRINGER_JOURNALS_DATA_MODEL_UUID);
	}

	private static final String BASE_URI = "http://data.slub-dresden.de/";

	private static final String SLASH = "/";
	private static final String HASH = "#";

	private static final String DATAMODELS_DIRECTIVE = "datamodels";
	private static final String RESOURCES_DIRECTIVE = "resources";
	private static final String RECORDS_DIRECTIVE = "records";
	private static final String SCHEMAS_DIRECTIVE = "schemas";

	private static final String SCHEMA_IDENTIFIER = "schema";

	public static Collection<String> getDataModelsToInbuiltSchemata() {

		return dataModelsToInbuiltSchemata;
	}

	public static String determineDataModelSchemaBaseURI(final DataModel dataModel) {

		if (dataModel != null && dataModel.getSchema() != null) {

			final Schema schema = dataModel.getSchema();

			if(!Strings.isNullOrEmpty(schema.getBaseURI())) {

				// utilize assigned base URI

				return schema.getBaseURI();
			} else if(!Strings.isNullOrEmpty(schema.getUuid())) {

				// make use of schema identifier

				final String schemaUuid = dataModel.getSchema().getUuid();

				return BASE_URI + SCHEMAS_DIRECTIVE + SLASH + schemaUuid + HASH;
			} else {

				return generateLegacyDataModelSchemaBaseURI(dataModel);
			}
		} else {

			return generateLegacyDataModelSchemaBaseURI(dataModel);
		}
	}

	public static String determineDataModelBaseURI(final DataModel dataModel) {

		if (dataModel == null) {

			DataModelUtils.LOG
					.error("data model shouldn't be really null at data model base uri determination; however I'll create a uri with a random uuid instead ...");

			final StringBuilder sb = new StringBuilder();

			// create uri with random uuid

			sb.append(BASE_URI + DATAMODELS_DIRECTIVE + SLASH).append(UUID.randomUUID());

			return sb.toString();
		}

		// create data resource base uri
		URI uri = null;
		String dataResourceName = null;

		final org.dswarm.persistence.model.resource.Resource dataResource = dataModel.getDataResource();

		if (dataResource != null) {

			final JsonNode pathNode = dataResource.getAttribute(ResourceStatics.PATH);

			if (pathNode != null) {

				final String path = pathNode.asText();

				if (path != null) {

					if (path.contains(File.separator)) {

						// e.g. C:\DMP\datamanagement-platform\converter\target\test-classes\test_csv.csv
						// return /test_csv.csv
						dataResourceName = SLASH + path.substring(path.lastIndexOf(File.separator) + 1, path.length());

					} else {

						dataResourceName = path.substring(path.lastIndexOf(SLASH), path.length());
					}

					try {

						uri = URI.create(dataResourceName);
					} catch (final Exception e) {

						DataModelUtils.LOG.debug("couldn't create uri from data resource path", e);
					}
				} else {

					DataModelUtils.LOG.warn("The data model [{}] is missing the data resource path string", dataModel.getUuid());
				}
			} else {

				DataModelUtils.LOG.warn("The data model [{}] is missing the data resource path", dataModel.getUuid());
			}
		} else {

			DataModelUtils.LOG.warn("The data model [{}] is missing the data resource", dataModel.getUuid());
		}

		final String dataResourceBaseURI;

		if (uri != null && uri.getScheme() != null) {

			// data resource name could act as base uri

			dataResourceBaseURI = dataResourceName;
		} else {

			// create uri with help of given data resource id or data model id

			final StringBuilder sb = new StringBuilder();

			if (dataModel.getUuid() != null) {

				// create uri from data model id

				sb.append(BASE_URI + DATAMODELS_DIRECTIVE + SLASH).append(dataModel.getUuid());
			} else if (dataResource != null && dataResource.getUuid() != null) {

				// create uri from resource id

				sb.append(BASE_URI + RESOURCES_DIRECTIVE + SLASH).append(dataResource.getUuid());
			} else {

				// create uri with random uuid

				sb.append(BASE_URI + DATAMODELS_DIRECTIVE + SLASH).append(UUID.randomUUID());
			}

			dataResourceBaseURI = sb.toString();
		}

		return dataResourceBaseURI;
	}

	public static org.dswarm.graph.json.Resource mintRecordResource(final DataModel dataModel) {

		// mint completely new uri

		final StringBuilder sb = new StringBuilder();

		if (dataModel != null) {

			// create uri from resource id and configuration id and random uuid

			sb.append(BASE_URI + DATAMODELS_DIRECTIVE + SLASH).append(dataModel.getUuid()).append(SLASH + RECORDS_DIRECTIVE + SLASH);
		} else {

			// create uri from random uuid

			sb.append(BASE_URI + RECORDS_DIRECTIVE + SLASH);
		}

		final String recordURI = sb.append(UUID.randomUUID()).toString();

		return new org.dswarm.graph.json.Resource(recordURI);
	}

	private static String generateLegacyDataModelSchemaBaseURI(final DataModel dataModel) {
		// for legacy purpose (?)

		final String dataModelBaseURI = DataModelUtils.determineDataModelBaseURI(dataModel);

		if (dataModelBaseURI == null) {

			return null;
		}

		if (dataModelBaseURI.endsWith(SLASH)) {

			return dataModelBaseURI + SCHEMA_IDENTIFIER + HASH;
		}

		return dataModelBaseURI + SLASH + SCHEMA_IDENTIFIER + HASH;
	}
}