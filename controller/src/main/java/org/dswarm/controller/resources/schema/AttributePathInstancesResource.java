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
package org.dswarm.controller.resources.schema;

import org.dswarm.controller.resources.BasicDMPResource;
import org.dswarm.controller.resources.schema.utils.AttributePathInstancesResourceUtils;
import org.dswarm.controller.status.DMPStatus;
import org.dswarm.persistence.model.schema.AttributePathInstance;
import org.dswarm.persistence.model.schema.proxy.ProxyAttributePathInstance;
import org.dswarm.persistence.service.schema.AttributePathInstanceService;

/**
 * A generic resource (controller service) for {@link AttributePathInstance}s.
 * 
 * @author tgaengler
 * @param <POJOCLASSRESOURCEUTILS>
 * @param <POJOCLASSPERSISTENCESERVICE> the concrete {@link AttributePathInstance} persistence service of the resource that is
 *            related to the concrete {@link AttributePathInstance} class
 * @param <PROXYPOJOCLASS>
 * @param <POJOCLASS> the concrete {@link AttributePathInstance} class
 */
public abstract class AttributePathInstancesResource<POJOCLASSRESOURCEUTILS extends AttributePathInstancesResourceUtils<POJOCLASSPERSISTENCESERVICE, PROXYPOJOCLASS, POJOCLASS>, POJOCLASSPERSISTENCESERVICE extends AttributePathInstanceService<PROXYPOJOCLASS, POJOCLASS>, PROXYPOJOCLASS extends ProxyAttributePathInstance<POJOCLASS>, POJOCLASS extends AttributePathInstance>
		extends BasicDMPResource<POJOCLASSRESOURCEUTILS, POJOCLASSPERSISTENCESERVICE, PROXYPOJOCLASS, POJOCLASS> {

	/**
	 * Creates a new resource (controller service) for the given concrete {@link AttributePathInstance} class with the provider of
	 * the concrete {@link AttributePathInstance} persistence service, the object mapper and metrics registry.
	 * 
	 * @param pojoClassResourceUtilsArg
	 * @param dmpStatusArg
	 */
	public AttributePathInstancesResource(final POJOCLASSRESOURCEUTILS pojoClassResourceUtilsArg, final DMPStatus dmpStatusArg) {

		super(pojoClassResourceUtilsArg, dmpStatusArg);
	}

	/**
	 * {@inheritDoc}<br/>
	 */
	@Override
	protected POJOCLASS prepareObjectForUpdate(final POJOCLASS objectFromJSON, final POJOCLASS object) {

		super.prepareObjectForUpdate(objectFromJSON, object);

		object.setAttributePath(objectFromJSON.getAttributePath());

		return object;
	}
}
