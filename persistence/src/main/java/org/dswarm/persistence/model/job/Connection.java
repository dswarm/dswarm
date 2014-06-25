package org.dswarm.persistence.model.job;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.dswarm.persistence.model.AdvancedDMPJPAObject;

@XmlRootElement
public class Connection extends AdvancedDMPJPAObject {

	/**
	 *
	 */
	private static final long	serialVersionUID	= 1L;

	private ConnectionType		type;

	@XmlTransient
	private Component			source;

	@XmlTransient
	private Component			target;

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

		if (source == null) {

			return null;
		}

		return source.getId();
	}

	@XmlElement(name = "target")
	public Long getTargetId() {

		if (target == null) {

			return null;
		}

		return target.getId();
	}
}
