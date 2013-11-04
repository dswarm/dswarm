package de.avgl.dmp.persistence.model.job;

public class EndpointComponent extends Component {

	private long resourceId;
	private long configurationId;

	public EndpointComponent(Component component) {
		setId(component.getId());
		setName(component.getName());
		setPayload(component.getPayload());
		setType(component.getType());
	}

	public long getResourceId() {
		return resourceId;
	}

	public void setResourceId(long resourceId) {
		this.resourceId = resourceId;
	}

	public long getConfigurationId() {
		return configurationId;
	}

	public void setConfigurationId(long configurationId) {
		this.configurationId = configurationId;
	}
}
