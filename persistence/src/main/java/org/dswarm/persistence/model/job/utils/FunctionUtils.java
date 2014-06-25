package org.dswarm.persistence.model.job.utils;

import org.dswarm.persistence.model.job.Function;
import org.dswarm.persistence.model.job.Transformation;
import org.dswarm.persistence.util.DMPPersistenceUtil;

/**
 * @author tgaengler
 */
public final class FunctionUtils extends BasicFunctionUtils<Function> {

	@Override
	public boolean completeEquals(final Function existingObject, final Function newObject) {

		if (Transformation.class.isInstance(existingObject) && Transformation.class.isInstance(newObject)) {

			return DMPPersistenceUtil.getTransformationUtils().completeEquals((Transformation) existingObject, (Transformation) newObject);
		}

		return super.completeEquals(existingObject, newObject);
	}
}
