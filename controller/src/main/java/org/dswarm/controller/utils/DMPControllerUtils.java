package org.dswarm.controller.utils;

import java.io.File;
import java.io.InputStream;

import com.google.inject.Inject;
import com.google.inject.Singleton;
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
@Singleton
public final class DMPControllerUtils {

	private static final Logger	LOG	= LoggerFactory.getLogger(DMPControllerUtils.class);

	private final DMPUtil dmpUtil;

	@Inject
	public DMPControllerUtils(final DMPUtil dmpUtil) {
		this.dmpUtil = dmpUtil;
	}

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
	public File writeToFile(final InputStream source, final String fileName, final String directoryPostFix) throws DMPControllerException {

		try {

			final File file = dmpUtil.createLocalTmpFile(fileName, directoryPostFix);

			FileUtils.copyInputStreamToFile(source, file);

			return file;
		} catch (final Exception e) {

			DMPControllerUtils.LOG.debug("couldn't write input stream to file '{}'", fileName);

			throw new DMPControllerException("couldn't write input stream to file '" + fileName + "'\n" + e.getMessage());
		}
	}
}
