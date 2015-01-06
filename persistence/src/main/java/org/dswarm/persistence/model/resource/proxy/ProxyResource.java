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
package org.dswarm.persistence.model.resource.proxy;

import javax.xml.bind.annotation.XmlRootElement;

import org.dswarm.persistence.model.proxy.ProxyExtendedBasicDMPJPAObject;
import org.dswarm.persistence.model.proxy.RetrievalType;
import org.dswarm.persistence.model.resource.Resource;

/**
 * A proxy class for {@link Resource}s.
 * 
 * @author tgaengler
 */
@XmlRootElement
public class ProxyResource extends ProxyExtendedBasicDMPJPAObject<Resource> {

	/**
	 *
	 */
	private static final long	serialVersionUID	= 1L;

	/**
	 * Default constructor for handing over a freshly created data resource, i.e., no updated or already existing data resource.
	 * 
	 * @param resourceArg a freshly created data resource
	 */
	public ProxyResource(final Resource resourceArg) {

		super(resourceArg);
	}

	/**
	 * Creates a new proxy with the given real data resource and the type how the data resource was processed by the data resource
	 * persistence service, e.g., {@link RetrievalType.CREATED}.
	 * 
	 * @param resourceArg a data resource that was processed by the data resource persistence service
	 * @param typeArg the type how this data resource was processed by the data resource persistence service
	 */
	public ProxyResource(final Resource resourceArg, final RetrievalType typeArg) {

		super(resourceArg, typeArg);
	}
}
