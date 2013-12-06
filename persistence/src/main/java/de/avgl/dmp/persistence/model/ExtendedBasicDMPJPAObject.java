package de.avgl.dmp.persistence.model;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@MappedSuperclass
public abstract class ExtendedBasicDMPJPAObject extends BasicDMPJPAObject {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;

	@Column(name = "DESCRIPTION", columnDefinition = "VARCHAR(4000)", length = 4000)
	private String				description			= null;

	public String getDescription() {

		return description;
	}

	public void setDescription(final String description) {

		this.description = description;
	}
}
