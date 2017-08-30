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
package org.dswarm.persistence.model.job;

/**
 * The function type enum. A function type indicates the function type of a function. Currently, the function types are mapped to
 * specialised function implementations, i.e., {@link FunctionType#Function} to {@link Function} and
 * {@link FunctionType#Transformation} to {@link Transformation}.<br>
 * 
 * @author tgaengler (created), Mar 18, 2013
 * @author $Author$ (last changed)
 * @version $Rev$, $Date$<br>
 *          $Id: $
 */

public enum FunctionType {

	/**
	 * The filter type to indicate functions ({@link Function}).
	 */
	Function("FUNCTION"),

	/**
	 * The filter type to indicate transformations ({@link Transformation}).
	 */
	Transformation("TRANSFORMATION");

	/**
	 * The name of the function type.
	 */
	private final String	name;

	/**
	 * Gets the name of the function type.
	 * 
	 * @return the name of the function type
	 */
	public String getName() {

		return name;
	}

	/**
	 * Creates a new function type with the given name.
	 * 
	 * @param nameArg the name of the function type.
	 */
	private FunctionType(final String nameArg) {

		name = nameArg;
	}

	/**
	 * Gets the function type by the given name, e.g. 'FUNCTION' or 'TRANSFORMATION'.<br>
	 * Created by: ydeng
	 * 
	 * @param name the name of the function type
	 * @return the appropriated function type
	 */
	public static FunctionType getByName(final String name) {

		for (final FunctionType functionType : FunctionType.values()) {

			if (functionType.name.equals(name)) {

				return functionType;
			}
		}

		throw new IllegalArgumentException(name);
	}

	/**
	 * {@inheritDoc}<br>
	 * Returns the name of the function type.
	 * 
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {

		return name;
	}
}
