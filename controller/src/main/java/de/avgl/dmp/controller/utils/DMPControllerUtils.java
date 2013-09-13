package de.avgl.dmp.controller.utils;

import java.io.File;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;

import de.avgl.dmp.controller.DMPControllerException;
import de.avgl.dmp.init.util.DMPUtil;

public class DMPControllerUtils {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(DMPControllerUtils.class);

	public static File writeToFile(final InputStream source, final String fileName, final String directoryPostFix) throws DMPControllerException {

		try {

			final File file = DMPUtil.createLocalTmpFile(fileName, directoryPostFix);

			FileUtils.copyInputStreamToFile(source, file);

			return file;
		} catch (Exception e) {

			LOG.debug("couldn't write input stream to file '" + fileName + "'");

			throw new DMPControllerException("couldn't write input stream to file '" + fileName + "'\n" + e.getMessage());
		}
	}
}
