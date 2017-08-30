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
package org.dswarm.persistence.model.internal.helper;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ch.lambdaj.Lambda;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import org.hamcrest.Matchers;

import org.dswarm.init.util.DMPStatics;

public class AttributePathHelperHelper {

	public static AttributePathHelper addAttributePath(final JsonNode unnormalizedSchema,
	                                                   final Set<AttributePathHelper> attributePaths,
	                                                   final AttributePathHelper attributePath) {

		final String attribute = unnormalizedSchema.asText();
		final boolean multivalue = false;

		return AttributePathHelperHelper.addAttributePath(attribute, multivalue, attributePaths, attributePath);
	}

	public static AttributePathHelper addAttributePath(final String attributeURI,
	                                                   final Boolean multivalue,
	                                                   final Set<AttributePathHelper> attributePaths,
	                                                   final AttributePathHelper attributePath) {

		final List<String> currentAttributePath = Lists.newArrayList(attributePath.getAttributePath());
		currentAttributePath.add(attributeURI);
		final Boolean required = null;
		final AttributePathHelper schemaNormalizerHelper = new AttributePathHelper(currentAttributePath, required, multivalue);
		attributePaths.add(schemaNormalizerHelper);

		return schemaNormalizerHelper;
	}

	public static AttributePathHelper addAttributePath(final AttributePathHelper childAttributePath,
	                                                   final Set<AttributePathHelper> attributePaths,
	                                                   final AttributePathHelper rootAttributePath) {

		final List<String> currentAttributePath = Lists.newArrayList(rootAttributePath.getAttributePath());
		currentAttributePath.addAll(childAttributePath.getAttributePath());
		final AttributePathHelper schemaNormalizerHelper = new AttributePathHelper(currentAttributePath);
		attributePaths.add(schemaNormalizerHelper);

		return schemaNormalizerHelper;
	}

	public static boolean levelAsArray(final List<AttributePathHelper> attributePaths,
	                                   final String levelCurrentRootAttributePath) {

		boolean levelAsArray = false;

		for (final AttributePathHelper attributePathHelper : attributePaths) {

			if (!attributePathHelper.toString().startsWith(levelCurrentRootAttributePath)) {

				levelAsArray = true;

				break;
			}
		}

		return levelAsArray;
	}

	public static String determineLevelRootAttributePath(final AttributePathHelper attributePathHelper,
	                                                     final int level) {

		String levelCurrentRootAttributePath = "";

		int currentLevel = 0;

		final Iterator<String> iter = attributePathHelper.getAttributePath().iterator();

		while ((level > currentLevel) && iter.hasNext()) {

			if (currentLevel > 0) {

				levelCurrentRootAttributePath += DMPStatics.ATTRIBUTE_DELIMITER;
			}

			levelCurrentRootAttributePath += iter.next();

			currentLevel++;
		}

		if (level == 1) {

			if (levelCurrentRootAttributePath.contains(DMPStatics.ATTRIBUTE_DELIMITER.toString())) {

				levelCurrentRootAttributePath = levelCurrentRootAttributePath.substring(0,
						levelCurrentRootAttributePath.indexOf(DMPStatics.ATTRIBUTE_DELIMITER.toString()));
			}
		}

		return levelCurrentRootAttributePath;
	}

	public static List<AttributePathHelper> prepareAttributePathHelpers(final List<AttributePathHelper> attributePaths,
	                                                                    final int level) {

		// only relevant attribute paths
		final List<AttributePathHelper> filteredAttributePaths = Lambda.filter(
				Lambda.having(Lambda.on(AttributePathHelper.class).length(), Matchers.greaterThanOrEqualTo(level)), attributePaths);

		if (filteredAttributePaths == null || filteredAttributePaths.isEmpty()) {

			return null;
		}

		// sort
		return Lambda.sort(filteredAttributePaths, Lambda.on(AttributePathHelper.class).length());
	}

	public static List<AttributePathHelper> getNextAttributePathHelpersForLevelRootAttributePath(final List<AttributePathHelper> attributePaths,
	                                                                                             final String levelRootAttributePath,
	                                                                                             final int level) {

		// only attribute paths for level root attribute path
		final List<AttributePathHelper> levelRootAttributePaths = Lambda.filter(
				Lambda.having(Lambda.on(AttributePathHelper.class).toString(), Matchers.startsWith(levelRootAttributePath)), attributePaths);

		if (levelRootAttributePaths == null || levelRootAttributePaths.isEmpty()) {

			return null;
		}

		// only level attribute paths for next level
		return AttributePathHelperHelper.prepareAttributePathHelpers(levelRootAttributePaths, level + 1);
	}

	public static boolean hasNextLevel(final List<AttributePathHelper> attributePaths,
	                                   final int level) {

		final List<AttributePathHelper> nextLevelAttributePaths = AttributePathHelperHelper.prepareAttributePathHelpers(attributePaths, level + 1);

		return nextLevelAttributePaths != null && !nextLevelAttributePaths.isEmpty();
	}
}
