package de.avgl.dmp.persistence.service.internal.test;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import de.avgl.dmp.persistence.GuicedTest;
import de.avgl.dmp.persistence.model.internal.Model;
import de.avgl.dmp.persistence.model.internal.gdm.GDMModel;
import de.avgl.dmp.persistence.model.internal.rdf.RDFModel;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.DataModel;
import de.avgl.dmp.persistence.model.resource.Resource;
import de.avgl.dmp.persistence.model.resource.ResourceType;
import de.avgl.dmp.persistence.model.resource.utils.ConfigurationStatics;
import de.avgl.dmp.persistence.model.schema.Attribute;
import de.avgl.dmp.persistence.model.schema.AttributePath;
import de.avgl.dmp.persistence.model.schema.Clasz;
import de.avgl.dmp.persistence.model.schema.Schema;
import de.avgl.dmp.persistence.service.internal.graph.InternalGDMGraphService;
import de.avgl.dmp.persistence.service.internal.graph.InternalRDFGraphService;
import de.avgl.dmp.persistence.service.internal.test.utils.InternalGDMGraphServiceTestUtils;
import de.avgl.dmp.persistence.service.resource.ConfigurationService;
import de.avgl.dmp.persistence.service.resource.DataModelService;
import de.avgl.dmp.persistence.service.resource.ResourceService;
import de.avgl.dmp.persistence.service.schema.AttributePathService;
import de.avgl.dmp.persistence.service.schema.AttributeService;
import de.avgl.dmp.persistence.service.schema.ClaszService;
import de.avgl.dmp.persistence.service.schema.SchemaService;

public class InternalGDMGraphServiceTest extends GuicedTest {

	private static final Logger	LOG	= LoggerFactory.getLogger(InternalGDMGraphServiceTest.class);

	/**
	 * write data via InternalRDFGraphService and read it via InternalGDMGraphService. TODO: adapt record uri re. current model
	 * (to ensure integrity)
	 * 
	 * @throws Exception
	 */
	@Test
	public void testReadGDMFromDB() throws Exception {

		// process input data model
		final ConfigurationService configurationService = injector.getInstance(ConfigurationService.class);
		final Configuration configuration = configurationService.createObjectTransactional().getObject();

		// config for XML
		configuration.setName("config1");
		configuration.addParameter(ConfigurationStatics.RECORD_TAG, new TextNode("record"));
		configuration.addParameter(ConfigurationStatics.STORAGE_TYPE, new TextNode("xml"));

		Configuration updatedConfiguration = configurationService.updateObjectTransactional(configuration).getObject();

		final ResourceService resourceService = injector.getInstance(ResourceService.class);
		final Resource resource = resourceService.createObjectTransactional().getObject();
		resource.setName("dmpf_bsp1.xml");
		resource.setType(ResourceType.FILE);
		resource.addConfiguration(updatedConfiguration);

		final URL fileURL = Resources.getResource("dmpf_bsp1.xml");
		final File resourceFile = FileUtils.toFile(fileURL);

		resource.addAttribute("path", resourceFile.getAbsolutePath());

		Resource updatedResource = resourceService.updateObjectTransactional(resource).getObject();

		final DataModelService dataModelService = injector.getInstance(DataModelService.class);
		final DataModel dataModel = dataModelService.createObjectTransactional().getObject();

		dataModel.setDataResource(updatedResource);
		dataModel.setConfiguration(updatedConfiguration);

		DataModel updatedDataModel = dataModelService.updateObjectTransactional(dataModel).getObject();

		final com.hp.hpl.jena.rdf.model.Model model = ModelFactory.createDefaultModel();

		final String testResourceUri = Resources.getResource("dmpf_bsp1.n3").toString();
		model.read(testResourceUri, "N3");

		final RDFModel rdfModel = new RDFModel(model, "http://data.slub-dresden.de/datamodels/22/records/18d68601-0623-42b4-ad89-f8954cc25912",
				"http://www.openarchives.org/OAI/2.0/recordType");

		final InternalRDFGraphService rdfGraphService = injector.getInstance(InternalRDFGraphService.class);

		rdfGraphService.createObject(dataModel.getId(), rdfModel);
		// finished writing RDF statements to graph

		// retrieve updated fresh data model
		final DataModel freshDataModel = dataModelService.getObject(updatedDataModel.getId());

		Assert.assertNotNull("the fresh data model shouldn't be null", freshDataModel);
		Assert.assertNotNull("the schema of the fresh data model shouldn't be null", freshDataModel.getSchema());

		final Schema schema = freshDataModel.getSchema();

		final InternalGDMGraphService gdmGraphService = injector.getInstance(InternalGDMGraphService.class);

		final Optional<Map<String, Model>> optionalModelMap = gdmGraphService.getObjects(updatedDataModel.getId(), Optional.<Integer> absent());

		Assert.assertNotNull("Ralf's MABXML record model map optional shouldn't be null", optionalModelMap);
		Assert.assertTrue("Ralf's MABXML record model map should be present", optionalModelMap.isPresent());
		Assert.assertFalse("Ralf's MABXML record model map shouldn't be empty", optionalModelMap.get().isEmpty());

		// check result
		final Map<String, Model> modelMap = optionalModelMap.get();

		Assert.assertTrue(
				"model map doesn't contain an entry for the record 'http://data.slub-dresden.de/datamodels/22/records/18d68601-0623-42b4-ad89-f8954cc25912'",
				modelMap.containsKey("http://data.slub-dresden.de/datamodels/22/records/18d68601-0623-42b4-ad89-f8954cc25912"));

		final Model recordModel = modelMap.get("http://data.slub-dresden.de/datamodels/22/records/18d68601-0623-42b4-ad89-f8954cc25912");

		Assert.assertNotNull("the record model shouldn't be null", recordModel);
		Assert.assertTrue("the record model should be a GDMModel", GDMModel.class.isInstance(recordModel));

		final GDMModel gdmRecordModel = (GDMModel) recordModel;
		final de.avgl.dmp.graph.json.Model realRecordModel = gdmRecordModel.getModel();

		Assert.assertNotNull("the real record model shouldn't be null", realRecordModel);
		Assert.assertEquals("wrong size of the record model; expected '2601'", 2601, realRecordModel.size());

		// TODO: move clean-up to tearDown2

		// clean-up
		final Map<Long, Attribute> attributes = Maps.newHashMap();

		final Map<Long, AttributePath> attributePaths = Maps.newLinkedHashMap();

		final Clasz recordClass = schema.getRecordClass();

		if (schema != null) {

			final Set<AttributePath> attributePathsToDelete = schema.getAttributePaths();

			if (attributePaths != null) {

				for (final AttributePath attributePath : attributePathsToDelete) {

					attributePaths.put(attributePath.getId(), attributePath);

					final Set<Attribute> attributesToDelete = attributePath.getAttributes();

					if (attributePathsToDelete != null) {

						for (final Attribute attribute : attributesToDelete) {

							attributes.put(attribute.getId(), attribute);
						}
					}
				}
			}
		}

		dataModelService.deleteObject(updatedDataModel.getId());
		final SchemaService schemaService = injector.getInstance(SchemaService.class);

		schemaService.deleteObject(schema.getId());

		final AttributePathService attributePathService = injector.getInstance(AttributePathService.class);

		for (final AttributePath attributePath : attributePaths.values()) {

			attributePathService.deleteObject(attributePath.getId());
		}

		final AttributeService attributeService = injector.getInstance(AttributeService.class);

		for (final Attribute attribute : attributes.values()) {

			attributeService.deleteObject(attribute.getId());
		}

		final ClaszService claszService = injector.getInstance(ClaszService.class);

		claszService.deleteObject(recordClass.getId());

		configurationService.deleteObject(updatedConfiguration.getId());
		resourceService.deleteObject(updatedResource.getId());
	}

	@After
	public void tearDown2() {

		InternalGDMGraphServiceTestUtils.cleanGraphDB();
	}
}
