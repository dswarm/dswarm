package de.avgl.dmp.converter.flow.test;

import org.junit.Test;

import com.fasterxml.jackson.databind.node.TextNode;
import com.hp.hpl.jena.rdf.model.Model;

import de.avgl.dmp.converter.DMPConverterException;
import de.avgl.dmp.converter.flow.XMLSourceResourceTriplesFlow;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.utils.ConfigurationStatics;

public class XMLSourceResourceTriplesFlowTest {

	private void testFlow(final XMLSourceResourceTriplesFlow flow, final String fileName) throws DMPConverterException {

		final Model triples = flow.applyResource(fileName);

		if(triples != null) {
			
			triples.write(System.out, "N3");
		}
	}

	// @Test
	// public void testEndToEnd() throws Exception {
	//
	// final CSVSourceResourceTriplesFlow flow =
	// CSVResourceFlowFactory.fromConfigurationParameters("UTF-8", '\\', '"', ';', "\n",
	// CSVSourceResourceTriplesFlow.class);
	//
	// testFlow(flow);
	// }

	@Test
	public void testFromConfiguration() throws Exception {
		final Configuration configuration = new Configuration();

		configuration.addParameter(ConfigurationStatics.RECORD_TAG, new TextNode("datensatz"));
		configuration.addParameter(ConfigurationStatics.XML_NAMESPACE, new TextNode("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd"));

		final XMLSourceResourceTriplesFlow flow = new XMLSourceResourceTriplesFlow(configuration);

		testFlow(flow, "test-mabxml.xml");
	}
	
	@Test
	public void testFromConfiguration2() throws Exception {
		final Configuration configuration = new Configuration();

		configuration.addParameter(ConfigurationStatics.RECORD_TAG, new TextNode("datensatz"));
		configuration.addParameter(ConfigurationStatics.XML_NAMESPACE, new TextNode("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd"));

		final XMLSourceResourceTriplesFlow flow = new XMLSourceResourceTriplesFlow(configuration);

		testFlow(flow, "test-complex-xml.xml");
	}

	// @Test(expected = DMPConverterException.class)
	// public void testNullConfiguration() throws Exception {
	// final Configuration configuration = null;
	// @SuppressWarnings("UnusedDeclaration") final CSVSourceResourceTriplesFlow flow = new
	// CSVSourceResourceTriplesFlow(configuration);
	// }
	//
	// @Test(expected = NullPointerException.class)
	// public void testNullConfigurationParameter() throws Exception {
	// final Configuration configuration = new Configuration();
	// @SuppressWarnings("UnusedDeclaration") final CSVSourceResourceTriplesFlow flow = new
	// CSVSourceResourceTriplesFlow(configuration);
	// }

}
