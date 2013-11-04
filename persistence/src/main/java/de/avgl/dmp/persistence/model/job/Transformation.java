package de.avgl.dmp.persistence.model.job;

import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Transformation extends DMPObject {

	private List<Component> components;
	private EndpointComponent source;
	private EndpointComponent target;

	public List<Component> getComponents() {

		return components;
	}

	public void setComponents(final List<Component> components) {

		this.components = components;
	}

	public void setSource(final EndpointComponent source) {
		this.source = source;
	}

	public EndpointComponent getSource() {
		return source;
	}

	public void setTarget(final EndpointComponent target) {
		this.target = target;
	}

	public EndpointComponent getTarget() {
		return target;
	}
}
