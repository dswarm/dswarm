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
package org.dswarm.controller.resources.schema.test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.dswarm.persistence.model.schema.Attribute;
import org.dswarm.persistence.model.schema.AttributePath;

interface PathHelper {

	public Set<AttributePath> attributePaths();

	public void path(final String... a);

	public void make(final String name);

	static class Actual implements PathHelper {

		private final String	uri;

		Actual(final String uri) {
			this.uri = uri;
		}

		private final Map<String, Attribute> attributes		= Maps.newHashMap();
		private final Set<AttributePath>		attributePaths	= Sets.newHashSet();

		public void path(final String... a) {
			final List<Attribute> list = Lists.newLinkedList();
			for (final String s : a) {
				final Attribute e = Preconditions.checkNotNull(attributes.get(s), "%s does not have an attribute defined", s);
				list.add(e);
			}
			attributePaths.add(new AttributePath(list));
		}

		public void make(final String name) {
			attributes.put(name, new Attribute(uri + "#" + name, name));
		}

		@Override
		public Set<AttributePath> attributePaths() {
			return attributePaths;
		}
	}

	static class Expected implements PathHelper {

		private final PathHelper pathHelper;

		Expected(final PathHelper pathHelper) {
			this.pathHelper = pathHelper;
		}

		private final Set<String>				attributePathHashes = Sets.newHashSet();

		public void path(final String... a) {
			for (int i = 1; i < a.length; i++) {
				add(withType(i, a));
				add(sliced(a, i));
			}
			add(withType(a));
			add(withValue(a));
			add(a);
		}

		public void force(final String... a) {
			pathHelper.path(a);
		}

		@Override
		public Set<AttributePath> attributePaths() {
			return pathHelper.attributePaths();
		}

		@Override
		public void make(final String name) {
			pathHelper.make(name);
		}

		private void add(final String... a) {
			final String hash = Joiner.on("").join(a);
			if (!attributePathHashes.contains(hash)) {
				attributePathHashes.add(hash);
				pathHelper.path(a);
			}
		}

		private static String[] withValue(final String... original) {
			return withValue(original.length, original);
		}

		private static String[] withValue(final int slice, final String... original) {
			return withAdditional("value", slice, original);
		}

		private static String[] withType(final String... original) {
			return withType(original.length, original);
		}

		private static String[] withType(final int slice, final String... original) {
			return withAdditional("type", slice, original);
		}

		private static String[] withAdditional(final String part, final int slice, final String... original) {
			final String[] paths = sliced(original, slice, 1);
			paths[slice] = part;
			return paths;
		}

		private static String[] sliced(final String[] array, final int slice) {
			return sliced(array, slice, 0);
		}

		private static String[] sliced(final String[] array, final int slice, final int pad) {
			final String[] newArray = new String[slice + pad];
			System.arraycopy(array, 0, newArray, 0, slice);
			return newArray;
		}
	}
}
