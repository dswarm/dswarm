package de.avgl.dmp.persistence.model.jsonschema;


import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JSRoot extends JSObject {

	public JSRoot(String name) {
		super(name);
	}

	@Override
	public JSElement withName(String newName) {
		final JSRoot jsRoot = new JSRoot(newName);

		for (JSElement jsElement : this) {
			jsRoot.add(jsElement);
		}

		return jsRoot;
	}

	@Override
	protected void render(JsonGenerator jgen) throws IOException {
		jgen.writeStartObject();

		jgen.writeStringField("title", getName());
		jgen.writeStringField("type", getType());

		renderDescription(jgen);

		renderInternal(jgen);

		jgen.writeEndObject();

		jgen.flush();
		jgen.close();
	}

	public void render(ObjectMapper mapper, OutputStream out) throws IOException {
		render(mapper.getFactory(), out);

	}
	public void render(ObjectMapper mapper, OutputStream out, JsonEncoding encoding) throws IOException {
		render(mapper.getFactory(), out, encoding);

	}
	public void render(ObjectMapper mapper, Writer writer) throws IOException {
		render(mapper.getFactory(), writer);

	}
	public void render(ObjectMapper mapper, File file, JsonEncoding encoding) throws IOException {
		render(mapper.getFactory(), file, encoding);
	}

	public void render(JsonFactory jsonFactory, OutputStream out) throws IOException {
		render(jsonFactory.createGenerator(out));

	}
	public void render(JsonFactory jsonFactory, OutputStream out, JsonEncoding encoding) throws IOException {
		render(jsonFactory.createGenerator(out, encoding));

	}
	public void render(JsonFactory jsonFactory, Writer writer) throws IOException {
		render(jsonFactory.createGenerator(writer));

	}
	public void render(JsonFactory jsonFactory, File file, JsonEncoding encoding) throws IOException {
		render(jsonFactory.createGenerator(file, encoding));
	}

	public String render() throws IOException {
		JsonFactory jsonFactory = new JsonFactory();
		final StringWriter writer = new StringWriter();

		render(jsonFactory, writer);

		return writer.getBuffer().toString();
	}
}
