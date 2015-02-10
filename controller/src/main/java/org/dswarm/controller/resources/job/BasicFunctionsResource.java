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
package org.dswarm.controller.resources.job;

import javax.inject.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.dswarm.controller.resources.ExtendedBasicDMPResource;
import org.dswarm.persistence.model.job.Function;
import org.dswarm.persistence.model.job.proxy.ProxyBasicFunction;
import org.dswarm.persistence.service.job.BasicFunctionService;

/**
 * A generic resource (controller service) for {@link Function}s.
 *
 * @param <POJOCLASSPERSISTENCESERVICE> the concrete {@link Function} persistence service of the resource that is related to the
 *                                      concrete {@link Function} class
 * @param <POJOCLASS>                   the concrete {@link Function} class
 * @author tgaengler
 */
public abstract class BasicFunctionsResource<POJOCLASSPERSISTENCESERVICE extends BasicFunctionService<PROXYPOJOCLASS, POJOCLASS>, PROXYPOJOCLASS extends ProxyBasicFunction<POJOCLASS>, POJOCLASS extends Function>
		extends ExtendedBasicDMPResource<POJOCLASSPERSISTENCESERVICE, PROXYPOJOCLASS, POJOCLASS> {

	/**
	 * Creates a new resource (controller service) for the given concrete {@link Function} class with the provider of the concrete
	 * {@link Function} persistence service, the object mapper and metrics registry.
	 *
	 * @param pojoClassArg                  a concrete POJO class
	 * @param persistenceServiceProviderArg the concrete persistence service that is related to the concrete POJO class
	 * @param objectMapperProviderArg       an object mapper
	 */
	public BasicFunctionsResource(final Class<POJOCLASS> pojoClassArg, final Provider<POJOCLASSPERSISTENCESERVICE> persistenceServiceProviderArg,
	                              final Provider<ObjectMapper> objectMapperProviderArg) {

		super(pojoClassArg, persistenceServiceProviderArg, objectMapperProviderArg);
	}

	/**
	 * {@inheritDoc}<br/>
	 * Updates the name, description, parameters and machine processable function description of the function.
	 */
	@Override
	protected POJOCLASS prepareObjectForUpdate(final POJOCLASS objectFromJSON, final POJOCLASS object) {

		super.prepareObjectForUpdate(objectFromJSON, object);

		object.setFunctionDescription(objectFromJSON.getFunctionDescription());
		object.setParameters(objectFromJSON.getParameters());

		return object;
	}
}
