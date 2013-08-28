package de.avgl.dmp.persistence.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Connection extends DMPObject {

	private ConnectionType	type;

	private Component		source;

	private Component		target;

	public ConnectionType getType() {

		return type;
	}

	public void setType(final ConnectionType type) {

		this.type = type;
	}

	public Component getSource() {

		return source;
	}

	public void setSource(final Component source) {

		this.source = source;
	}

	public Component getTarget() {

		return target;
	}

	public void setTarget(final Component target) {

		this.target = target;
	}
}
