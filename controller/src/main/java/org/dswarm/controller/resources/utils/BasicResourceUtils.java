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
package org.dswarm.controller.resources.utils;

import java.io.IOException;
import java.util.List;

import javax.inject.Provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.controller.DMPControllerException;
import org.dswarm.controller.DMPJsonException;
import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.DMPObject;
import org.dswarm.persistence.model.proxy.ProxyDMPObject;
import org.dswarm.persistence.service.BasicJPAService;

/**
 * @param <POJOCLASSPERSISTENCESERVICE>
 * @param <POJOCLASS>
 * @author tgaengler
 */
public abstract class BasicResourceUtils<POJOCLASSPERSISTENCESERVICE extends BasicJPAService<PROXYPOJOCLASS, POJOCLASS>, PROXYPOJOCLASS extends ProxyDMPObject<POJOCLASS>, POJOCLASS extends DMPObject> {

	private static final Logger LOG = LoggerFactory.getLogger(BasicResourceUtils.class);

	protected final Class<POJOCLASS> pojoClass;

	protected final String pojoClassName;

	protected final Provider<POJOCLASSPERSISTENCESERVICE> persistenceServiceProvider;

	protected final Provider<ObjectMapper> objectMapperProvider;

	protected final ResourceUtilsFactory utilsFactory;

	public BasicResourceUtils(final Class<POJOCLASS> pojoClassArg, final Provider<POJOCLASSPERSISTENCESERVICE> persistenceServiceProviderArg,
			final Provider<ObjectMapper> objectMapperProviderArg,
			final ResourceUtilsFactory utilsFactoryArg) {

		persistenceServiceProvider = persistenceServiceProviderArg;

		objectMapperProvider = objectMapperProviderArg;

		utilsFactory = utilsFactoryArg;

		pojoClass = pojoClassArg;
		pojoClassName = pojoClass.getSimpleName();
	}

	/**
	 * Gets the concrete POJO class of this resource (controller service).
	 *
	 * @return the concrete POJO class
	 */
	public Class<POJOCLASS> getClasz() {

		return pojoClass;
	}

	public String getClaszName() {

		return pojoClassName;
	}

	public ObjectMapper getObjectMapper() {

		return objectMapperProvider.get();
	}

	public POJOCLASSPERSISTENCESERVICE getPersistenceService() {

		return persistenceServiceProvider.get();
	}

	public List<POJOCLASS> getObjects() {

		return persistenceServiceProvider.get().getObjects();
	}

	public void deleteObject(final String uuid) {

		if (uuid != null) {

			persistenceServiceProvider.get().deleteObject(uuid);
		}
	}

	public POJOCLASS deserializeObjectJSONString(final String objectJSONString) throws DMPControllerException {

		POJOCLASS objectFromJSON = null;

		try {

			objectFromJSON = objectMapperProvider.get().readValue(objectJSONString, pojoClass);
		} catch (final JsonMappingException je) {

			throw new DMPJsonException(String.format("something went wrong while deserializing the %s JSON string", pojoClassName), je);
		} catch (final IOException e) {

			BasicResourceUtils.LOG.debug("something went wrong while deserializing the {} JSON string", pojoClassName);

			throw new DMPControllerException(String.format("something went wrong while deserializing the %s JSON string.\n%s", pojoClassName, e.getMessage()));
		}

		if (objectFromJSON == null) {

			throw new DMPControllerException(String.format("deserialized %s is null", pojoClassName));
		}

		return objectFromJSON;
	}

	public String serializeObject(final Object object) throws DMPControllerException {

		String objectJSONString = null;

		try {

			objectJSONString = objectMapperProvider.get().writeValueAsString(object);
		} catch (final JsonProcessingException e) {

			BasicResourceUtils.LOG.debug("couldn't serialize enhanced {} JSON.", pojoClassName);

			throw new DMPControllerException("couldn't serialize enhanced " + pojoClassName + " JSON.\n" + e.getMessage());
		}

		if (objectJSONString == null) {

			BasicResourceUtils.LOG.debug("couldn't serialize enhanced {} JSON correctly.", pojoClassName);

			throw new DMPControllerException("couldn't serialize enhanced " + pojoClassName + " JSON correctly.\n");
		}

		return objectJSONString;
	}

	/**
	 * Creates and persists a new object into the database.
	 *
	 * @param objectFromJSON     the new object
	 * @param persistenceService the related persistence service
	 * @return the persisted object
	 * @throws DMPPersistenceException
	 */
	public PROXYPOJOCLASS createObject(final POJOCLASS objectFromJSON, final POJOCLASSPERSISTENCESERVICE persistenceService)
			throws DMPPersistenceException {

		return persistenceService.createObjectTransactional();
	}
}
