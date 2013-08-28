package de.avgl.dmp.persistence.model;

import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Transformation extends DMPObject {

	private Set<Component>	components;

	private Set<Connection>	connection;

	public Set<Connection> getConnection() {
		return connection;
	}

	public void setConnection(Set<Connection> connection) {
		this.connection = connection;
	}

	public Set<Component> getComponents() {

		return components;
	}

	public void setComponents(final Set<Component> components) {

		this.components = components;
	}
}
