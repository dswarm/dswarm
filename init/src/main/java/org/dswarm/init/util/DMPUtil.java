package org.dswarm.init.util;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

public final class DMPUtil {

	/**
	 * Determines the temporary directory that should be utilised to store processed files temporarily for further utilisation.<br>
	 * Created by: tgaengler
	 *
	 * @return the temporary directory
	 * @throws Exception
	 */
	private static String getTmpDir(final String postfix) throws Exception {

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

		return FileUtils.getFile(javaIOTmpDir, fileName);
	}
}
