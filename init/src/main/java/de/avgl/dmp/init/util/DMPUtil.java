package de.avgl.dmp.init.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import de.avgl.dmp.init.DMPException;

public class DMPUtil {

	private static final JsonNodeFactory	factory	= JsonNodeFactory.instance;
	private static final ObjectMapper		mapper;

	static {
		mapper = new ObjectMapper();
		final JaxbAnnotationModule module = new JaxbAnnotationModule();
		mapper.registerModule(module);
	}

	public static String getResourceAsString(String resource) throws IOException {
		URL url = Resources.getResource(resource);
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

	public static ObjectMapper getJSONObjectMapper() {

		return mapper;
	}

	public static JsonNodeFactory getJSONFactory() {

		return factory;
	}

	/**
	 * Determines the temporary directory that should be utilised to store processed files temporarily for further utilisation.<br>
	 * Created by: tgaengler
	 * 
	 * @return the temporary directory
	 * @throws Exception
	 */
	public static String getTmpDir(final String postfix) throws Exception {

		final InputStream inStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("dmp.properties");
		final Properties properties = new Properties();
		properties.load(inStream);

		final String tmpDirRoot = properties.getProperty("tmp_path");

		final String javaIOTmpDir;

		if (tmpDirRoot == null) {
			javaIOTmpDir = System.getProperty("java.io.tmpdir");
		} else {

			if (postfix != null) {

				javaIOTmpDir = tmpDirRoot + "/" + postfix;
			} else {

				javaIOTmpDir = tmpDirRoot;
			}
		}

		if (javaIOTmpDir == null) {

			throw new Exception("java.io.tmpdir system property shouldn't be null");
		}

		return javaIOTmpDir;
	}

	/**
	 * Creates a new file with the given file name in the temporary directory.<br>
	 * Created by: tgaengler
	 * 
	 * @param fileName the file name
	 * @param directoryPostFix a postfix for the directory
	 * @return a new {@link File} instance with the given file name.
	 * @throws Exception
	 */
	public static File createLocalTmpFile(final String fileName, final String directoryPostFix) throws Exception {

		final String javaIOTmpDir = DMPUtil.getTmpDir(directoryPostFix);

		final File file = FileUtils.getFile(javaIOTmpDir, fileName);

		return file;
	}
}
