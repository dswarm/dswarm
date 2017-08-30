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
package org.dswarm.persistence.model.proxy;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Objects;
import org.apache.commons.lang3.builder.ToStringBuilder;

import org.dswarm.persistence.model.DMPObject;

/**
 * The most abstract proxy POJO class, i.e., this class is intended for inheritance. It only provides a getter for the identifier
 * of the real object and basic #hashCode and #equals implementations (by identifier).<br/>
 * Note: these proxy object should only be utilised when information is retrieved via persistence services
 *
 * @param <POJOCLASS> the proxied object class
 * @author tgaengler
 */
@XmlRootElement
public abstract class ProxyDMPObject<POJOCLASS extends DMPObject> implements Serializable {

	protected final POJOCLASS dmpObject;

	private final RetrievalType type;

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor for handing over a freshly created object, i.e., no updated or already existing object.
	 *
	 * @param dmpObjectArg a freshly created object
	 */
	public ProxyDMPObject(final POJOCLASS dmpObjectArg) {

		dmpObject = dmpObjectArg;
		type = RetrievalType.CREATED;
	}

	/**
	 * Creates a new proxy with the given real object and the type how the object was processed by the persistence service, e.g.,
	 * {@link RetrievalType.CREATED}.
	 *
	 * @param dmpObjectArg an object that was processed by a persistence service
	 * @param typeArg      the type how this object was processed by the persistence service
	 */
	public ProxyDMPObject(final POJOCLASS dmpObjectArg, final RetrievalType typeArg) {

		dmpObject = dmpObjectArg;
		type = typeArg;
	}

	/**
	 * Gets the real object that was proxied by this object.
	 *
	 * @return the real object that was proxied by this object
	 */
	public final POJOCLASS getObject() {

		return dmpObject;
	}

	/**
	 * Gets the type how the object was processed by the persistence service, e.g., {@link RetrievalType.CREATED}.
	 *
	 * @return the type how the object was processed by the persistence service
	 */
	public RetrievalType getType() {

		return type;
	}

	/**
	 * Gets the identifier of the proxied object.
	 *
	 * @return the identifier of the proxied object as the implemented identifier type of the real object
	 */
	public String getId() {

		if (dmpObject == null) {

			return null;
		}

		return dmpObject.getUuid();
	}

	@Override
	public int hashCode() {

		if (dmpObject == null) {

			// TODO: [@tgaengler] is this correct/ok?

			return 0;
		}

		return Objects.hashCode(dmpObject.getUuid());
	}

	@Override
	public boolean equals(final Object obj) {

		return ProxyDMPObject.class.isInstance(obj) && Objects.equal(((ProxyDMPObject<?>) obj).getId(), getId());

	}

	@Override
	public String toString() {

		return ToStringBuilder.reflectionToString(this);
	}
}
