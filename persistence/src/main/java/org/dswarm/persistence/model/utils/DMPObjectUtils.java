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
package org.dswarm.persistence.model.utils;

import java.util.Collection;

import org.dswarm.persistence.model.DMPObject;

public class DMPObjectUtils<POJOCLASS extends DMPObject> {

	public boolean completeEquals(final Collection<POJOCLASS> existingObjects, final Collection<POJOCLASS> newObjects) {

		if (existingObjects == null && newObjects == null) {

			return true;
		}

		if (newObjects == null) {

			return false;
		}

		if (existingObjects == null) {

			return false;
		}

		final boolean equalsResult = existingObjects.equals(newObjects);

		if (!equalsResult) {

			return false;
		}

		for (final POJOCLASS existingObject : existingObjects) {

			if (!newObjects.contains(existingObject)) {

				return false;
			}

			POJOCLASS newMatchedObject = null;

			for (final POJOCLASS newObject : newObjects) {

				if (existingObject.equals(newObject)) {

					newMatchedObject = newObject;

					break;
				}
			}

			if (newMatchedObject == null) {

				return false;
			}

			final boolean result = completeEquals(existingObject, newMatchedObject);

			if (!result) {

				return false;
			}
		}

		return true;
	}

	public boolean completeEquals(final POJOCLASS existingObject, final POJOCLASS newObject) {

		if (existingObject == null && newObject == null) {

			return true;
		}

		if (newObject == null) {

			return false;
		}

		if (existingObject == null) {

			return false;
		}

		final boolean equalsResult = existingObject.equals(newObject);

		return equalsResult && existingObject.completeEquals(newObject);

	}

}
