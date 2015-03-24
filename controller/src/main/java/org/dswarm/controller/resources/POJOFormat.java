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
package org.dswarm.controller.resources;

public enum POJOFormat {
	FULL, MEDIUM, SHORT;

	// Required for Jersey, so that we can use 'full' instead of 'FULL'
	@SuppressWarnings("UnusedDeclaration")
	public static POJOFormat fromString(final String name) {
		switch (name.toLowerCase()) {
			case "short":
				return SHORT;
			case "medium":
				return MEDIUM;
			// case "full":
			// TODO: should we fail for an invalid format instead of defaulting to full?
			default:
				return FULL;
		}
	}
}
