package de.avgl.dmp.persistence.model.internal.impl.test;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import de.avgl.dmp.persistence.model.internal.impl.RDFModel;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

/**
 * 
 * @author tgaengler
 *
 */
public class RDFModelTest {

	@Test
	public void testToJSON() {

		testToJSONInternal("src/test/resources/test-mabxml.n3",
				"http://data.slub-dresden.de/resources/1/configurations/1/records/0a878376-affb-45cd-9f62-1619c6df4c11", "test-mabxml.json");
	}

	@Test
	public void testToJSON2() {

		testToJSONInternal("src/test/resources/test-complex-xml.n3",
				"http://data.slub-dresden.de/resources/1/configurations/1/records/d5d85499-b4a4-42aa-b492-b2927ac37384", "test-complex-xml.json");
	}

	public void testToJSONInternal(final String fileName, final String resourceURI, final String expectedFileName) {

		// prepare
		final File file = new File(fileName);
		final Model model = ModelFactory.createDefaultModel();

		model.read(file.toURI().toString(), "N3");

		final RDFModel rdfModel = new RDFModel(model, resourceURI);
		final JsonNode jsonNode = rdfModel.toJSON();

		String jsonString = null;
		try {
			jsonString = DMPPersistenceUtil.getJSONObjectMapper().writeValueAsString(jsonNode);
		} catch (JsonProcessingException e) {

			e.printStackTrace();
		}

		Assert.assertNotNull("the JSON string shouldn't be null", jsonString);

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
