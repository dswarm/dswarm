package de.avgl.dmp.converter.flow.test.xml;

import java.util.List;

import org.junit.Test;

import com.fasterxml.jackson.databind.node.TextNode;

import de.avgl.dmp.converter.DMPConverterException;
import de.avgl.dmp.converter.GuicedTest;
import de.avgl.dmp.converter.flow.XMLSourceResourceTriplesFlow;
import de.avgl.dmp.persistence.model.internal.rdf.RDFModel;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.DataModel;
import de.avgl.dmp.persistence.model.resource.Resource;
import de.avgl.dmp.persistence.model.resource.utils.ConfigurationStatics;
import de.avgl.dmp.persistence.service.resource.DataModelService;


public class XMLSourceResourceTriplesFlowTest extends GuicedTest {

	private void testFlow(final XMLSourceResourceTriplesFlow flow, final String fileName) throws DMPConverterException {

		final List<RDFModel> rdfModels = flow.applyResource(fileName);

		if (rdfModels != null && !rdfModels.isEmpty()) {

			for (final RDFModel rdfModel : rdfModels) {

				rdfModel.getModel().write(System.out, "N3");
			}
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

		final DataModel dataModel = new DataModel();

		dataModel.setConfiguration(configuration);

		final XMLSourceResourceTriplesFlow flow = new XMLSourceResourceTriplesFlow(dataModel);

		testFlow(flow, "test-mabxml.xml");
	}

	@Test
	public void testFromConfiguration2() throws Exception {

		final Configuration configuration = new Configuration();

		configuration.addParameter(ConfigurationStatics.RECORD_TAG, new TextNode("datensatz"));
		configuration.addParameter(ConfigurationStatics.XML_NAMESPACE, new TextNode("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd"));

		final DataModel dataModel = new DataModel();

		dataModel.setConfiguration(configuration);

		final XMLSourceResourceTriplesFlow flow = new XMLSourceResourceTriplesFlow(dataModel);

		testFlow(flow, "test-complex-xml.xml");
	}

	@Test
	public void testFromConfiguration3() throws Exception {

		final DataModelService dataModelService = injector.getInstance(DataModelService.class);
		final DataModel dataModel = dataModelService.createObjectTransactional().getObject();

		dataModel.setConfiguration(new Configuration() {{
			addParameter(ConfigurationStatics.RECORD_TAG, new TextNode("record"));
		}});
		dataModel.setDataResource(new Resource() {{
			addAttribute("path", "/tmp/file.record");
		}});

		final XMLSourceResourceTriplesFlow flow = new XMLSourceResourceTriplesFlow(dataModel);

		testFlow(flow, "test-pnx.xml");

		dataModelService.deleteObject(dataModel.getId());
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
