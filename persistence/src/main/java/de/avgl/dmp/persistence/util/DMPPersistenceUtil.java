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
import com.google.inject.Injector;

import de.avgl.dmp.init.DMPException;

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
	private static final JsonNodeFactory	FACTORY;

	/**
	 * The object mapper that can be utilised to de-/serialise JSON nodes.
	 */
	private static final ObjectMapper		MAPPER;

	/**
	 * The injector for dependency injection.
	 */
	@SuppressWarnings("StaticNonFinalField")
	public static transient Injector		injector;

	private static final long				LOWER_RANGE	= Long.valueOf("-9223372036854775808").longValue(); // assign lower range
																											// value
	private static final long				UPPER_RANGE	= -1;												// assign upper range
																											// value
	private static final Random				random		= new SecureRandom();

	static {
		MAPPER = new ObjectMapper();
		final JaxbAnnotationModule module = new JaxbAnnotationModule();
		MAPPER.registerModule(module).registerModule(new Hibernate4Module()).setSerializationInclusion(Include.NON_NULL)
				.setSerializationInclusion(Include.NON_EMPTY);

		FACTORY = MAPPER.getNodeFactory();
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
			return MAPPER.readValue(jsonString, ObjectNode.class);
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
			return MAPPER.readValue(jsonString, ArrayNode.class);
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

		return MAPPER;
	}

	/**
	 * Gets the JSON node factory that can be utilised to create new JSON nodes (objects or arrays)
	 * 
	 * @return JSON node factory that can be utilised to create new JSON nodes (objects or arrays)
	 */
	public static JsonNodeFactory getJSONFactory() {

		return FACTORY;
	}

	/**
	 * Gets the injector for dependency injection.
	 * 
	 * @return the injector for dependency injection
	 * @throws DMPException
	 */
	public static Injector getInjector() throws DMPException {
		if (injector == null) {
			throw new DMPException("you should not use getInjector without providing it first. Try to use @Inject first.");
		}

		return injector;
	}

	public static long generateRandomDummyId() {

		long randomValue = LOWER_RANGE + (long) (random.nextDouble() * (UPPER_RANGE - LOWER_RANGE));
		
		return randomValue;
	}

}
