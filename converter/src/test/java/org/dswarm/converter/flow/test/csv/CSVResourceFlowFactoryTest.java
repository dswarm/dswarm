/**
 * Copyright (C) 2013 – 2015 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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
package org.dswarm.converter.flow.test.csv;

import java.io.Reader;

import com.fasterxml.jackson.databind.node.TextNode;
import org.culturegraph.mf.framework.ObjectPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.junit.Test;

import org.dswarm.converter.DMPConverterException;
import org.dswarm.converter.flow.AbstractCSVResourceFlow;
import org.dswarm.converter.flow.CSVResourceFlowFactory;
import org.dswarm.converter.mf.stream.reader.CsvReader;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.utils.ConfigurationStatics;
import org.dswarm.persistence.service.UUIDService;

public class CSVResourceFlowFactoryTest {

	private class TestFlow extends AbstractCSVResourceFlow<String> {

		@Override
		protected String process(final ObjectPipe<String, ObjectReceiver<Reader>> opener, final String obj, final CsvReader pipe) {
			return "";
		}
	}

	@Test(expected = DMPConverterException.class)
	public void testNoConstructor1() throws Exception {

		final String uuid = UUIDService.getUUID(Configuration.class.getSimpleName());

		final Configuration configuration = new Configuration(uuid);

		configuration.addParameter(ConfigurationStatics.COLUMN_DELIMITER, new TextNode(";"));
		configuration.addParameter(ConfigurationStatics.ENCODING, new TextNode("UTF-8"));
		configuration.addParameter(ConfigurationStatics.ESCAPE_CHARACTER, new TextNode("\\"));
		configuration.addParameter(ConfigurationStatics.QUOTE_CHARACTER, new TextNode("\""));
		configuration.addParameter(ConfigurationStatics.ROW_DELIMITER, new TextNode("\n"));

		CSVResourceFlowFactory.fromConfiguration(configuration, TestFlow.class);
	}

	@Test(expected = DMPConverterException.class)
	public void testNoConstructor2() throws Exception {
		CSVResourceFlowFactory.fromConfigurationParameters("UTF-8", '\\', '"', ';', "\n", TestFlow.class);

	}
}
