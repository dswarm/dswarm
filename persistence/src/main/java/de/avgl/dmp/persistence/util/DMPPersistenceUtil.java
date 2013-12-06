package de.avgl.dmp.persistence.util;

import java.io.IOException;
import java.net.URL;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.inject.Injector;

import de.avgl.dmp.init.DMPException;


public class DMPPersistenceUtil {

	private static final JsonNodeFactory	factory	= JsonNodeFactory.instance;
	private static final ObjectMapper		mapper;

	public static transient Injector		injector;

	static {
		mapper = new ObjectMapper();
		final JaxbAnnotationModule module = new JaxbAnnotationModule();
		mapper.registerModule(module)
			.setSerializationInclusion(Include.NON_NULL)
			.setSerializationInclusion(Include.NON_EMPTY);
	}

	public static String getResourceAsString(final String resource) throws IOException {
		final URL url = Resources.getResource(resource);
		return Resources.toString(url, Charsets.UTF_8);
	}

	public static ObjectNode getJSON(final String jsonString) throws DMPException {

		try {
			return mapper.readValue(jsonString, ObjectNode.class);
		} catch (JsonParseException e) {

			throw new DMPException("something went wrong while parsing the JSON string '" + jsonString + "'\n" + e.getMessage());
		} catch (JsonMappingException e) {

			throw new DMPException("something went wrong while parsing the JSON string '" + jsonString + "'\n" + e.getMessage());
		} catch (IOException e) {

			throw new DMPException("something went wrong while parsing the JSON string '" + jsonString + "'\n" + e.getMessage());
		}
	}
	
	public static ArrayNode getJSONArray(final String jsonString) throws DMPException {

		try {
			return mapper.readValue(jsonString, ArrayNode.class);
		} catch (JsonParseException e) {

			throw new DMPException("something went wrong while parsing the JSON string '" + jsonString + "'\n" + e.getMessage());
		} catch (JsonMappingException e) {

			throw new DMPException("something went wrong while parsing the JSON string '" + jsonString + "'\n" + e.getMessage());
		} catch (IOException e) {

			throw new DMPException("something went wrong while parsing the JSON string '" + jsonString + "'\n" + e.getMessage());
		}
	}

	public static ObjectMapper getJSONObjectMapper() {

		return mapper;
	}

	public static JsonNodeFactory getJSONFactory() {

		return factory;
	}

	public static Injector getInjector() throws DMPException {
		if (injector == null) {
			throw new DMPException("you should not use getInjector without providing ist first. Try to use @Inject first.");
		}

		return injector;
	}

}
