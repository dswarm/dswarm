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
package org.dswarm.persistence.service;

/**
 * The persistence type enum. A persistence type indicates the persistence method that should be utilised when creating entities via a persistence service {@link BasicJPAService}.<br>
 *
 * @author tgaengler (created), Mar 18, 2013
 * @author $Author$ (last changed)
 * @version $Rev$, $Date$<br>
 *          $Id: $
 */

public enum PersistenceType {

	/**
	 * The persistence type to indicate that the entity should be written via {@link javax.persistence.EntityManager#persist(Object)}.
	 */
	Persist("PERSIST"),

	/**
	 * The persistence type to indicate that the entity should be written via {@link javax.persistence.EntityManager#merge(Object)}.
	 */
	Merge("MERGE");

	/**
	 * The name of the persistence type.
	 */
	private final String name;

	/**
	 * Gets the name of the persistence type.
	 *
	 * @return the name of the persistence type
	 */
	public String getName() {

		return name;
	}

	/**
	 * Creates a new persistence type with the given name.
	 *
	 * @param nameArg the name of the persistence type.
	 */
	private PersistenceType(final String nameArg) {

		name = nameArg;
	}

	/**
	 * Gets the persistence type by the given name, e.g. 'PERSIST' or 'MERGE'.<br>
	 * Created by: ydeng
	 *
	 * @param name the name of the persistence type
	 * @return the appropriated persistence type
	 */
	public static PersistenceType getByName(final String name) {

		for (final PersistenceType persistenceType : PersistenceType.values()) {

			if (persistenceType.name.equals(name)) {

				return persistenceType;
			}
		}

		throw new IllegalArgumentException(name);
	}

	/**
	 * {@inheritDoc}<br>
	 * Returns the name of the persistence type.
	 *
	 * @see Enum#toString()
	 */
	@Override
	public String toString() {

		return name;
	}
}
