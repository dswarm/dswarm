package org.dswarm.persistence.model.types;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;

/**
 *
 */
@XmlRootElement
public class Tuple<V1, V2> {

	public static <V1, V2> Tuple<V1, V2> tuple(final V1 v1, final V2 v2) {
		return new Tuple<>(v1, v2);
	}

	@JsonProperty("v1")
	private final V1	v1;

	@JsonProperty("v2")
	private final V2	v2;

	@JsonCreator
	public Tuple(@JsonProperty("v1") final V1 v1, @JsonProperty("v2") final V2 v2) {
		this.v1 = Preconditions.checkNotNull(v1);
		this.v2 = Preconditions.checkNotNull(v2);
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
