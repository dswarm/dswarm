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
package org.dswarm.persistence.model.resource;

import java.util.Locale;

import javax.ws.rs.BadRequestException;

@SuppressWarnings("UnusedDeclaration")
public enum UpdateFormat {
	FULL, DELTA;

	// Required for Jersey, so that we can use 'full' instead of 'FULL'
	public static UpdateFormat fromString(final String name) {
		try {
			return valueOf(name.toUpperCase(Locale.ENGLISH));
		} catch (final IllegalArgumentException iae) {
			throw new BadRequestException(
					String.format("UpdateFormat must be one of {full|delta}, but got [%s] instead", name),
					iae);
		}
	}
}
