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
package org.dswarm.persistence.model.job;

import org.dswarm.persistence.DMPPersistenceException;

public enum ConnectionType {

	DEFAULT("default");

	/**
	 * The name of the connection type as it can occur in a transformation.
	 */
	private final String	ctName;

	/**
	 * Creates a new connection with the given name.
	 * 
	 * @param name the name of the connection type
	 */
	private ConnectionType(final String name) {

		ctName = name;
	}

	/**
	 * Gets the name of the connection type.<br>
	 * Created by: tgaengler
	 * 
	 * @return the name of the connection type
	 */
	String getName() {

		return ctName;
	}

	/**
	 * Tries to get a connection type by its name.<br>
	 * Created by: tgaengler
	 * 
	 * @param name the name of the connection type
	 * @return the connection type that matches the given name
	 * @throws DMPPersistenceException if the connection type by the given name is not part of this enumeration, i.e., this
	 *             connection model is probably not implement yet.
	 */
	public static ConnectionType getConnectionTypeByName(final String name) throws DMPPersistenceException {

		if (name == null) {

			throw new DMPPersistenceException("connection name shouldn't be null");
		}

		for (final ConnectionType connectionType : ConnectionType.values()) {

			if (connectionType.getName().equals(name)) {

				return connectionType;
			}
		}

		throw new DMPPersistenceException("couldn't determine connection type for connection name = " + name);
	}

}
