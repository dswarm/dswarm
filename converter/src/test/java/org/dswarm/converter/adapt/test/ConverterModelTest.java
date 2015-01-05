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

import java.net.URI;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.persistence.adapt.JsonModelExportException;
import org.dswarm.converter.adapt.JsonModelTransformException;
import org.dswarm.persistence.test.DMPPersistenceTestUtils;

/**
 * @author sbarthel
 * @author tgaengler
 */
public class ConverterModelTest extends ModelTest {

	private static final Logger log = LoggerFactory.getLogger(ConverterModelTest.class);

	//@Test
	public void shouldTransformResource() {
		try {
			for (final URI uri : DMPPersistenceTestUtils.collectResources(root)) {
				rewriteTaskJSON(uri, true);
			}
		} catch (JsonModelTransformException | JsonModelExportException e) {
			ConverterModelTest.log.error(e.getMessage(), e);
			Assert.fail(e.getMessage());
		}
	}

	//@Test
	public void rewriteSchemaJSONs() throws Exception {

		rewriteSchemaJSON("mabxml.schema.json");
		rewriteSchemaJSON("mabxml-1.schema.json");
		rewriteSchemaJSON("ralfs_mabxml_dmp_schema.json");
	}

	//@Test
	public void rewriteTaskJSONs() throws Exception {

		rewriteTaskJSON("complex-transformation.json");
		rewriteTaskJSON("converter_task.csv.json");
		rewriteTaskJSON("converter_task.json");
		rewriteTaskJSON("dmpf-task.json");
	}
}
