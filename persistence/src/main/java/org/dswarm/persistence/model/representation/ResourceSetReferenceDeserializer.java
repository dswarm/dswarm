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
package org.dswarm.persistence.model.representation;

import com.fasterxml.jackson.databind.DeserializationContext;

import org.dswarm.persistence.model.resource.Resource;
import org.dswarm.persistence.model.resource.proxy.ProxyResource;
import org.dswarm.persistence.service.resource.ResourceService;

/**
 * @author tgaengler
 */
public class ResourceSetReferenceDeserializer extends SetReferenceDeserializer<ResourceService, ProxyResource, Resource> {

	@Override protected ResourceService getJpaService(final DeserializationContext deserializationContext) throws IllegalStateException {

		return getJPAService(deserializationContext, ResourceService.class);
	}

	@Override protected Resource getNewObject(final String uuid) {

		return new Resource(uuid);
	}
}
