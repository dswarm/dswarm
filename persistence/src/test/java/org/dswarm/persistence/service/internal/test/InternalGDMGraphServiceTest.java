/**
 * Copyright (C) 2013 – 2016 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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
package org.dswarm.persistence.service.internal.test;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;
import javaslang.Tuple2;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import org.dswarm.persistence.GuicedTest;
import org.dswarm.persistence.model.internal.Model;
import org.dswarm.persistence.model.internal.gdm.GDMModel;
import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.resource.Resource;
import org.dswarm.persistence.model.resource.ResourceType;
import org.dswarm.persistence.model.resource.utils.ConfigurationStatics;
import org.dswarm.persistence.model.schema.Attribute;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.model.schema.SchemaAttributePathInstance;
import org.dswarm.persistence.service.internal.graph.InternalGDMGraphService;
import org.dswarm.persistence.service.resource.ConfigurationService;
import org.dswarm.persistence.service.resource.DataModelService;
import org.dswarm.persistence.service.resource.ResourceService;
import org.dswarm.persistence.util.DMPPersistenceUtil;

public class InternalGDMGraphServiceTest extends GuicedTest {

	private static final Logger LOG = LoggerFactory.getLogger(InternalGDMGraphServiceTest.class);

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
		final org.dswarm.graph.json.Model model = org.dswarm.graph.json.util.Util.getJSONObjectMapper().readValue(testResourceString,
				org.dswarm.graph.json.Model.class);

		final GDMModel rdfModel = new GDMModel(model, "http://data.slub-dresden.de/datamodels/22/records/18d68601-0623-42b4-ad89-f8954cc25912",
				"http://www.openarchives.org/OAI/2.0/recordType");

		final InternalGDMGraphService rdfGraphService = GuicedTest.injector.getInstance(InternalGDMGraphService.class);

		final Observable<Response> responseObservable = rdfGraphService.createObject(dataModel.getUuid(), Observable.just(rdfModel));

		final Response response = responseObservable.toBlocking().firstOrDefault(null);
		// finished writing RDF statements to graph

		Assert.assertNotNull(response);

		// retrieve updated fresh data model
		final DataModel freshDataModel = dataModelService.getObject(updatedDataModel.getUuid());

		Assert.assertNotNull("the fresh data model shouldn't be null", freshDataModel);
		Assert.assertNotNull("the schema of the fresh data model shouldn't be null", freshDataModel.getSchema());

		final InternalGDMGraphService gdmGraphService = GuicedTest.injector.getInstance(InternalGDMGraphService.class);

		final Observable<Map<String, Model>> optionalModelMapObservable = gdmGraphService
				.getObjects(updatedDataModel.getUuid(), Optional.empty())
				.toMap(Tuple2::_1, Tuple2::_2);
		final Optional<Map<String, Model>> optionalModelMap = optionalModelMapObservable.map(Optional::of).toBlocking()
				.firstOrDefault(Optional.empty());

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
		final org.dswarm.graph.json.Model realRecordModel = gdmRecordModel.getModel();

		Assert.assertNotNull("the real record model shouldn't be null", realRecordModel);
		Assert.assertEquals("wrong size of the record model; expected '2601'", 2601, realRecordModel.size());
	}

	@Test
	public void testCheckSchemaCreation() throws Exception {

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
		resource.setName("testset.xml");
		resource.setType(ResourceType.FILE);
		resource.addConfiguration(updatedConfiguration);

		final URL fileURL = Resources.getResource("testset.xml");
		final File resourceFile = FileUtils.toFile(fileURL);

		resource.addAttribute("path", resourceFile.getAbsolutePath());

		final Resource updatedResource = resourceService.updateObjectTransactional(resource).getObject();

		final DataModelService dataModelService = GuicedTest.injector.getInstance(DataModelService.class);
		final DataModel dataModel = dataModelService.createObjectTransactional().getObject();

		dataModel.setDataResource(updatedResource);
		dataModel.setConfiguration(updatedConfiguration);

		final DataModel updatedDataModel = dataModelService.updateObjectTransactional(dataModel).getObject();

		final String testResourceString = DMPPersistenceUtil.getResourceAsString("testset.json");
		final org.dswarm.graph.json.Model model = org.dswarm.graph.json.util.Util.getJSONObjectMapper().readValue(testResourceString,
				org.dswarm.graph.json.Model.class);

		final GDMModel rdfModel = new GDMModel(model, null, "http://data.slub-dresden.de/resources/1/schema#recordType");

		final Set<String> recordURIs = Sets.newHashSet();
		recordURIs.add("http://data.slub-dresden.de/datamodels/3/records/fdd51aac-61ca-4a73-b679-05d10c669659");
		recordURIs.add("http://data.slub-dresden.de/datamodels/3/records/0874190c-f7e1-42dd-b591-f2c669d1bacd");
		recordURIs.add("http://data.slub-dresden.de/datamodels/3/records/9b86dc50-b70a-49ee-93ff54");
		recordURIs.add("http://data.slub-dresden.de/datamodels/3/records/a93be5cf-e143-41ca-aa76-f5c602f98722");
		recordURIs.add("http://data.slub-dresden.de/datamodels/3/records/d3c18314-ad40-4e38-9fd2-f53f4f22cfd1");
		recordURIs.add("http://data.slub-dresden.de/datamodels/3/records/2c57b726-9e18-4357-827b-28800f641585");
		recordURIs.add("http://data.slub-dresden.de/datamodels/3/records/75e2dc01-0c98-4500-82c1-efa9192d99fb");
		rdfModel.setRecordURIs(recordURIs);

		final InternalGDMGraphService rdfGraphService = GuicedTest.injector.getInstance(InternalGDMGraphService.class);

		rdfGraphService.createObject(dataModel.getUuid(), Observable.just(rdfModel));
		// finished writing RDF statements to graph

		// retrieve updated fresh data model
		final DataModel freshDataModel = dataModelService.getObject(updatedDataModel.getUuid());

		Assert.assertNotNull("the fresh data model shouldn't be null", freshDataModel);
		Assert.assertNotNull("the schema of the fresh data model shouldn't be null", freshDataModel.getSchema());

		final Schema schema = freshDataModel.getSchema();

		Assert.assertNotNull(schema);

		final Set<SchemaAttributePathInstance> sattributePaths = schema.getUniqueAttributePaths();

		Assert.assertNotNull(sattributePaths);

		final Set<Attribute> sattributes = Sets.newHashSet();

		for (final SchemaAttributePathInstance attributePath : sattributePaths) {

			final Set<Attribute> apAttributes = attributePath.getAttributePath().getAttributes();

			if (apAttributes == null) {

				continue;
			}

			sattributes.addAll(apAttributes);
		}

		Assert.assertEquals(7, sattributes.size());
	}
}
