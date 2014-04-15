package de.avgl.dmp.persistence.model.types;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 *
 */
public class Tuple<V1, V2> {

	public static <V1, V2> Tuple<V1, V2> tuple(final V1 v1, final V2 v2) {
		return new Tuple<>(v1, v2);
	}

	private final V1 v1;
	private final V2 v2;

	public Tuple(final V1 v1, final V2 v2) {
		this.v1 = checkNotNull(v1);
		this.v2 = checkNotNull(v2);
	}

	public V1 v1() {
		return v1;
	}

	public V2 v2() {
		return v2;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		final Tuple tuple = (Tuple) o;

		return v1.equals(tuple.v1) && v2.equals(tuple.v2);

	}

	@Override
	public int hashCode() {
		int result = v1.hashCode();
		result = 31 * result + v2.hashCode();
		return result;
	}
}
