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

import org.dswarm.persistence.model.proxy.ProxyAdvancedDMPJPAObject;
import org.dswarm.persistence.model.proxy.RetrievalType;
import org.dswarm.persistence.model.schema.Attribute;

/**
 * A proxy class for {@link Attribute}s.
 * 
 * @author tgaengler
 */
@XmlRootElement
public class ProxyAttribute extends ProxyAdvancedDMPJPAObject<Attribute> {

	/**
	 *
	 */
	private static final long	serialVersionUID	= 1L;

	/**
	 * Default constructor for handing over a freshly created attribute, i.e., no updated or already existing attribute.
	 * 
	 * @param attributeArg a freshly created attribute
	 */
	public ProxyAttribute(final Attribute attributeArg) {

		super(attributeArg);
	}

	/**
	 * Creates a new proxy with the given real attribute and the type how the attribute was processed by the attribute persistence
	 * service, e.g., {@link RetrievalType.CREATED}.
	 * 
	 * @param attributeArg an attribute that was processed by the attribute persistence service
	 * @param typeArg the type how this attribute was processed by the attribute persistence service
	 */
	public ProxyAttribute(final Attribute attributeArg, final RetrievalType typeArg) {

		super(attributeArg, typeArg);
	}
}
