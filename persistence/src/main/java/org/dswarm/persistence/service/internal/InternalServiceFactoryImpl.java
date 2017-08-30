/**
 * Copyright (C) 2013 – 2017 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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
package org.dswarm.persistence.service.internal;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.dswarm.persistence.service.InternalModelService;
import org.dswarm.persistence.service.InternalModelServiceFactory;
import org.dswarm.persistence.service.internal.graph.InternalGDMGraphService;

/**
 * An internal model service factory implementation.
 * 
 * @author tgaengler
 */
@Singleton
public class InternalServiceFactoryImpl implements InternalModelServiceFactory {

	/**
	 * The GDM graph internal model service implementation.
	 */
	private final InternalGDMGraphService	internalGDMGraphService;

	/**
	 * Creates a new internal model service factory with the given memory DB and triple internal model service implementations.
	 * 
	 * @param internalGDMGraphService the GDM graph internal model service implementation
	 */
	@Inject
	public InternalServiceFactoryImpl(final InternalGDMGraphService internalGDMGraphService) {

		this.internalGDMGraphService = internalGDMGraphService;
	}

	@Override
	public InternalModelService getInternalGDMGraphService() {

		return internalGDMGraphService;
	}
}
