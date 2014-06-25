package org.dswarm.persistence.service.internal.test;

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

import org.dswarm.persistence.GuicedTest;
import org.dswarm.persistence.model.internal.Model;
import org.dswarm.persistence.model.internal.gdm.GDMModel;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.resource.Resource;
import org.dswarm.persistence.model.resource.ResourceType;
import org.dswarm.persistence.model.resource.utils.ConfigurationStatics;
import org.dswarm.persistence.model.schema.Attribute;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.Clasz;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.service.internal.graph.InternalGDMGraphService;
import org.dswarm.persistence.service.internal.test.utils.InternalGDMGraphServiceTestUtils;
import org.dswarm.persistence.service.resource.ConfigurationService;
import org.dswarm.persistence.service.resource.DataModelService;
import org.dswarm.persistence.service.resource.ResourceService;
import org.dswarm.persistence.service.schema.SchemaService;
import org.dswarm.persistence.service.schema.test.utils.AttributePathServiceTestUtils;
import org.dswarm.persistence.service.schema.test.utils.AttributeServiceTestUtils;
import org.dswarm.persistence.service.schema.test.utils.ClaszServiceTestUtils;
import org.dswarm.persistence.util.DMPPersistenceUtil;

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
		final ConfigurationService configurationService = GuicedTest.injector.getInstance(ConfigurationService.class);
		final Configuration configuration = configurationService.createObjectTransactional().getObject();

		// config for XML
		configuration.setName("config1");
		configuration.addParameter(ConfigurationStatics.RECORD_TAG, new TextNode("record"));
		configuration.addParameter(ConfigurationStatics.STORAGE_TYPE, new TextNode("xml"));

		final Configuration updatedConfiguration = configurationService.updateObjectTransactional(configuration).getObject();

		final ResourceService resourceService = GuicedTest.injector.getInstance(ResourceService.class);
		final Resource resource = resourceService.createObjectTransactional().getObject();
		resource.setName("dmpf_bsp1.xml");
		resource.setType(ResourceType.FILE);
		resource.addConfiguration(updatedConfiguration);

		final URL fileURL = Resources.getResource("dmpf_bsp1.xml");
		final File resourceFile = FileUtils.toFile(fileURL);

		resource.addAttribute("path", resourceFile.getAbsolutePath());

		final Resource updatedResource = resourceService.updateObjectTransactional(resource).getObject();

		final DataModelService dataModelService = GuicedTest.injector.getInstance(DataModelService.class);
		final DataModel dataModel = dataModelService.createObjectTransactional().getObject();

		dataModel.setDataResource(updatedResource);
		dataModel.setConfiguration(updatedConfiguration);

		final DataModel updatedDataModel = dataModelService.updateObjectTransactional(dataModel).getObject();

		final String testResourceString = DMPPersistenceUtil.getResourceAsString("dmpf_bsp1.json");
		final de.avgl.dmp.graph.json.Model model = de.avgl.dmp.graph.json.util.Util.getJSONObjectMapper().readValue(testResourceString,
				de.avgl.dmp.graph.json.Model.class);

		final GDMModel rdfModel = new GDMModel(model, "http://data.slub-dresden.de/datamodels/22/records/18d68601-0623-42b4-ad89-f8954cc25912",
				"http://www.openarchives.org/OAI/2.0/recordType");

		final InternalGDMGraphService rdfGraphService = GuicedTest.injector.getInstance(InternalGDMGraphService.class);

		rdfGraphService.createObject(dataModel.getId(), rdfModel);
		// finished writing RDF statements to graph

		// retrieve updated fresh data model
		final DataModel freshDataModel = dataModelService.getObject(updatedDataModel.getId());

		Assert.assertNotNull("the fresh data model shouldn't be null", freshDataModel);
		Assert.assertNotNull("the schema of the fresh data model shouldn't be null", freshDataModel.getSchema());

		final Schema schema = freshDataModel.getSchema();

		final InternalGDMGraphService gdmGraphService = GuicedTest.injector.getInstance(InternalGDMGraphService.class);

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
		final SchemaService schemaService = GuicedTest.injector.getInstance(SchemaService.class);

		schemaService.deleteObject(schema.getId());

		final AttributePathServiceTestUtils attributePathServiceTestUtils = new AttributePathServiceTestUtils();

		for (final AttributePath attributePath : attributePaths.values()) {

			attributePathServiceTestUtils.deleteObject(attributePath);
		}

		final AttributeServiceTestUtils attributeServiceTestUtils = new AttributeServiceTestUtils();

		for (final Attribute attribute : attributes.values()) {

			attributeServiceTestUtils.deleteObject(attribute);
		}

		final ClaszServiceTestUtils claszServiceTestUtils = new ClaszServiceTestUtils();

		claszServiceTestUtils.deleteObject(recordClass);

		configurationService.deleteObject(updatedConfiguration.getId());
		resourceService.deleteObject(updatedResource.getId());
	}

	@After
	public void tearDown2() {

		InternalGDMGraphServiceTestUtils.cleanGraphDB();
	}
}
