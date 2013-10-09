package de.avgl.dmp.persistence.model.jsonschema;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;

public class JSNull extends JSElement {

	public JSNull(final String name) {
		super(name);
	}

	@Override
	public String getType() {
		return "null";
	}

	@Override
	public List<JSElement> getProperties() {
		return null;
	}

	@Override
	public JSElement withName(final String newName) {
		return new JSNull(newName);
	}

	@Override
	protected void render(final JsonGenerator jgen) throws IOException {
		jgen.writeNullField(getName());
	}
}
