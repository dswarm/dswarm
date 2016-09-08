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
package org.dswarm.controller.resources.job.test;

import com.fasterxml.jackson.databind.node.TextNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.utils.ConfigurationStatics;
import org.dswarm.persistence.service.UUIDService;

public abstract class AbstractXMLTasksResourceTest extends AbstractTasksResourceTest {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractXMLTasksResourceTest.class);

	protected final String recordTag;
	protected final String storageType;

	public AbstractXMLTasksResourceTest(final String taskJSONFileNameArg,
	                                    final String inputDataResourceFileNameArg,
	                                    final String recordTagArg,
	                                    final String storageTypeArg,
	                                    final String testPostfixArg,
	                                    final boolean prepareInputDataResourceArg,
	                                    final boolean utiliseExistingInputSchemaArg) {

		super(taskJSONFileNameArg, inputDataResourceFileNameArg, testPostfixArg, prepareInputDataResourceArg, utiliseExistingInputSchemaArg);

		recordTag = recordTagArg;
		storageType = storageTypeArg;
	}

	@Override protected PrepareConfiguration createPrepareConfiguration(
			PrepareResource prepareResource) {

		return new PrepareXMLConfiguration(prepareResource, recordTag, storageType);
	}

	private class PrepareXMLConfiguration extends PrepareConfiguration {

		private final String recordTag;
		private final String storageType;

		public PrepareXMLConfiguration(final PrepareResource prepareResource, final String recordTagArg, final String storageTypeArg) {

			super(prepareResource);

			recordTag = recordTagArg;
			storageType = storageTypeArg;
		}

		public PrepareConfiguration invoke() throws Exception {

			final String configuration1Uuid = UUIDService.getUUID(Configuration.class.getSimpleName());

			// process input data model
			conf1 = new Configuration(configuration1Uuid);

			conf1.setName("configuration " + testPostfix);
			conf1.addParameter(ConfigurationStatics.RECORD_TAG, new TextNode(recordTag));
			conf1.addParameter(ConfigurationStatics.STORAGE_TYPE, new TextNode(storageType));

			final String configurationJSONString = objectMapper.writeValueAsString(conf1);

			if (prepareInputDataResource) {

				// create configuration
				configuration = resourcesResourceTestUtils.addResourceConfiguration(resource, configurationJSONString);
			} else {

				configuration = configurationsResourceTestUtils.createObjectWithoutComparison(configurationJSONString);
			}

			return this;
		}
	}
}
