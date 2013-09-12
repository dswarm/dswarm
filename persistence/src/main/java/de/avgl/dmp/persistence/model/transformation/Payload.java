package de.avgl.dmp.persistence.model.transformation;

import java.util.Map;

public class Payload extends DMPObject {

	private Map<String, Parameter>	parameters;

	public Payload() {
	}

	public Map<String, Parameter> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, Parameter> parameters) {
		this.parameters = parameters;
	}

	@Override
	public boolean equals(Object o) {

		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		Payload payload = (Payload) o;

		if (parameters != null ? !parameters.equals(payload.parameters) : payload.parameters != null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (parameters != null ? parameters.hashCode() : 0);
		return result;
	}
}
