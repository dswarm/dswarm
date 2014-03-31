package de.avgl.dmp.persistence.model.internal.impl.test;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.io.Resources;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import de.avgl.dmp.persistence.model.internal.rdf.RDFModel;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

/**
 *
 * @author tgaengler
 *
 */
public class RDFModelTest {

	@Test
	public void testToJSON() {

		testToJSONInternal("test-mabxml.n3",
				"http://data.slub-dresden.de/resources/1/configurations/1/records/aa916dc1-231a-4552-89e3-822886715fa9", "test-mabxml.json");
	}

	@Test
	public void testToJSON2() {

		testToJSONInternal("test-complex-xml.n3",
				"http://data.slub-dresden.de/resources/1/configurations/1/records/d5d85499-b4a4-42aa-b492-b2927ac37384", "test-complex-xml.json");
	}
	
	@Test
	public void testGetSchema() {

		testGetSchemaInternal("test-mabxml.n3",
				"http://data.slub-dresden.de/resources/1/configurations/1/records/aa916dc1-231a-4552-89e3-822886715fa9", "test-mabxml.json");
	}

	private void testToJSONInternal(final String fileName, final String resourceURI, final String expectedFileName) {

		// prepare
//		final File file = new File(fileName);
		final Model model = ModelFactory.createDefaultModel();

		final String testResourceUri = Resources.getResource(fileName).toString();
		model.read(testResourceUri, "N3");

		final RDFModel rdfModel = new RDFModel(model, resourceURI);
		final JsonNode jsonNode = rdfModel.toRawJSON();

		String jsonString = null;
		try {
			jsonString = DMPPersistenceUtil.getJSONObjectMapper().writeValueAsString(jsonNode);
		} catch (JsonProcessingException e) {

			e.printStackTrace();
		}

		Assert.assertNotNull("the JSON string shouldn't be null", jsonString);
		
		// System.out.println(jsonString);

		String expectedJsonString = null;

		try {

			expectedJsonString = DMPPersistenceUtil.getResourceAsString(expectedFileName);
		} catch (IOException e) {

			e.printStackTrace();
		}

		Assert.assertNotNull("the JSON string shouldn't be null", expectedJsonString);

		// TODO: do proper comparison of the JSON objects
	}
	
	private void testGetSchemaInternal(final String fileName, final String resourceURI, final String expectedFileName) {

		// prepare
//		final File file = new File(fileName);
		final Model model = ModelFactory.createDefaultModel();

		final String testResourceUri = Resources.getResource(fileName).toString();
		model.read(testResourceUri, "N3");

		final RDFModel rdfModel = new RDFModel(model, resourceURI);
		final JsonNode jsonNode = rdfModel.getSchema();

		String jsonString = null;
		try {
			jsonString = DMPPersistenceUtil.getJSONObjectMapper().writeValueAsString(jsonNode);
		} catch (JsonProcessingException e) {

			e.printStackTrace();
		}

		Assert.assertNotNull("the JSON string shouldn't be null", jsonString);
		
		// System.out.println(jsonString);

		String expectedJsonString = null;

		try {

			expectedJsonString = DMPPersistenceUtil.getResourceAsString(expectedFileName);
		} catch (IOException e) {

			e.printStackTrace();
		}

		Assert.assertNotNull("the JSON string shouldn't be null", expectedJsonString);

		// TODO: do proper comparison of the JSON objects
	}
}
