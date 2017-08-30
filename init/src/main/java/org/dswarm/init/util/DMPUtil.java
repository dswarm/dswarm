/**
 * Copyright (C) 2013 – 2017 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

		checkDirExistenceOrCreateMissingParts(tmpDir);
	}

	private static void checkDirExistenceOrCreateMissingParts(final String dirPath) {

		final File dirFile = new File(dirPath);

		if (!dirFile.exists()) {

			dirFile.mkdirs();
		}
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

		final String tmpDirPath = String.format("%s%s%s", tmpDir, separator, postfix);

		checkDirExistenceOrCreateMissingParts(tmpDirPath);

		return tmpDirPath;
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
