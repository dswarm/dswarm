package de.avgl.dmp.persistence.model.job;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement
public class Connection extends DMPObject {

	private ConnectionType	type;

	@XmlTransient
	private Component		source;

	@XmlTransient
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

	@XmlElement(name = "source")
	public String getSourceId() {

		if (this.source == null) {

			return null;
		}

		return this.source.getId();
	}

	@XmlElement(name = "target")
	public String getTargetId() {

		if (this.target == null) {

			return null;
		}

		return this.target.getId();
	}
}
