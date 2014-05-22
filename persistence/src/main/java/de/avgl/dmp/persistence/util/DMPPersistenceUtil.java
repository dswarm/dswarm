package de.avgl.dmp.persistence.util;

import java.io.IOException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.Random;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import de.avgl.dmp.init.DMPException;
import de.avgl.dmp.persistence.model.job.utils.ComponentUtils;
import de.avgl.dmp.persistence.model.job.utils.FilterUtils;
import de.avgl.dmp.persistence.model.job.utils.FunctionUtils;
import de.avgl.dmp.persistence.model.job.utils.JobUtils;
import de.avgl.dmp.persistence.model.job.utils.MappingUtils;
import de.avgl.dmp.persistence.model.job.utils.TransformationUtils;
import de.avgl.dmp.persistence.model.resource.utils.ConfigurationUtils;
import de.avgl.dmp.persistence.model.resource.utils.DataModelUtils;
import de.avgl.dmp.persistence.model.resource.utils.ResourceUtils;
import de.avgl.dmp.persistence.model.schema.utils.AttributePathUtils;
import de.avgl.dmp.persistence.model.schema.utils.AttributeUtils;
import de.avgl.dmp.persistence.model.schema.utils.ClaszUtils;
import de.avgl.dmp.persistence.model.schema.utils.MappingAttributePathInstanceUtils;
import de.avgl.dmp.persistence.model.schema.utils.SchemaUtils;

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

	private static final TransformationUtils				TRANSFORMATIONUTILS;

	private static final FilterUtils						FILTERUTILS;

	private static final DataModelUtils						DATAMODELUTILS;

	private static final JobUtils							JOBUTILS;

	private static final MappingAttributePathInstanceUtils	MAPPINGATTRIBUTEPATHINSTANCEUTILS;

	private static final long								LOWER_RANGE	= -9223372036854775808L;	// assign
																									// lower
																									// range
																									// value
	private static final long								UPPER_RANGE	= -1;						// assign
																									// upper
																									// range
																									// value
	private static final Random								random		= new SecureRandom();

	static {
		MAPPER = new ObjectMapper();
		final JaxbAnnotationModule module = new JaxbAnnotationModule();
		DMPPersistenceUtil.MAPPER.registerModule(module).registerModule(new Hibernate4Module()).setSerializationInclusion(Include.NON_NULL)
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
		TRANSFORMATIONUTILS = new TransformationUtils();
		FILTERUTILS = new FilterUtils();
		DATAMODELUTILS = new DataModelUtils();
		JOBUTILS = new JobUtils();
		MAPPINGATTRIBUTEPATHINSTANCEUTILS = new MappingAttributePathInstanceUtils();
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

	public static long generateRandomDummyId() {

		return DMPPersistenceUtil.LOWER_RANGE
				+ (long) (DMPPersistenceUtil.random.nextDouble() * (DMPPersistenceUtil.UPPER_RANGE - DMPPersistenceUtil.LOWER_RANGE));
	}

}
