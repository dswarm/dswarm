package de.avgl.dmp.persistence.model;

import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Transformation extends DMPObject {

	private Set<Component>	components;

	private Set<Connection>	connections;

	public Set<Connection> getConnection() {
		return connections;
	}

	public void setConnections(Set<Connection> connections) {
		this.connections = connections;
	}

	public Set<Component> getComponents() {

		return components;
	}

	public void setComponents(final Set<Component> components) {

		this.components = components;
	}
}
