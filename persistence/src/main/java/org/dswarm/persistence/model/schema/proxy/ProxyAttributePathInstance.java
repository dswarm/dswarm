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
package org.dswarm.persistence.model.schema.proxy;

import javax.xml.bind.annotation.XmlRootElement;

import org.dswarm.persistence.model.proxy.ProxyBasicDMPJPAObject;
import org.dswarm.persistence.model.proxy.RetrievalType;
import org.dswarm.persistence.model.schema.AttributePathInstance;

/**
 * An abstract proxy class for {@link AttributePathInstance}s.
 * 
 * @author tgaengler
 */
@XmlRootElement
public abstract class ProxyAttributePathInstance<POJOCLASS extends AttributePathInstance> extends ProxyBasicDMPJPAObject<POJOCLASS> {

	/**
	 *
	 */
	private static final long	serialVersionUID	= 1L;

	/**
	 * Default constructor for handing over a freshly created attribute path instance, i.e., no updated or already existing
	 * attribute path instance.
	 * 
	 * @param attributePathInstanceArg a freshly created attribute path instance
	 */
	public ProxyAttributePathInstance(final POJOCLASS attributePathInstanceArg) {

		super(attributePathInstanceArg);
	}

	/**
	 * Creates a new proxy with the given real attribute path instance and the type how the attribute path instance was processed
	 * by the attribute path instance persistence service, e.g., {@link RetrievalType.CREATED}.
	 * 
	 * @param attributePathInstanceArg a attribute path instance that was processed by the attribute path instance persistence
	 *            service
	 * @param typeArg the type how this attribute path instance was processed by the attribute path instance persistence service
	 */
	public ProxyAttributePathInstance(final POJOCLASS attributePathInstanceArg, final RetrievalType typeArg) {

		super(attributePathInstanceArg, typeArg);
	}
}
