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
package org.dswarm.controller.resources.job.test.utils;

import org.dswarm.controller.resources.test.utils.ExtendedBasicDMPResourceTestUtils;
import org.dswarm.persistence.model.job.Function;
import org.dswarm.persistence.model.job.proxy.ProxyBasicFunction;
import org.dswarm.persistence.service.job.BasicFunctionService;
import org.dswarm.persistence.service.job.test.utils.BasicFunctionServiceTestUtils;

public abstract class BasicFunctionsResourceTestUtils<POJOCLASSPERSISTENCESERVICETESTUTILS extends BasicFunctionServiceTestUtils<POJOCLASSPERSISTENCESERVICE, PROXYPOJOCLASS, POJOCLASS>, POJOCLASSPERSISTENCESERVICE extends BasicFunctionService<PROXYPOJOCLASS, POJOCLASS>, PROXYPOJOCLASS extends ProxyBasicFunction<POJOCLASS>, POJOCLASS extends Function>
		extends ExtendedBasicDMPResourceTestUtils<POJOCLASSPERSISTENCESERVICETESTUTILS, POJOCLASSPERSISTENCESERVICE, PROXYPOJOCLASS, POJOCLASS> {

	public BasicFunctionsResourceTestUtils(final String resourceIdentifier, final Class<POJOCLASS> pojoClassArg,
			final Class<POJOCLASSPERSISTENCESERVICE> persistenceServiceClassArg,
			final Class<POJOCLASSPERSISTENCESERVICETESTUTILS> persistenceServiceTestUtilsClassArg) {

		super(resourceIdentifier, pojoClassArg, persistenceServiceClassArg, persistenceServiceTestUtilsClassArg);
	}
}
