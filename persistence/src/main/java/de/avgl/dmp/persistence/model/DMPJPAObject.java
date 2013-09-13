package de.avgl.dmp.persistence.model;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.google.common.base.Objects;

@MappedSuperclass
@Cacheable(true)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public abstract class DMPJPAObject {

	@Id
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
