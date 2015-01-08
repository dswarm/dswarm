/**
 * Copyright (C) 2013, 2014 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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
package org.dswarm.converter.adapt.test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.google.inject.Key;
import com.google.inject.name.Names;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.persistence.adapt.IdUuidTransformer;
import org.dswarm.persistence.adapt.JsonModelExportException;
import org.dswarm.persistence.test.DMPPersistenceTestUtils;
import org.dswarm.persistence.GuicedTest;

public class IdUuidTransformerConverterTest extends GuicedTest {

	private static final Logger log = LoggerFactory.getLogger(IdUuidTransformerConverterTest.class);

	protected final String root;

	public IdUuidTransformerConverterTest() {

		root = GuicedTest.injector.getInstance(Key.get(String.class, Names.named("dswarm.paths.root"))) + "/converter";
	}

	//@Test
	public void convertIdToUuidInConverterTest() throws JsonModelExportException, IOException, URISyntaxException {

		try {
			for (final URI uri : DMPPersistenceTestUtils.collectResources(root)) {
				IdUuidTransformer.transformIdToUuidInJsonObjectFile(uri);
			}
		} catch (JsonModelExportException e) {
			IdUuidTransformerConverterTest.log.error(e.getMessage(), e);
			Assert.fail(e.getMessage());
		}

		IdUuidTransformer.transformIdToUuidInJsonObjectFile("mabxml.schema.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("mabxml-1.schema.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("ralfs_mabxml_dmp_schema.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("complex-transformation.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("converter_task.csv.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("converter_task.json", root);
		IdUuidTransformer.transformIdToUuidInJsonObjectFile("dmpf-task.json", root);
	}
}
