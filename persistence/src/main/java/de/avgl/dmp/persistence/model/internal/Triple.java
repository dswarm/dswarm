package de.avgl.dmp.persistence.model.internal;

/**
 *
 */
public class Triple<V1, V2, V3> {

	public static <V1, V2, V3> Triple<V1, V2, V3> triple(V1 v1, V2 v2, V3 v3) {
		return new Triple<V1, V2, V3>(v1, v2, v3);
	}

	private final V1 v1;
	private final V2 v2;
	private final V3 v3;

	public Triple(V1 v1, V2 v2, V3 v3) {
		this.v1 = v1;
		this.v2 = v2;
		this.v3 = v3;
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
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Triple triple = (Triple) o;

		if (v1 != null ? !v1.equals(triple.v1) : triple.v1 != null) return false;
		if (v2 != null ? !v2.equals(triple.v2) : triple.v2 != null) return false;
		if (v3 != null ? !v3.equals(triple.v3) : triple.v3 != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = v1 != null ? v1.hashCode() : 0;
		result = 31 * result + (v2 != null ? v2.hashCode() : 0);
		result = 17 * result + (v3 != null ? v3.hashCode() : 0);
		return result;
	}
}
