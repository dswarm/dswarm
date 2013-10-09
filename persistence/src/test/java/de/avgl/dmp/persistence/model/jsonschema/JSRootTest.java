package de.avgl.dmp.persistence.model.jsonschema;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;


public class JSRootTest extends BaseJSTest<JSRoot> {

	public JSRootTest() {
		super(JSRoot.class);
	}

	@Override
	@Test
	public void testWithName() throws Exception {
		super.testWithName();

		final JSNull aNull = new JSNull("null");
		obj.add(aNull);
		final JSElement qux = obj.withName("qux");

		assertThat(qux.getProperties(), hasSize(1));
		assertThat(qux.getProperties(), hasItem(aNull));
	}

	private void doRenderTest(final JsonNode jsonNode) throws IOException {

		assertThat(jsonNode.get("title").asText(), equalTo(obj.getName()));
		assertThat(jsonNode.get("type").asText(), equalTo(obj.getType()));
		assertThat(jsonNode.get("properties").isObject(), equalTo(true));
		assertThat(Lists.newArrayList(jsonNode.get("properties").fieldNames()), is(empty()));
	}

	private void doRenderTest(final InputStream inputStream) throws IOException {
		doRenderTest(om.readTree(inputStream));
	}

	private void doRenderTest(final ByteArrayOutputStream outputStream) throws IOException {
		doRenderTest(new ByteArrayInputStream(outputStream.toByteArray()));
	}

	private void doRenderTest(Reader reader) throws IOException {
		doRenderTest(om.readTree(reader));
	}

	private void doRenderTest(StringWriter writer) throws IOException {
		doRenderTest(new StringReader(writer.toString()));
	}

	private void doRenderTest(String rendered) throws IOException {
		doRenderTest(om.readTree(rendered));
	}

	@Test
	public void testComplexRender() throws Exception {

		final JSObject jsObject = new JSObject("object");
		jsObject.add(new JSNull("null"));
		jsObject.addAll(Lists.newArrayList(new JSNull("null2")));
		jsObject.setDescription("description");

		final JSRoot root = new JSRoot("root");

		root.add(new JSString("string"));
		root.add(new JSArray(new JSOther("other", "namespace")));
		root.add(jsObject);


		final String render = root.render();
		final JsonNode jsonNode = om.readTree(render);

		assertThat(jsonNode.get("title").asText(), equalTo("root"));
		assertThat(jsonNode.get("type").asText(), equalTo("object"));
		assertThat(jsonNode.get("properties").isObject(), equalTo(true));

		final JsonNode rootProperties = jsonNode.get("properties");
		final ArrayList<String> rootPropertyNames = Lists.newArrayList(rootProperties.fieldNames());

		assertThat(rootPropertyNames, hasSize(3));

		final String stringFieldName = rootPropertyNames.get(0);
		assertThat(stringFieldName, equalTo("string"));

		final JsonNode string = rootProperties.get("string");
		assertThat(string.isObject(), equalTo(true));
		assertThat(string.get("type").asText(), equalTo("string"));



		final String otherFieldName = rootPropertyNames.get(1);
		assertThat(otherFieldName, equalTo("other"));

		final JsonNode array = rootProperties.get("other");
		assertThat(array.isObject(), equalTo(true));
		assertThat(array.get("type").asText(), equalTo("array"));

		final JsonNode items = array.get("items");
		assertThat(items.isObject(), equalTo(true));
		final ArrayList<String> itemPropertyNames = Lists.newArrayList(items.fieldNames());

		assertThat(itemPropertyNames, hasSize(1));

		final String otherOtherFieldName = itemPropertyNames.get(0);
		assertThat(otherOtherFieldName, equalTo("other"));
		final JsonNode other = items.get("other");

		assertThat(other.isObject(), equalTo(true));
		assertThat(other.get("type").asText(), equalTo("other"));
		assertThat(other.get("namespace").asText(), equalTo("namespace"));


		final String objectFieldName = rootPropertyNames.get(2);
		assertThat(objectFieldName, equalTo("object"));

		final JsonNode object = rootProperties.get("object");
		assertThat(object.isObject(), equalTo(true));
		assertThat(object.get("type").asText(), equalTo("object"));
		assertThat(object.get("description").asText(), equalTo("description"));

		final JsonNode objectProperties = object.get("properties");
		final ArrayList<String> objectPropertyNames = Lists.newArrayList(objectProperties.fieldNames());

		assertThat(objectPropertyNames, hasSize(2));

		final String nullFieldName = objectPropertyNames.get(0);
		assertThat(nullFieldName, equalTo("null"));

		final JsonNode nNull = objectProperties.get("null");
		assertThat(nNull.isNull(), equalTo(true));

		final String null2FieldName = objectPropertyNames.get(1);
		assertThat(null2FieldName, equalTo("null2"));

		final JsonNode nNull2 = objectProperties.get("null2");
		assertThat(nNull2.isNull(), equalTo(true));
	}

	@Test
	public void testOverloadedRender() throws Exception {

		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		final StringWriter writer = new StringWriter();
		File tmpFile;

		final String render = obj.render();
		doRenderTest(render);


		obj.render(om, outputStream);
		doRenderTest(outputStream);
		outputStream.reset();


		obj.render(om, outputStream, JsonEncoding.UTF8);
		doRenderTest(outputStream);
		outputStream.reset();


		obj.render(om, writer);
		doRenderTest(writer);
		outputStream.reset();


		tmpFile = File.createTempFile("dmp-test", "tmp");
		obj.render(om, tmpFile, JsonEncoding.UTF8);
		doRenderTest(om.readTree(tmpFile));
		tmpFile.deleteOnExit();


		final JsonFactory factory = om.getFactory();

		obj.render(factory, outputStream);
		doRenderTest(outputStream);
		outputStream.reset();


		obj.render(factory, outputStream, JsonEncoding.UTF8);
		doRenderTest(outputStream);
		outputStream.reset();


		obj.render(factory, writer);
		doRenderTest(writer);
		outputStream.reset();


		tmpFile = File.createTempFile("dmp-test", "tmp");
		obj.render(factory, tmpFile, JsonEncoding.UTF8);
		doRenderTest(om.readTree(tmpFile));
		tmpFile.deleteOnExit();


		final JsonGenerator generator = factory.createGenerator(outputStream);
		obj.render(generator);
		generator.flush();
		doRenderTest(outputStream);
	}
}
