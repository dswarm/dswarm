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
package org.dswarm.controller.resources.schema.test.helper;

import java.util.Iterator;

import com.google.common.collect.AbstractIterator;

import org.dswarm.controller.resources.schema.test.helper.PathHelper;
import org.dswarm.persistence.model.schema.Attribute;
import org.dswarm.persistence.model.schema.AttributePath;

public final class PathHelpers {
	private PathHelpers() {
	}

	public static void force(final PathHelper pathHelper, final String[]... additionals) {
		if (pathHelper instanceof PathHelper.Expected) {
			final PathHelper.Expected helper = (PathHelper.Expected) pathHelper;
			for (final String[] additional : additionals) {
				helper.force(additional);
			}
		} else {
			for (final String[] additional : additionals) {
				pathHelper.path(additional);
			}
		}
	}

	public static PathHelper newActual(final String uri) {
		return new PathHelper.Actual(uri);
	}

	public static PathHelper newExpected(final String uri) {
		return new PathHelper.Expected(newActual(uri));
	}

	static Iterable<String> pathIterable(final PathHelper pathHelper) {
		return new Iterable<String>() {
			@Override
			public Iterator<String> iterator() {
				final Iterator<AttributePath> parent = pathHelper.attributePaths().iterator();
				return new AbstractIterator<String>() {
					@Override
					protected String computeNext() {
						if (!parent.hasNext()) {
							return endOfData();
						}
						final AttributePath attributePath = parent.next();
						final StringBuilder sb = new StringBuilder();
						sb.append("[ ");
						for (final Attribute attribute : attributePath.getAttributePath()) {
							sb.append(attribute.getName()).append(" > ");
						}
						sb.delete(Math.max(0, sb.length() - 3), sb.length()).append(" ]");
						return sb.toString();
					}
				};
			}
		};
	}
}
