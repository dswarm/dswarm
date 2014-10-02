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
package org.dswarm.persistence.model;

import java.io.Serializable;

import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Objects;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * The most abstract POJO class, i.e., this class is intended for inheritance. It only provides a getter for the identifier and
 * basic #hashCode and #equals implementations (by identifier).
 *
 * @author tgaengler
 * @param <IDTYPE> the identifier type of the object
 */
@XmlRootElement
@MappedSuperclass
public abstract class DMPObject<IDTYPE> implements Serializable {

	/**
	 *
	 */
	private static final long	serialVersionUID	= 1L;

	/**
	 * Gets the identifier of this object.
	 *
	 * @return the identifier of this object as the implemented identifier type
	 */
	public abstract IDTYPE getId();

	@Override
	public int hashCode() {

		return Objects.hashCode(getId());
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || !(obj instanceof DMPObject)) {
			return false;
		}

		final DMPObject<?> other = (DMPObject<?>) obj;

		// The ID is `null` if a DMPObject is not initialized/persisted in the DB.
		// If this is the case for both objects, treat them as non equal, since they might be
		// one of many recently `new`-ed objects and their equality cannot be proven at this point.
		// If we introduce UUIDs for every object, this can change so that `null` IDs are treated
		// as equal.
		if (this.getId() == null && other.getId() == null) {
			return false;
		}

		return Objects.equal(other.getId(), getId());
	}

	public boolean completeEquals(final Object obj) {

		return DMPObject.class.isInstance(obj) && Objects.equal(((DMPObject<?>) obj).getId(), getId());
	}

	@Override
	public String toString() {

		return ToStringBuilder.reflectionToString(this);
	}
}
