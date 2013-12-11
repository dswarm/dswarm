package de.avgl.dmp.persistence.model.job;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import de.avgl.dmp.persistence.model.BasicDMPObject;

@XmlRootElement
public class Connection extends BasicDMPObject {

	/**
	 *
	 */
	private static final long	serialVersionUID	= 1L;

	private ConnectionType	type;

	@XmlTransient
	private Component		source;

	@XmlTransient
	private Component		target;

	public Connection(final String id) {

		super(id);
	}

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
	public Long getSourceId() {

		if (this.source == null) {

			return null;
		}

		return this.source.getId();
	}

	@XmlElement(name = "target")
	public Long getTargetId() {

		if (this.target == null) {

			return null;
		}

		return this.target.getId();
	}
}
