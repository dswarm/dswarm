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
package org.dswarm.persistence.util;

import java.io.IOException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.List;
import java.util.Random;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import org.dswarm.init.DMPException;
import org.dswarm.persistence.model.job.utils.ComponentUtils;
import org.dswarm.persistence.model.job.utils.FilterUtils;
import org.dswarm.persistence.model.job.utils.FunctionUtils;
import org.dswarm.persistence.model.job.utils.JobUtils;
import org.dswarm.persistence.model.job.utils.MappingUtils;
import org.dswarm.persistence.model.job.utils.TransformationUtils;
import org.dswarm.persistence.model.resource.utils.ConfigurationUtils;
import org.dswarm.persistence.model.resource.utils.DataModelUtils;
import org.dswarm.persistence.model.resource.utils.ResourceUtils;
import org.dswarm.persistence.model.schema.utils.AttributePathUtils;
import org.dswarm.persistence.model.schema.utils.AttributeUtils;
import org.dswarm.persistence.model.schema.utils.ClaszUtils;
import org.dswarm.persistence.model.schema.utils.ContentSchemaUtils;
import org.dswarm.persistence.model.schema.utils.MappingAttributePathInstanceUtils;
import org.dswarm.persistence.model.schema.utils.SchemaAttributePathInstanceUtils;
import org.dswarm.persistence.model.schema.utils.SchemaUtils;

/**
 * A utility class for the persistence module.
 * 
 * @author tgaengler
 * @author phorn
 */
public final class DMPPersistenceUtil {

	/**
	 * The JSON node factory that can be utilised to create new JSON nodes (objects or arrays).
	 */
	private static final JsonNodeFactory					FACTORY;

	/**
	 * The object mapper that can be utilised to de-/serialise JSON nodes.
	 */
	private static final ObjectMapper						MAPPER;

	private static final AttributePathUtils					ATTRIBUTEPATHUTILS;

	private static final AttributeUtils						ATTRIBUTEUTILS;

	private static final ComponentUtils						COMPONENTUTILS;

	private static final MappingUtils						MAPPINGUTILS;

	private static final FunctionUtils						FUNCTIONUTILS;

	private static final ResourceUtils						RESOURCEUTILS;

	private static final ConfigurationUtils					CONFIGURATIONUTILS;

	private static final ClaszUtils							CLASZUTILS;

	private static final SchemaUtils						SCHEMAUTILS;

	private static final ContentSchemaUtils					CONTENTSCHEMAUTILS;

	private static final TransformationUtils				TRANSFORMATIONUTILS;

	private static final FilterUtils						FILTERUTILS;

	private static final DataModelUtils						DATAMODELUTILS;

	private static final JobUtils							JOBUTILS;

	private static final MappingAttributePathInstanceUtils	MAPPINGATTRIBUTEPATHINSTANCEUTILS;
	
	private static final SchemaAttributePathInstanceUtils	SCHEMAATTRIBUTEPATHINSTANCEUTILS;

	private static final long								LOWER_RANGE	= -9223372036854775808L;	// assign
	// lower
	// range
	// value
	private static final long								UPPER_RANGE	= -1;						// assign
	// upper
	// range
	// value
	private static final Random								random		= new SecureRandom();

	public static final String RECORD_ID = "__record_id";
	public static final String RECORD_DATA = "__record_data";

	static {
		MAPPER = new ObjectMapper();
		final JaxbAnnotationModule module = new JaxbAnnotationModule();
		DMPPersistenceUtil.MAPPER.registerModule(module).setSerializationInclusion(Include.NON_NULL)
				.setSerializationInclusion(Include.NON_EMPTY);

		FACTORY = DMPPersistenceUtil.MAPPER.getNodeFactory();

		ATTRIBUTEPATHUTILS = new AttributePathUtils();
		ATTRIBUTEUTILS = new AttributeUtils();
		COMPONENTUTILS = new ComponentUtils();
		MAPPINGUTILS = new MappingUtils();
		FUNCTIONUTILS = new FunctionUtils();
		RESOURCEUTILS = new ResourceUtils();
		CONFIGURATIONUTILS = new ConfigurationUtils();
		CLASZUTILS = new ClaszUtils();
		SCHEMAUTILS = new SchemaUtils();
		CONTENTSCHEMAUTILS = new ContentSchemaUtils();
		TRANSFORMATIONUTILS = new TransformationUtils();
		FILTERUTILS = new FilterUtils();
		DATAMODELUTILS = new DataModelUtils();
		JOBUTILS = new JobUtils();
		MAPPINGATTRIBUTEPATHINSTANCEUTILS = new MappingAttributePathInstanceUtils();
		SCHEMAATTRIBUTEPATHINSTANCEUTILS = new SchemaAttributePathInstanceUtils();
	}

	/**
	 * Retrieves a resource by the give path and converts its content to a string.
	 * 
	 * @param resource a resource path
	 * @return a string representation fo the content of the resource
	 * @throws IOException
	 */
	public static String getResourceAsString(final String resource) throws IOException {
		final URL url = Resources.getResource(resource);
		return Resources.toString(url, Charsets.UTF_8);
	}

	/**
	 * Retrieves a resource by the give path and converts its lines to strings.
	 * 
	 * @param resource a resource path
	 * @return a line-wise string representation fo the content of the resource
	 * @throws IOException
	 */
	public static List<String> getResourceLinesAsString(final String resource) throws IOException {
		final URL url = Resources.getResource(resource);
		return Resources.readLines(url, Charsets.UTF_8);
	}

	/**
	 * Gets a JSON object from the given string.
	 * 
	 * @param jsonString the string that holds a serialised JSON object.
	 * @return the deserialised JSON object
	 * @throws DMPException
	 */
	public static ObjectNode getJSON(final String jsonString) throws DMPException {

		try {
			return DMPPersistenceUtil.MAPPER.readValue(jsonString, ObjectNode.class);
		} catch (final IOException e) {

			throw new DMPException("something went wrong while parsing the JSON string '" + jsonString + "'\n" + e.getMessage());
		}
	}

	/**
	 * Gets a JSON array from the given string.
	 * 
	 * @param jsonString the string that holds a serialised JSON array.
	 * @return the deserialised JSON array
	 * @throws DMPException
	 */
	public static ArrayNode getJSONArray(final String jsonString) throws DMPException {

		try {
			return DMPPersistenceUtil.MAPPER.readValue(jsonString, ArrayNode.class);
		} catch (final IOException e) {

			throw new DMPException("something went wrong while parsing the JSON string '" + jsonString + "'\n" + e.getMessage());
		}
	}

	/**
	 * Gets the object mapper that can be utilised to de-/serialise JSON nodes.
	 * 
	 * @return the object mapper that can be utilised to de-/serialise JSON nodes
	 */
	public static ObjectMapper getJSONObjectMapper() {

		return DMPPersistenceUtil.MAPPER;
	}

	/**
	 * Gets the JSON node factory that can be utilised to create new JSON nodes (objects or arrays)
	 * 
	 * @return JSON node factory that can be utilised to create new JSON nodes (objects or arrays)
	 */
	public static JsonNodeFactory getJSONFactory() {

		return DMPPersistenceUtil.FACTORY;
	}

	public static AttributePathUtils getAttributePathUtils() {

		return DMPPersistenceUtil.ATTRIBUTEPATHUTILS;
	}

	public static AttributeUtils getAttributeUtils() {

		return DMPPersistenceUtil.ATTRIBUTEUTILS;
	}

	public static ComponentUtils getComponentUtils() {

		return DMPPersistenceUtil.COMPONENTUTILS;
	}

	public static MappingUtils getMappingUtils() {

		return DMPPersistenceUtil.MAPPINGUTILS;
	}

	public static FunctionUtils getFunctionUtils() {

		return DMPPersistenceUtil.FUNCTIONUTILS;
	}

	public static ResourceUtils getResourceUtils() {

		return DMPPersistenceUtil.RESOURCEUTILS;
	}

	public static ConfigurationUtils getConfigurationUtils() {

		return DMPPersistenceUtil.CONFIGURATIONUTILS;
	}

	public static ClaszUtils getClaszUtils() {

		return DMPPersistenceUtil.CLASZUTILS;
	}

	public static SchemaUtils getSchemaUtils() {

		return DMPPersistenceUtil.SCHEMAUTILS;
	}

	public static ContentSchemaUtils getContentSchemaUtils() {

		return DMPPersistenceUtil.CONTENTSCHEMAUTILS;
	}

	public static TransformationUtils getTransformationUtils() {

		return DMPPersistenceUtil.TRANSFORMATIONUTILS;
	}

	public static FilterUtils getFilterUtils() {

		return DMPPersistenceUtil.FILTERUTILS;
	}

	public static DataModelUtils getDataModelUtils() {

		return DMPPersistenceUtil.DATAMODELUTILS;
	}

	public static JobUtils getJobUtils() {

		return DMPPersistenceUtil.JOBUTILS;
	}

	public static MappingAttributePathInstanceUtils getMappingAttributePathInstanceUtils() {

		return DMPPersistenceUtil.MAPPINGATTRIBUTEPATHINSTANCEUTILS;
	}
	
	public static SchemaAttributePathInstanceUtils getSchemaAttributePathInstanceUtils() {

		return DMPPersistenceUtil.SCHEMAATTRIBUTEPATHINSTANCEUTILS;
	}

	public static long generateRandomDummyId() {

		return DMPPersistenceUtil.LOWER_RANGE
				+ (long) (DMPPersistenceUtil.random.nextDouble() * (DMPPersistenceUtil.UPPER_RANGE - DMPPersistenceUtil.LOWER_RANGE));
	}

}
