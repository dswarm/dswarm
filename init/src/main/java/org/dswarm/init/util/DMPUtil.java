package org.dswarm.init.util;

import java.io.File;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.apache.commons.io.FileUtils;

@Singleton
public final class DMPUtil {

	private final String tmpDir;
	private final String separator;

	@Inject
	public DMPUtil(@Named("dswarm.paths.tmp") final String tmpDir, @Named("file.separator") final String separator) {
		this.tmpDir = tmpDir;
		this.separator = separator;
	}

	/**
	 * Determines the temporary directory that should be utilised to store processed files temporarily for further utilisation.<br>
	 * Created by: tgaengler
	 * 
	 * @return the temporary directory
	 * @throws Exception
	 */
	private String getTmpDir(final String postfix) throws Exception {
		if (postfix == null) {
			return tmpDir;
		}

		return String.format("%s%s%s", tmpDir, separator, postfix);
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
	public File createLocalTmpFile(final String fileName, final String directoryPostFix) throws Exception {

		final String javaIOTmpDir = getTmpDir(directoryPostFix);

		return FileUtils.getFile(javaIOTmpDir, fileName);
	}
}
