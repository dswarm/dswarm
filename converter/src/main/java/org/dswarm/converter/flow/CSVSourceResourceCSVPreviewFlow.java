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
package org.dswarm.converter.flow;

import java.io.Reader;
import java.io.StringWriter;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import org.culturegraph.mf.framework.ObjectPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.stream.sink.ObjectJavaIoWriter;

import org.dswarm.converter.DMPConverterException;
import org.dswarm.converter.mf.stream.reader.CsvReader;
import org.dswarm.converter.mf.stream.source.CSVEncoder;
import org.dswarm.persistence.model.resource.Configuration;

/**
 * Flow that reads and parses a given CSV document and returns a preview of its content.
 *
 * @author tgaengler
 * @author phorn
 */
public class CSVSourceResourceCSVPreviewFlow extends AbstractCSVResourceFlow<String> {

	@AssistedInject
	private CSVSourceResourceCSVPreviewFlow(
			@Assisted("encoding") final String encoding,
			@Assisted("escapeCharacter") final Character escapeCharacter,
			@Assisted("quoteCharacter") final Character quoteCharacter,
			@Assisted("columnDelimiter") final Character columnDelimiter,
			@Assisted("rowDelimiter") final String rowDelimiter) {
		super(encoding, escapeCharacter, quoteCharacter, columnDelimiter, rowDelimiter);
	}

	@AssistedInject
	private CSVSourceResourceCSVPreviewFlow(
			@Assisted final Configuration configuration) throws DMPConverterException {
		super(configuration);
	}

	@Override
	protected String process(final ObjectPipe<String, ObjectReceiver<Reader>> opener, final String obj, final CsvReader pipe) {
		final CSVEncoder converter = new CSVEncoder();
		converter.withHeader();

		final StringWriter stringWriter = new StringWriter();
		final ObjectJavaIoWriter<String> writer = new ObjectJavaIoWriter<>(stringWriter);

		pipe.setReceiver(converter).setReceiver(writer);

		opener.process(obj);

		return stringWriter.toString();
	}
}
