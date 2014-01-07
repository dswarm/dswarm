package de.avgl.dmp.persistence.model;

import java.io.Serializable;

import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.google.common.base.Objects;

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

		return DMPObject.class.isInstance(obj) && Objects.equal(((DMPObject<?>) obj).getId(), getId());

	}

	@Override
	public String toString() {

		return ToStringBuilder.reflectionToString(this);
	}
}
