package de.avgl.dmp.persistence.model.jsonschema;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;

public abstract class JSElement {

	final String name;

	String description = null;

	protected JSElement(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public abstract String getType();

	public abstract List<JSElement> getProperties();

	public abstract JSElement withName(String newName);

	protected void render(JsonGenerator jgen) throws IOException {

		jgen.writeObjectFieldStart(getName());

		jgen.writeStringField("type", getType());
		renderDescription(jgen);
		renderInternal(jgen);

		jgen.writeEndObject();
	}

	protected void renderDescription(JsonGenerator jgen) throws IOException {
		if (getDescription() != null) {
			jgen.writeStringField("description", getDescription());
		}
	}

	protected void renderInternal(JsonGenerator jgen) throws IOException {}
}
