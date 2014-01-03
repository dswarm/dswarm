package de.avgl.dmp.controller.utils;

import java.io.File;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;

import de.avgl.dmp.controller.DMPControllerException;
import de.avgl.dmp.init.util.DMPUtil;

/**
 * A utility class for the controller module.
 * 
 * @author tgaengler
 */
public final class DMPControllerUtils {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(DMPControllerUtils.class);

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
}
