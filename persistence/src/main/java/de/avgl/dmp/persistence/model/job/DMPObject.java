package de.avgl.dmp.persistence.model.job;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.google.common.base.Objects;

@XmlRootElement
public class DMPObject implements Serializable {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;

	private String	id;

	private String	name;

	public String getId() {

		return id;
	}

	public void setId(final String id) {

		this.id = id;
	}

	public String getName() {

		return name;
	}

	public void setName(final String name) {

		this.name = name;
	}

	@Override
	public int hashCode() {

		return Objects.hashCode(getId());
	}

	@Override
	public boolean equals(final Object obj) {

		if (!DMPObject.class.isInstance(obj)) {

			return false;
		}

		return Objects.equal(((DMPObject) obj).getId(), getId());
	}

	@Override
	public String toString() {

		return ToStringBuilder.reflectionToString(this);
	}
}
