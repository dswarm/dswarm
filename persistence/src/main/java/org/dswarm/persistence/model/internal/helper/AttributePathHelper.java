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
package org.dswarm.persistence.model.internal.helper;

import java.util.LinkedList;

import com.google.common.collect.Lists;

import org.dswarm.init.util.DMPStatics;

public class AttributePathHelper {

	private final LinkedList<String>	attributePath	= Lists.newLinkedList();

	public void addAttribute(final String attribute) {

		attributePath.add(attribute);
	}

	public void setAttributePath(final LinkedList<String> attributePathArg) {

		attributePath.clear();
		attributePath.addAll(attributePathArg);
	}

	public LinkedList<String> getAttributePath() {

		return attributePath;
	}

	public int length() {

		return attributePath.size();
	}

	@Override
	public String toString() {

		final StringBuilder sb = new StringBuilder();

		for (int i = 0; i < attributePath.size(); i++) {

			final String attribute = attributePath.get(i);

			sb.append(attribute);

			if (i < (attributePath.size() - 1)) {

				sb.append(DMPStatics.ATTRIBUTE_DELIMITER);
			}
		}

		return sb.toString();
	}

	@Override
	public int hashCode() {

		return toString().hashCode();
	}

	@Override
	public boolean equals(final java.lang.Object obj) {

		return obj != null && AttributePathHelper.class.isInstance(obj) && toString().equals(obj.toString());
	}
}
