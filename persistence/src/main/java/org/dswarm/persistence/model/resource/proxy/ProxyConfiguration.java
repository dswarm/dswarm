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
package org.dswarm.persistence.model.resource.proxy;

import javax.xml.bind.annotation.XmlRootElement;

import org.dswarm.persistence.model.proxy.ProxyExtendedBasicDMPJPAObject;
import org.dswarm.persistence.model.proxy.RetrievalType;
import org.dswarm.persistence.model.resource.Configuration;

/**
 * A proxy class for {@link Configuration}s.
 * 
 * @author tgaengler
 */
@XmlRootElement
public class ProxyConfiguration extends ProxyExtendedBasicDMPJPAObject<Configuration> {

	/**
	 *
	 */
	private static final long	serialVersionUID	= 1L;

	/**
	 * Default constructor for handing over a freshly created configuration, i.e., no updated or already existing configuration.
	 * 
	 * @param configurationArg a freshly created configuration
	 */
	public ProxyConfiguration(final Configuration configurationArg) {

		super(configurationArg);
	}

	/**
	 * Creates a new proxy with the given real configuration and the type how the configuration was processed by the configuration
	 * persistence service, e.g., {@link RetrievalType.CREATED}.
	 * 
	 * @param configurationArg a configuration that was processed by the configuration persistence service
	 * @param typeArg the type how this configuration was processed by the configuration persistence service
	 */
	public ProxyConfiguration(final Configuration configurationArg, final RetrievalType typeArg) {

		super(configurationArg, typeArg);
	}
}
