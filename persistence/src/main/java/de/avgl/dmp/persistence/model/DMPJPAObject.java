package de.avgl.dmp.persistence.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlID;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.google.common.base.Objects;

@MappedSuperclass
// @JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="id")
public abstract class DMPJPAObject implements Serializable {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;
	
	@Id
	@XmlID
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "ID")
	private Long	id;

	public Long getId() {

		return id;
	}

	@Override
	public int hashCode() {

		return Objects.hashCode(getId());
	}

	@Override
	public boolean equals(final Object obj) {

		if (!DMPJPAObject.class.isInstance(obj)) {

			return false;
		}

		return Objects.equal(((DMPJPAObject) obj).getId(), getId());
	}

	@Override
	public String toString() {

		return ToStringBuilder.reflectionToString(this);
	}
}
