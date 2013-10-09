package de.avgl.dmp.persistence.model.internal;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 *
 */
public class Triple<V1, V2, V3> {

	public static <V1, V2, V3> Triple<V1, V2, V3> triple(final V1 v1, final V2 v2, final V3 v3) {
		return new Triple<V1, V2, V3>(v1, v2, v3);
	}

	private final V1 v1;
	private final V2 v2;
	private final V3 v3;

	public Triple(final V1 v1, final V2 v2, final V3 v3) {
		this.v1 = checkNotNull(v1);
		this.v2 = checkNotNull(v2);
		this.v3 = checkNotNull(v3);
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
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		final Triple triple = (Triple) o;

		if (!v1.equals(triple.v1)) {
			return false;
		}
		if (!v2.equals(triple.v2)) {
			return false;
		}
		if (!v3.equals(triple.v3)) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = v1.hashCode();
		result = 31 * result + v2.hashCode();
		result = 17 * result + v3.hashCode();
		return result;
	}
}
