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
package org.dswarm.persistence.model.schema.utils;

import java.util.Collection;

import org.dswarm.init.util.DMPStatics;
import org.dswarm.persistence.model.schema.Attribute;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.utils.DMPObjectUtils;

public final class AttributePathUtils extends DMPObjectUtils<AttributePath> {

	public static String generateAttributePath(final Collection<Attribute> attributePath) {

		if (null == attributePath) {

			return null;
		}

		if (attributePath.isEmpty()) {

			return null;
		}

		final StringBuilder sb = new StringBuilder();

		boolean first = true;

		for (final Attribute attribute : attributePath) {

			if (!first) {

				sb.append(DMPStatics.ATTRIBUTE_DELIMITER);
			} else {

				first = false;
			}

			sb.append(attribute.getUri());
		}

		return sb.toString();
	}
}
