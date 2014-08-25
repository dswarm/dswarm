package org.dswarm.controller.resources.schema.test;

import java.util.Iterator;

import com.google.common.collect.AbstractIterator;

import org.dswarm.persistence.model.schema.Attribute;
import org.dswarm.persistence.model.schema.AttributePath;

final class PathHelpers {
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

	static PathHelper newActual(final String uri) {
		return new PathHelper.Actual(uri);
	}

	static PathHelper newExpected(final String uri) {
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
