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
package org.dswarm.controller.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import com.google.inject.Inject;
import com.google.inject.Singleton;
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

	private static final Logger LOG = LoggerFactory.getLogger(DMPControllerUtils.class);
	private static final int BUFFER_SIZE = 1024;

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

		if (source == null) {

			final String message = "couldn't write input stream to file, because the input stream is not available";

			DMPControllerUtils.LOG.error(message);

			throw new DMPControllerException(message);
		}

		try {

			final File file = dmpUtil.createLocalTmpFile(fileName, directoryPostFix);

			final BufferedInputStream bis = new BufferedInputStream(source, BUFFER_SIZE);

			Files.copy(bis, file.toPath(), StandardCopyOption.REPLACE_EXISTING);

			bis.close();
			source.close();

			return file;
		} catch (final Exception e) {

			DMPControllerUtils.LOG.error("couldn't write input stream to file '{}'", fileName, e);

			throw new DMPControllerException(String.format("couldn't write input stream to file '%s'\n%s", fileName, e.getMessage()));
		}
	}
}
