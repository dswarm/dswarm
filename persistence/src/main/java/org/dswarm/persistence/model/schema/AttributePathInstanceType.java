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
package org.dswarm.persistence.model.schema;

/**
 * The attribute path instance type enum. An attribute path instance type indicates the attribute path instance type of an
 * attribute path instance.<br>
 * 
 * @author tgaengler (created), Mar 18, 2013 // TODO : ?
 * @author $Author$ (last changed)
 * @version $Rev$, $Date$<br>
 *          $Id: $
 */

public enum AttributePathInstanceType {

	/**
	 * The attribute path instance type to indicate mapping attribute path instances ({@link MappingAttributePathInstance}).
	 */
	MappingAttributePathInstance("MappingAttributePathInstance"),
	SchemaAttributePathInstance("SchemaAttributePathInstance");

	/**
	 * The name of the attribute path instance type.
	 */
	private final String	name;

	/**
	 * Gets the name of the attribute path instance type.
	 * 
	 * @return the name of the attribute path instance type
	 */
	public String getName() {

		return name;
	}

	/**
	 * Creates a new attribute path instance type with the given name.
	 * 
	 * @param nameArg the name of the attribute path instance type.
	 */
	private AttributePathInstanceType(final String nameArg) {

		name = nameArg;
	}

	/**
	 * Gets the attribute path instance type by the given name, e.g. 'Mapping Attribute Path Instance'.<br>
	 * Created by: ydeng // TODO : ?
	 * 
	 * @param name the name of the attribute path instance type
	 * @return the appropriated attribute path instance type
	 */
	public static AttributePathInstanceType getByName(final String name) {

		for (final AttributePathInstanceType attributePathInstanceType : AttributePathInstanceType.values()) {

			if (attributePathInstanceType.name.equals(name)) {

				return attributePathInstanceType;
			}
		}

		throw new IllegalArgumentException(name);
	}

	/**
	 * {@inheritDoc}<br>
	 * Returns the name of the attribute path instance type.
	 * 
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {

		return name;
	}
}
