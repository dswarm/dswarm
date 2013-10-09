package de.avgl.dmp.persistence.model.job;

import java.util.Map;

public class Payload extends DMPObject {

	private Map<String, Parameter>	parameters;

	public Map<String, Parameter> getParameters() {
		return parameters;
	}

	public void setParameters(final Map<String, Parameter> parameters) {
		this.parameters = parameters;
	}

	@Override
	public boolean equals(final Object o) {

		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		final Payload payload = (Payload) o;

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
