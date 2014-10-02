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
package org.dswarm.converter.flow.test.ralfsmabxml;

import org.dswarm.converter.flow.test.xml.AbstractXMLTransformationFlowTest;

/**
 * @author tgaengler
 */
public abstract class AbstractRalfsMABXMLTransformationFlowTest extends AbstractXMLTransformationFlowTest {

	public AbstractRalfsMABXMLTransformationFlowTest(final String taskJSONFileName, final String taskResultJSONFileName) {

		super(taskJSONFileName, taskResultJSONFileName, "record", null, "dmpf_bsp1.xml");
	}
}
