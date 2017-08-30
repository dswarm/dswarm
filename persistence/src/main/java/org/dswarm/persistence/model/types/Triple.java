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
package org.dswarm.persistence.model.types;

import com.google.common.base.Preconditions;

/**
 *
 */
public class Triple<V1, V2, V3> {

	public static <V1, V2, V3> Triple<V1, V2, V3> triple(final V1 v1, final V2 v2, final V3 v3) {
		return new Triple<>(v1, v2, v3);
	}

	private final V1	v1;
	private final V2	v2;
	private final V3	v3;

	public Triple(final V1 v1, final V2 v2, final V3 v3) {
		this.v1 = Preconditions.checkNotNull(v1);
		this.v2 = Preconditions.checkNotNull(v2);
		this.v3 = Preconditions.checkNotNull(v3);
	}

	public V1 v1() {
		return v1;
	}

	public V2 v2() {
		return v2;
	}

	public V3 v3() {
		return v3;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		final Triple triple = (Triple) o;

		return v1.equals(triple.v1) && v2.equals(triple.v2) && v3.equals(triple.v3);

	}

	@Override
	public int hashCode() {
		int result = v1.hashCode();
		result = 31 * result + v2.hashCode();
		result = 17 * result + v3.hashCode();
		return result;
	}
}
