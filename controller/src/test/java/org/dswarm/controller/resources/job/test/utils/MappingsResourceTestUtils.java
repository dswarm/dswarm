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
package org.dswarm.controller.resources.job.test.utils;

import org.dswarm.controller.resources.test.utils.BasicResourceTestUtils;
import org.dswarm.persistence.model.job.Mapping;
import org.dswarm.persistence.model.job.proxy.ProxyMapping;
import org.dswarm.persistence.service.job.MappingService;
import org.dswarm.persistence.service.job.test.utils.MappingServiceTestUtils;

public class MappingsResourceTestUtils extends BasicResourceTestUtils<MappingServiceTestUtils, MappingService, ProxyMapping, Mapping> {

	public MappingsResourceTestUtils() {

		super("mappings", Mapping.class, MappingService.class, MappingServiceTestUtils.class);
	}
}
