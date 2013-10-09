package de.avgl.dmp.persistence.model.job;

import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Transformation extends DMPObject {

	private List<Component> components;
	private Component source;
	private Component target;

	public List<Component> getComponents() {

		return components;
	}

	public void setComponents(final List<Component> components) {

		this.components = components;
	}

	public void setSource(final Component source) {
		this.source = source;
	}

	public Component getSource() {
		return source;
	}

	public void setTarget(final Component target) {
		this.target = target;
	}

	public Component getTarget() {
		return target;
	}
}
