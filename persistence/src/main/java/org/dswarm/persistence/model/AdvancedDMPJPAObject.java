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
package org.dswarm.persistence.model;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Objects;

/**
 * The abstract POJO class for entities where the uri of the entity should be provided at object creation.
 *
 * @author tgaengler
 */
@XmlRootElement
@MappedSuperclass
@Cacheable(false)
public abstract class AdvancedDMPJPAObject extends BasicDMPJPAObject {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	// @Id
	@Access(AccessType.FIELD)
	@Column(name = "URI", columnDefinition = "VARCHAR(255)", length = 255, unique = true)
	private final String uri;

	protected AdvancedDMPJPAObject() {

		uri = null;
	}

	protected AdvancedDMPJPAObject(final String uuid) {

		super(uuid);
		uri = null;
	}

	protected AdvancedDMPJPAObject(final String uuid, final String uriArg) {

		super(uuid);
		uri = uriArg;
	}

	protected AdvancedDMPJPAObject(final String uuid, final String uriArg, final String name) {

		super(uuid);
		uri = uriArg;
		setName(name);
	}

	public String getUri() {

		return uri;
	}

	@Override
	public boolean completeEquals(final Object obj) {

		return AdvancedDMPJPAObject.class.isInstance(obj) && super.completeEquals(obj)
				&& Objects.equal(((AdvancedDMPJPAObject) obj).getUri(), getUri());
	}
}
