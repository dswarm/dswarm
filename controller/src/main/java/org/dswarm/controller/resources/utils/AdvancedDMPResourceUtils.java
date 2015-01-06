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
package org.dswarm.controller.resources.utils;

import javax.inject.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.dswarm.controller.DMPControllerException;
import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.AdvancedDMPJPAObject;
import org.dswarm.persistence.model.proxy.ProxyAdvancedDMPJPAObject;
import org.dswarm.persistence.service.AdvancedDMPJPAService;

/**
 * @author tgaengler
 * @param <POJOCLASSPERSISTENCESERVICE>
 * @param <POJOCLASS>
 * @param <POJOCLASSIDTYPE>
 */
public abstract class AdvancedDMPResourceUtils<POJOCLASSPERSISTENCESERVICE extends AdvancedDMPJPAService<PROXYPOJOCLASS, POJOCLASS>, PROXYPOJOCLASS extends ProxyAdvancedDMPJPAObject<POJOCLASS>, POJOCLASS extends AdvancedDMPJPAObject>
		extends BasicDMPResourceUtils<POJOCLASSPERSISTENCESERVICE, PROXYPOJOCLASS, POJOCLASS> {

	public AdvancedDMPResourceUtils(final Class<POJOCLASS> pojoClassArg, final Provider<POJOCLASSPERSISTENCESERVICE> persistenceServiceProviderArg,
			final Provider<ObjectMapper> objectMapperProviderArg, final ResourceUtilsFactory utilsFactoryArg) {

		super(pojoClassArg, persistenceServiceProviderArg, objectMapperProviderArg, utilsFactoryArg);
	}

	@Override
	public String prepareObjectJSONString(final String objectJSONString) throws DMPControllerException {

		// an attribute or clasz is not a complex object

		return objectJSONString;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PROXYPOJOCLASS createObject(final POJOCLASS objectFromJSON, final POJOCLASSPERSISTENCESERVICE persistenceService)
			throws DMPPersistenceException {

		return persistenceService.createOrGetObjectTransactional(objectFromJSON.getUri());
	}
}
