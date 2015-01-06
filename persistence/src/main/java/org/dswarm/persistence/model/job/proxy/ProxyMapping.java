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
package org.dswarm.persistence.model.job.proxy;

import javax.xml.bind.annotation.XmlRootElement;

import org.dswarm.persistence.model.job.Mapping;
import org.dswarm.persistence.model.proxy.ProxyBasicDMPJPAObject;
import org.dswarm.persistence.model.proxy.RetrievalType;

/**
 * A proxy class for {@link Mapping}s.
 * 
 * @author tgaengler
 */
@XmlRootElement
public class ProxyMapping extends ProxyBasicDMPJPAObject<Mapping> {

	/**
	 *
	 */
	private static final long	serialVersionUID	= 1L;

	/**
	 * Default constructor for handing over a freshly created mapping, i.e., no updated or already existing mapping.
	 * 
	 * @param mappingArg a freshly created mapping
	 */
	public ProxyMapping(final Mapping mappingArg) {

		super(mappingArg);
	}

	/**
	 * Creates a new proxy with the given real mapping and the type how the mapping was processed by the mapping persistence
	 * service, e.g., {@link RetrievalType.CREATED}.
	 * 
	 * @param mappingArg a mapping that was processed by the mapping persistence service
	 * @param typeArg the type how this mapping was processed by the mapping persistence service
	 */
	public ProxyMapping(final Mapping mappingArg, final RetrievalType typeArg) {

		super(mappingArg, typeArg);
	}
}
