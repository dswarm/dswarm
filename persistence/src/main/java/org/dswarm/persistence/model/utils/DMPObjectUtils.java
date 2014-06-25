package org.dswarm.persistence.model.utils;

import java.util.Collection;

import org.dswarm.persistence.model.DMPObject;

public class DMPObjectUtils<POJOCLASS extends DMPObject<POJOCLASSIDTYPE>, POJOCLASSIDTYPE> {

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
