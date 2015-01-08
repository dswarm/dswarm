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
package org.dswarm.controller.resources.schema.utils;

import javax.inject.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.dswarm.controller.resources.utils.BasicDMPResourceUtils;
import org.dswarm.controller.resources.utils.ResourceUtilsFactory;
import org.dswarm.persistence.model.schema.AttributePathInstance;
import org.dswarm.persistence.model.schema.proxy.ProxyAttributePathInstance;
import org.dswarm.persistence.service.schema.AttributePathInstanceService;

/**
 * @param <POJOCLASSPERSISTENCESERVICE>
 * @param <POJOCLASS>
 * @author tgaengler
 */
public abstract class AttributePathInstancesResourceUtils<POJOCLASSPERSISTENCESERVICE extends AttributePathInstanceService<PROXYPOJOCLASS, POJOCLASS>, PROXYPOJOCLASS extends ProxyAttributePathInstance<POJOCLASS>, POJOCLASS extends AttributePathInstance>
		extends BasicDMPResourceUtils<POJOCLASSPERSISTENCESERVICE, PROXYPOJOCLASS, POJOCLASS> {

	public AttributePathInstancesResourceUtils(final Class<POJOCLASS> pojoClassArg,
			final Provider<POJOCLASSPERSISTENCESERVICE> persistenceServiceProviderArg, final Provider<ObjectMapper> objectMapperProviderArg,
			final ResourceUtilsFactory utilsFactory) {

		super(pojoClassArg, persistenceServiceProviderArg, objectMapperProviderArg, utilsFactory);
	}
}
