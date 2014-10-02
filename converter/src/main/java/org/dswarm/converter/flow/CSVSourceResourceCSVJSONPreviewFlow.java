/**
 * Copyright (C) 2013, 2014 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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

import com.google.common.base.Optional;
import org.culturegraph.mf.framework.ObjectPipe;
import org.culturegraph.mf.framework.ObjectReceiver;

import org.dswarm.converter.DMPConverterException;
import org.dswarm.converter.mf.stream.reader.CsvReader;
import org.dswarm.converter.mf.stream.source.CSVJSONEncoder;
import org.dswarm.converter.mf.stream.source.CSVJSONWriter;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.DataModel;

/**
 * Flow that reads and parses a given CSV document and returns a preview of its content as JSON representation.
 * 
 * @author tgaengler
 * @author phorn
 */
public class CSVSourceResourceCSVJSONPreviewFlow extends AbstractCSVResourceFlow<String> {

	public CSVSourceResourceCSVJSONPreviewFlow(final String encoding, final Character escapeCharacter, final Character quoteCharacter,
			final Character columnDelimiter, final String rowDelimiter) {
		super(encoding, escapeCharacter, quoteCharacter, columnDelimiter, rowDelimiter);
	}

	public CSVSourceResourceCSVJSONPreviewFlow(final Configuration configuration) throws DMPConverterException {
		super(configuration);
	}

	public CSVSourceResourceCSVJSONPreviewFlow(final DataModel dataModel) throws DMPConverterException {

		super(dataModel);
	}

	@Override
	protected String process(final ObjectPipe<String, ObjectReceiver<Reader>> opener, final String obj, final CsvReader pipe) {

		final CSVJSONEncoder encoder = new CSVJSONEncoder();
		encoder.withHeader();

		final CSVJSONWriter writer = new CSVJSONWriter();

		pipe.setReceiver(encoder).setReceiver(writer);

		opener.process(obj);

		return writer.toString();
	}

	public CSVSourceResourceCSVJSONPreviewFlow withLimit(final int limit) {

		atMost = Optional.of(limit);

		return this;
	}
}
