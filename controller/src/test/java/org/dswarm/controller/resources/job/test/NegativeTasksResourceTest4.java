/**
 * Copyright (C) 2013 – 2015 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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

/**
 * wrong record tag at xml data test
 *
 * @author tgaengler
 */
public class NegativeTasksResourceTest4 extends AbstractNegativeTasksResourceTest {

	private static final String taskJSONFileName          = "dd-538/oai-pmh_marcxml_controller_task.01.json";
	private static final String inputDataResourceFileName = "controller_test-mabxml.xml";
	private static final String recordTag                 = "record";
	private static final String storageType               = "xml";
	private static final String testPostfix               = "wrong record tag at xml data";
	private static final String expectedResponse          = "{\"error\":{\"message\":\"couldn't process task (maybe XML export) successfully\",\"stacktrace\":\"java.lang.RuntimeException: couldn't transform any record from xml data resource at '/home/tgaengler/git/tgaengler/dswarm/tmp/resources/controller_test-mabxml.xml' to GDM for data model 'DataModel-2e0c9850-6def-4942-abed-b513d3f56eba'; maybe you set a wrong record tag (current one = 'record')";
	private static final boolean prepateInputDataResource = true;
	private static final int    expectedResponseCode      = 200;

	public NegativeTasksResourceTest4() {

		super(taskJSONFileName, inputDataResourceFileName, recordTag, storageType, testPostfix, expectedResponse, prepateInputDataResource, expectedResponseCode);
	}
}
