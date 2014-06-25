package org.dswarm.converter.flow.test.csv;

import java.io.Reader;

import org.culturegraph.mf.framework.ObjectPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.junit.Test;

import com.fasterxml.jackson.databind.node.TextNode;

import org.dswarm.converter.DMPConverterException;
import org.dswarm.converter.flow.AbstractCSVResourceFlow;
import org.dswarm.converter.flow.CSVResourceFlowFactory;
import org.dswarm.converter.mf.stream.reader.CsvReader;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.utils.ConfigurationStatics;

public class CSVResourceFlowFactoryTest {

	private class TestFlow extends AbstractCSVResourceFlow<String> {

		@Override
		protected String process(final ObjectPipe<String, ObjectReceiver<Reader>> opener, final String obj, final CsvReader pipe) {
			return "";
		}
	}

	@Test(expected = DMPConverterException.class)
	public void testNoConstructor1() throws Exception {
		final Configuration configuration = new Configuration();

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
