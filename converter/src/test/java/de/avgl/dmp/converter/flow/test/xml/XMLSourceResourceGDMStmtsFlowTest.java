package de.avgl.dmp.converter.flow.test.xml;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.Charsets;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.io.Resources;

import de.avgl.dmp.converter.DMPConverterException;
import de.avgl.dmp.converter.GuicedTest;
import de.avgl.dmp.converter.flow.XMLSourceResourceGDMStmtsFlow;
import de.avgl.dmp.graph.json.Model;
import de.avgl.dmp.persistence.model.internal.gdm.GDMModel;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.DataModel;
import de.avgl.dmp.persistence.model.resource.Resource;
import de.avgl.dmp.persistence.model.resource.utils.ConfigurationStatics;
import de.avgl.dmp.persistence.service.resource.DataModelService;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

public class XMLSourceResourceGDMStmtsFlowTest extends GuicedTest {

	private void testFlow(final XMLSourceResourceGDMStmtsFlow flow, final String fileName, final String expectedResultFileName)
			throws DMPConverterException {

		final List<GDMModel> gdmModels = flow.applyResource(fileName);

		if (gdmModels != null && !gdmModels.isEmpty()) {

			for (final GDMModel gdmModel : gdmModels) {

				final Model model = gdmModel.getModel();

				Assert.assertNotNull("the GDM model shouldn't be null", model);

				// serialise model as JSON (pretty print)
				final ObjectMapper mapper = DMPPersistenceUtil.getJSONObjectMapper().configure(SerializationFeature.INDENT_OUTPUT, true);

				String modelJSON = null;

				try {

					modelJSON = mapper.writeValueAsString(model);
				} catch (final JsonProcessingException e) {

					Assert.assertTrue("something went wrong while serialising the GDM model to JSON", false);
				}

				Assert.assertNotNull("the GDM model JSON shouldn't be null", modelJSON);

				final URL expectedResultFileURL = Resources.getResource(expectedResultFileName);

				Assert.assertNotNull("the expected result file URL shouldn't be null", expectedResultFileURL);

				String expectedResult = null;

				try {

					expectedResult = Resources.toString(expectedResultFileURL, Charsets.UTF_8);
				} catch (final IOException e) {

					Assert.assertTrue("something went wrong while reading expected result from file", false);
				}

				Assert.assertNotNull("the expected result shouldn't be null", expectedResult);

				Assert.assertEquals("the processing result length is not equal to the expected one", expectedResult.length(), modelJSON.length());

				// System.out.println(modelJSON);
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

		final XMLSourceResourceGDMStmtsFlow flow = new XMLSourceResourceGDMStmtsFlow(dataModel);

		testFlow(flow, "test-mabxml.xml", "test-mabxml.gson");
	}

	@Test
	public void testFromConfiguration2() throws Exception {

		final Configuration configuration = new Configuration();

		configuration.addParameter(ConfigurationStatics.RECORD_TAG, new TextNode("datensatz"));
		configuration.addParameter(ConfigurationStatics.XML_NAMESPACE, new TextNode("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd"));

		final DataModel dataModel = new DataModel();

		dataModel.setConfiguration(configuration);

		final XMLSourceResourceGDMStmtsFlow flow = new XMLSourceResourceGDMStmtsFlow(dataModel);

		testFlow(flow, "test-complex-xml.xml", "test-complex-xml.gson");
	}

	@Test
	public void testFromConfiguration3() throws Exception {

		final DataModelService dataModelService = injector.getInstance(DataModelService.class);
		final DataModel dataModel = dataModelService.createObjectTransactional().getObject();

		dataModel.setConfiguration(new Configuration() {

			{
				addParameter(ConfigurationStatics.RECORD_TAG, new TextNode("record"));
			}
		});
		dataModel.setDataResource(new Resource() {

			{
				addAttribute("path", "/tmp/file.record");
			}
		});

		final XMLSourceResourceGDMStmtsFlow flow = new XMLSourceResourceGDMStmtsFlow(dataModel);

		testFlow(flow, "test-pnx.xml", "test-pnx.gson");

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
