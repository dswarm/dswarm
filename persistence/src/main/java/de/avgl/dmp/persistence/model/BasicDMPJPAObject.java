package de.avgl.dmp.persistence.model;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@MappedSuperclass
public abstract class BasicDMPJPAObject extends DMPJPAObject {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;

	@Column(name = "NAME")
	private String	name;

	public String getName() {

		return name;
	}

	public void setName(final String name) {

		this.name = name;
	}
}
