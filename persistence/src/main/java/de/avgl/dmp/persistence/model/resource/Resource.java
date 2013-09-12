package de.avgl.dmp.persistence.model.resource;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import de.avgl.dmp.persistence.model.DMPJPAObject;

@Entity
@Table(name = "RESOURCE")
public class Resource extends DMPJPAObject {
	
	@Column(name = "NAME")
	private String	name = null;
	
	public String getName() {

		return name;
	}

	public void setName(final String name) {

		this.name = name;
	}
}
