package org.dswarm.controller.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import com.google.common.io.Resources;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.controller.DMPControllerException;
import org.dswarm.init.util.DMPUtil;

/**
 * A utility class for the controller module.
 * 
 * @author tgaengler
 */
public final class DMPControllerUtils {

	private static final Logger	LOG	= LoggerFactory.getLogger(DMPControllerUtils.class);

	/**
	 * Write a given input source stream into the file with the given file name at the given directory postfix (relative file
	 * path).
	 * 
	 * @param source the input source stream
	 * @param fileName the file name
	 * @param directoryPostFix the directory postfix (relative file path)
	 * @return the created file
	 * @throws DMPControllerException
	 */
	public static File writeToFile(final InputStream source, final String fileName, final String directoryPostFix) throws DMPControllerException {

		try {

			final File file = DMPUtil.createLocalTmpFile(fileName, directoryPostFix);

			FileUtils.copyInputStreamToFile(source, file);

			return file;
		} catch (final Exception e) {

			DMPControllerUtils.LOG.debug("couldn't write input stream to file '" + fileName + "'");

			throw new DMPControllerException("couldn't write input stream to file '" + fileName + "'\n" + e.getMessage());
		}
	}

	/**
	 * load a properties file from a specific location
	 * 
	 * @param fileName the location of the properties file, relative to the classpath
	 * @return properties, empty if the file could not be found
	 */
	public static Properties loadProperties(final String fileName) {

		final Properties properties = new Properties();

		final URL resource = Resources.getResource(fileName);
		try {
			properties.load(resource.openStream());
		} catch (final IOException e) {
			DMPControllerUtils.LOG.error("Could not load dmp.properties", e);
		}

		return properties;
	}

	/**
	 * load the default dmp.properties file
	 * 
	 * @return properties, empty if the file could not be found
	 */
	public static Properties loadProperties() {
		return DMPControllerUtils.loadProperties("dmp.properties");
	}
}
