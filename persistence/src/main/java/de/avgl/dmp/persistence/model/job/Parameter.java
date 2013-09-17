package de.avgl.dmp.persistence.model.job;

import java.util.Map;

public class Parameter extends DMPObject {

	private String	type;
	private boolean	repeat;
	private String 	data;

	private Map<String, Parameter> parameters;


	public Parameter() {
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isRepeat() {
		return repeat;
	}

	public void setRepeat(boolean repeat) {
		this.repeat = repeat;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
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

		Parameter parameter = (Parameter) o;

		if (parameters != null ? !parameters.equals(parameter.parameters) : parameter.parameters != null)
			return false;
		if (type != null ? !type.equals(parameter.type) : parameter.type != null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (type != null ? type.hashCode() : 0);
		result = 31 * result + (parameters != null ? parameters.hashCode() : 0);
		return result;
	}
}
