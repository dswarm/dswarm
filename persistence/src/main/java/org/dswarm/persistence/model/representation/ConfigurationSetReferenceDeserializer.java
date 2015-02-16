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
package org.dswarm.persistence.model.representation;

import com.fasterxml.jackson.databind.DeserializationContext;

import org.dswarm.persistence.model.resource.Configuration;
import org.dswarm.persistence.model.resource.proxy.ProxyConfiguration;
import org.dswarm.persistence.service.resource.ConfigurationService;

/**
 * @author tgaengler
 */
public class ConfigurationSetReferenceDeserializer extends SetReferenceDeserializer<ConfigurationService, ProxyConfiguration, Configuration> {

	@Override protected ConfigurationService getJpaService(final DeserializationContext deserializationContext) throws IllegalStateException {

		return getJPAService(deserializationContext, ConfigurationService.class);
	}

	@Override protected Configuration getNewObject(final String uuid) {

		return new Configuration(uuid);
	}
}
