/**
 * Copyright (C) 2013 – 2017 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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
package org.dswarm.controller.adapt.test;

import java.io.IOException;
import java.net.URISyntaxException;

import com.google.inject.Key;
import com.google.inject.name.Names;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.persistence.GuicedTest;
import org.dswarm.persistence.adapt.IdUuidTransformer;
import org.dswarm.persistence.adapt.JsonModelExportException;

public class IdUuidTransformerControllerTest extends GuicedTest {

	private static final Logger log = LoggerFactory.getLogger(IdUuidTransformerControllerTest.class);

	protected final String root;

	public IdUuidTransformerControllerTest() {

		root = GuicedTest.injector.getInstance(Key.get(String.class, Names.named("dswarm.paths.root"))) + "/controller";
	}

	//@Test
	public void convertIdToUuidInControllerTest() throws JsonModelExportException, IOException, URISyntaxException {

		IdUuidTransformer.transformIdToUuidInJsonObjectFile("atMostTwoRowsCsv_Configuration.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("atMostTwoRowsCsv_Resource.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("attribute1.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("attribute2.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("attribute3.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("attribute4.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("attribute5.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("attribute6.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("attribute7.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("attribute8.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("attribute9.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("attribute10.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("attribute_path.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("attribute_path1.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("attribute_path2.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("attribute_path3.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("attribute_path4.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("attribute_path5.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("attribute_path6.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("attribute_path7.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("attribute_path8.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("attribute_path9.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("attribute_path10.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("broken_filter.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("clasz.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("clasz1.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("clasz2.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("complex_resource.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("component.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("component1.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("configuration2.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("content_schema.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("controller_configuration.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("datamodel.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("datamodel1.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("filter.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("filter2.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("filter3.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("function.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("function1.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("input_mapping_attribute_path_instance.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("mabxml-configuration.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("mabxml_datamodel.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("mapping.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("mapping1.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("mapping_attribute_path_instance.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("mapping_attribute_path_instance2.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("output_mapping_attribute_path_instance.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("project.dd376.multiple_inputmappingattributepathinstances.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("project.full.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("project.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("project.w.new.entities.and.dummy.ids.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("project.w.new.entities.and.real.ids.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("project.w.new.entities.and.dummy.ids.onepersistentmappingattributepathinstance.json",
				root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("project.w.new.entities.and.real.ids.onepersistentmappingattributepathinstance.json",
				root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("project_to_remove_mapping_from_with_original_IDs_and_output_data_model.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("resource.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("resource1.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("resource2.json", root);
		IdUuidTransformer.transformIdToUuidInJsonArrayFile("resource_configurations.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("schema.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("schema_attribute_path_instance.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("project_to_remove_mapping_from_with_dummy_IDs.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("project.w.new.entities.onepersistentmappingattributepathinstance.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("project.w.new.entities.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("task.csv.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("task.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("dmpf-task.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("test-mabxml-resource.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("test-mabxml-resource2.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("transformation.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("transformation1.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("transformation_component.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("transformation_component1.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("UTF-8Csv_Configuration.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("UTF-8Csv_Resource.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("xml-configuration.json", root);
	}
}
