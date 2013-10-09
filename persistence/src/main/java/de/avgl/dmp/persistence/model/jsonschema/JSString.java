package de.avgl.dmp.persistence.model.jsonschema;

import java.util.List;

public class JSString extends JSElement {

	public JSString(String name) {
		super(name);
	}

	@Override
	public String getType() {
		return "string";
	}

	@Override
	public List<JSElement> getProperties() {
		return null;
	}

	@Override
	public JSElement withName(String newName) {
		return new JSString(newName);
	}
}
