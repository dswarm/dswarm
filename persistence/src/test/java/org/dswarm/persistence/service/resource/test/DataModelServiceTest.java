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
package org.dswarm.persistence.service.resource.test;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.resource.proxy.ProxyDataModel;
import org.dswarm.persistence.service.resource.DataModelService;
import org.dswarm.persistence.service.resource.test.utils.DataModelServiceTestUtils;
import org.dswarm.persistence.service.test.BasicJPAServiceTest;

public class DataModelServiceTest extends BasicJPAServiceTest<ProxyDataModel, DataModel, DataModelService> {

	private static final Logger LOG = LoggerFactory.getLogger(DataModelServiceTest.class);

	private DataModelServiceTestUtils dataModelServiceTestUtils;

	public DataModelServiceTest() {

		super("data model", DataModelService.class);
	}

	@Override protected void initObjects() {
		super.initObjects();

		dataModelServiceTestUtils = new DataModelServiceTestUtils();
	}

	@Test
	public void testSimpleObject() throws Exception {

		final DataModel dataModel = dataModelServiceTestUtils.createAndPersistDefaultObject();

		dataModel.setDeprecated(true);

		final DataModel updatedDataModel = dataModelServiceTestUtils.updateAndCompareObject(dataModel, dataModel);

		logObjectJSON(updatedDataModel);
	}
}
