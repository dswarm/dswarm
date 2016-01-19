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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CSVTasksResourceTestDD1252 extends AbstractCSVTasksResourceTest {

	private static final Logger LOG = LoggerFactory.getLogger(CSVTasksResourceTestDD1252.class);

	private static final boolean UTILISE_EXISTING_INPUT_SCHEMA = true;

	public CSVTasksResourceTestDD1252() {

		super("csvtasks/csv.task.dd-1252.json", "csvtasks/IEEE-Proceedings_Titelliste.sample.csv", "\\t", true,
				"csvtasks/IEEE-Proceedings_Titelliste.sample.csv.result.xml", "01", UTILISE_EXISTING_INPUT_SCHEMA);
	}
}
