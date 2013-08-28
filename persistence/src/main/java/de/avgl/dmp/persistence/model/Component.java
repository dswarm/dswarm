package de.avgl.dmp.persistence.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Component extends DMPObject {

	private ComponentType	type;

	public ComponentType getType() {

		return type;
	}

	public void setType(final ComponentType type) {

		this.type = type;
	}
}
