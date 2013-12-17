package de.avgl.dmp.persistence.model.jsonschema;


import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;

public class JSOther extends JSElement {

	private final String nameSpace;

	public JSOther(final String name, final String nameSpace) {
		super(name);
		this.nameSpace = nameSpace;
	}

	@Override
	public String getType() {
		return "other";
	}

	@Override
	public List<JSElement> getProperties() {
		return null;
	}

	@Override
	public JSElement withName(final String newName) {
		return new JSOther(newName, nameSpace);
	}

	String getNameSpace() {
		return nameSpace;
	}

	@Override
	protected void renderInternal(final JsonGenerator jgen) throws IOException {
		jgen.writeStringField("namespace", getNameSpace());
	}
}
