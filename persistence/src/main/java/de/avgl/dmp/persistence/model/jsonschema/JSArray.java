package de.avgl.dmp.persistence.model.jsonschema;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;

public class JSArray extends JSElement {

	private final JSElement item;

	public JSArray(final JSElement item) {

		super(item.getName());
		this.item = item;
	}

	@Override
	public String getType() {
		return "array";
	}

	@Override
	public List<JSElement> getProperties() {
		return null;
	}

	@Override
	public JSElement withName(final String newName) {
		return new JSArray(item.withName(newName));
	}

	public JSElement getItem() {
		return item;
	}

	@Override
	protected void renderInternal(final JsonGenerator jgen) throws IOException {

		renderDescription(jgen);

		jgen.writeObjectFieldStart("items");
		getItem().render(jgen);
		jgen.writeEndObject();
	}
}
