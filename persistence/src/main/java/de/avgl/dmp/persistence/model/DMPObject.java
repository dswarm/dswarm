package de.avgl.dmp.persistence.model;

import java.io.Serializable;

import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.google.common.base.Objects;

@XmlRootElement
@MappedSuperclass
public abstract class DMPObject<IDTYPE> implements Serializable {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;

	public abstract IDTYPE getId();

	@Override
	public int hashCode() {

		return Objects.hashCode(getId());
	}

	@Override
	public boolean equals(final Object obj) {

		if (!DMPObject.class.isInstance(obj)) {

			return false;
		}

		return Objects.equal(((DMPObject<?>) obj).getId(), getId());
	}

	@Override
	public String toString() {

		return ToStringBuilder.reflectionToString(this);
	}
}
