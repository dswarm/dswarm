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
package org.dswarm.converter.mf.stream.source;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import org.apache.commons.io.input.BOMInputStream;
import org.culturegraph.mf.exceptions.MetafactureException;
import org.culturegraph.mf.framework.DefaultObjectPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.stream.source.Opener;
import org.culturegraph.mf.util.ResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Opens a file and passes a reader for it to the receiver. It also checks and discards an UTF8 BOM.
 * 
 * @author phorn
 */
@Description("Opens a file and checks for UTF8 BOM.")
@In(String.class)
@Out(java.io.Reader.class)
public class BOMResourceOpener extends DefaultObjectPipe<String, ObjectReceiver<Reader>> implements Opener {

	private static final Logger	LOG			= LoggerFactory.getLogger(BOMResourceOpener.class);

	private String				encoding	= "UTF-8";

	/**
	 * Returns the encoding used to open the resource.
	 * 
	 * @return current default setting
	 */
	public String getEncoding() {
		return encoding;
	}

	/**
	 * Sets the encoding used to open the resource.
	 * 
	 * @param encoding new encoding
	 */
	public void setEncoding(final String encoding) {
		this.encoding = encoding;
	}

	private InputStream getFileInputStream(final String fileName) throws FileNotFoundException {
		return new BOMInputStream(ResourceUtil.getStream(fileName));
	}

	@Override
	public void process(final String file) throws MetafactureException {

		InputStream is = null;
		try {
			is = getFileInputStream(file);
			final Reader reader = new InputStreamReader(is, encoding);
			getReceiver().process(reader);

		} catch (final FileNotFoundException | UnsupportedEncodingException e) {
			throw new MetafactureException(e);
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (final IOException e) {
				BOMResourceOpener.LOG.error("IO error while closing file inputstream", e);
			}
		}
	}

}
