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
import java.util.Collection;
import java.util.List;

import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;

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

		MatcherAssert.assertThat(qux.getProperties(), Matchers.hasSize(1));
		MatcherAssert.assertThat(qux.getProperties(), Matchers.hasItem(aNull));
	}

	private void doRenderTest(final JsonNode jsonNode) throws IOException {

		MatcherAssert.assertThat(jsonNode.get("title").asText(), Matchers.equalTo(obj.getName()));
		MatcherAssert.assertThat(jsonNode.get("type").asText(), Matchers.equalTo(obj.getType()));
		MatcherAssert.assertThat(jsonNode.get("properties").isObject(), Matchers.equalTo(true));

		final List<String> fields = Lists.newArrayList(jsonNode.get("properties").fieldNames());
		final Matcher<Collection<String>> emptyMatcher = Matchers.empty();
		MatcherAssert.assertThat(fields, Matchers.is(emptyMatcher));
	}

	private void doRenderTest(final InputStream inputStream) throws IOException {
		doRenderTest(BaseJSTest.om.readTree(inputStream));
	}

	private void doRenderTest(final ByteArrayOutputStream outputStream) throws IOException {
		doRenderTest(new ByteArrayInputStream(outputStream.toByteArray()));
	}

	private void doRenderTest(final Reader reader) throws IOException {
		doRenderTest(BaseJSTest.om.readTree(reader));
	}

	private void doRenderTest(final StringWriter writer) throws IOException {
		doRenderTest(new StringReader(writer.toString()));
	}

	private void doRenderTest(final String rendered) throws IOException {
		doRenderTest(BaseJSTest.om.readTree(rendered));
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
		final JsonNode jsonNode = BaseJSTest.om.readTree(render);

		MatcherAssert.assertThat(jsonNode.get("title").asText(), Matchers.equalTo("root"));
		MatcherAssert.assertThat(jsonNode.get("type").asText(), Matchers.equalTo("object"));
		MatcherAssert.assertThat(jsonNode.get("properties").isObject(), Matchers.equalTo(true));

		final JsonNode rootProperties = jsonNode.get("properties");
		final ArrayList<String> rootPropertyNames = Lists.newArrayList(rootProperties.fieldNames());

		MatcherAssert.assertThat(rootPropertyNames, Matchers.hasSize(3));

		final String stringFieldName = rootPropertyNames.get(0);
		MatcherAssert.assertThat(stringFieldName, Matchers.equalTo("string"));

		final JsonNode string = rootProperties.get("string");
		MatcherAssert.assertThat(string.isObject(), Matchers.equalTo(true));
		MatcherAssert.assertThat(string.get("type").asText(), Matchers.equalTo("string"));

		final String otherFieldName = rootPropertyNames.get(1);
		MatcherAssert.assertThat(otherFieldName, Matchers.equalTo("other"));

		final JsonNode array = rootProperties.get("other");
		MatcherAssert.assertThat(array.isObject(), Matchers.equalTo(true));
		MatcherAssert.assertThat(array.get("type").asText(), Matchers.equalTo("array"));

		final JsonNode items = array.get("items");
		MatcherAssert.assertThat(items.isObject(), Matchers.equalTo(true));
		final ArrayList<String> itemPropertyNames = Lists.newArrayList(items.fieldNames());

		MatcherAssert.assertThat(itemPropertyNames, Matchers.hasSize(1));

		final String otherOtherFieldName = itemPropertyNames.get(0);
		MatcherAssert.assertThat(otherOtherFieldName, Matchers.equalTo("other"));
		final JsonNode other = items.get("other");

		MatcherAssert.assertThat(other.isObject(), Matchers.equalTo(true));
		MatcherAssert.assertThat(other.get("type").asText(), Matchers.equalTo("other"));
		MatcherAssert.assertThat(other.get("namespace").asText(), Matchers.equalTo("namespace"));

		final String objectFieldName = rootPropertyNames.get(2);
		MatcherAssert.assertThat(objectFieldName, Matchers.equalTo("object"));

		final JsonNode object = rootProperties.get("object");
		MatcherAssert.assertThat(object.isObject(), Matchers.equalTo(true));
		MatcherAssert.assertThat(object.get("type").asText(), Matchers.equalTo("object"));
		MatcherAssert.assertThat(object.get("description").asText(), Matchers.equalTo("description"));

		final JsonNode objectProperties = object.get("properties");
		final ArrayList<String> objectPropertyNames = Lists.newArrayList(objectProperties.fieldNames());

		MatcherAssert.assertThat(objectPropertyNames, Matchers.hasSize(2));

		final String nullFieldName = objectPropertyNames.get(0);
		MatcherAssert.assertThat(nullFieldName, Matchers.equalTo("null"));

		final JsonNode nNull = objectProperties.get("null");
		MatcherAssert.assertThat(nNull.isNull(), Matchers.equalTo(true));

		final String null2FieldName = objectPropertyNames.get(1);
		MatcherAssert.assertThat(null2FieldName, Matchers.equalTo("null2"));

		final JsonNode nNull2 = objectProperties.get("null2");
		MatcherAssert.assertThat(nNull2.isNull(), Matchers.equalTo(true));
	}

	@Test
	public void testOverloadedRender() throws Exception {

		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		final StringWriter writer = new StringWriter();
		File tmpFile;

		final String render = obj.render();
		doRenderTest(render);

		obj.render(BaseJSTest.om, outputStream);
		doRenderTest(outputStream);
		outputStream.reset();

		obj.render(BaseJSTest.om, outputStream, JsonEncoding.UTF8);
		doRenderTest(outputStream);
		outputStream.reset();

		obj.render(BaseJSTest.om, writer);
		doRenderTest(writer);
		outputStream.reset();

		tmpFile = File.createTempFile("dmp-test", "tmp");
		obj.render(BaseJSTest.om, tmpFile, JsonEncoding.UTF8);
		doRenderTest(BaseJSTest.om.readTree(tmpFile));
		tmpFile.deleteOnExit();

		final JsonFactory factory = BaseJSTest.om.getFactory();

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
		doRenderTest(BaseJSTest.om.readTree(tmpFile));
		tmpFile.deleteOnExit();

		final JsonGenerator generator = factory.createGenerator(outputStream);
		obj.render(generator);
		generator.flush();
		doRenderTest(outputStream);
	}
}
