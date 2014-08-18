package org.dswarm.persistence.model;

import java.io.Serializable;

import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Objects;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * The most abstract POJO class, i.e., this class is intended for inheritance. It only provides a getter for the identifier and
 * basic #hashCode and #equals implementations (by identifier).
 *
 * @author tgaengler
 * @param <IDTYPE> the identifier type of the object
 */
@XmlRootElement
@MappedSuperclass
public abstract class DMPObject<IDTYPE> implements Serializable {

	/**
	 *
	 */
	private static final long	serialVersionUID	= 1L;

	/**
	 * Gets the identifier of this object.
	 *
	 * @return the identifier of this object as the implemented identifier type
	 */
	public abstract IDTYPE getId();

	@Override
	public int hashCode() {

		return Objects.hashCode(getId());
	}

	@Override
	public boolean equals(final Object obj) {
//		return DMPObject.class.isInstance(obj) && Objects.equal(((DMPObject<?>) obj).getId(), getId());

		if (this == obj) {
			return true;
		}
		if (obj == null || !(obj instanceof DMPObject)) {
			return false;
		}

		final DMPObject<?> other = (DMPObject<?>) obj;

		if (this.getId() == null && other.getId() == null) {
			return false;
		}

		return Objects.equal(other.getId(), getId());
	}

	public boolean completeEquals(final Object obj) {

		return DMPObject.class.isInstance(obj) && Objects.equal(((DMPObject<?>) obj).getId(), getId());
	}

	@Override
	public String toString() {

		return ToStringBuilder.reflectionToString(this);
	}
}
