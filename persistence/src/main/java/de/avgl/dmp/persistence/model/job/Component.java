package de.avgl.dmp.persistence.model.job;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Component extends DMPObject {

	private ComponentType	type;

	private Payload			payload;

	public ComponentType getType() {

		return type;
	}

	public void setType(final ComponentType type) {

		this.type = type;
	}

	public Payload getPayload() {
		return payload;
	}

	public void setPayload(Payload payload) {
		this.payload = payload;
	}


}
