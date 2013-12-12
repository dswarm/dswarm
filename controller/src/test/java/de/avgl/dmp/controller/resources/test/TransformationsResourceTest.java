package de.avgl.dmp.controller.resources.test;

import org.junit.After;
import org.junit.Assert;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.avgl.dmp.controller.resources.test.utils.ComponentsResourceTestUtils;
import de.avgl.dmp.controller.resources.test.utils.FunctionsResourceTestUtils;
import de.avgl.dmp.controller.resources.test.utils.TransformationsResourceTestUtils;
import de.avgl.dmp.persistence.model.job.Component;
import de.avgl.dmp.persistence.model.job.Function;
import de.avgl.dmp.persistence.model.job.Transformation;
import de.avgl.dmp.persistence.service.job.TransformationService;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

public class TransformationsResourceTest extends BasicResourceTest<TransformationsResourceTestUtils, TransformationService, Transformation, Long> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(AttributesResourceTest.class);

	private final FunctionsResourceTestUtils		functionsResourceTestUtils;

	private final ComponentsResourceTestUtils		componentsResourceTestUtils;

	private Function								function;

	private Component								component;

	public TransformationsResourceTest() {

		super(Transformation.class, TransformationService.class, "transformations", "transformation.json", new TransformationsResourceTestUtils());

		functionsResourceTestUtils = new FunctionsResourceTestUtils();
		componentsResourceTestUtils = new ComponentsResourceTestUtils();
	}

	@Override
	public void prepare() throws Exception {

		super.prepare();

		function = functionsResourceTestUtils.createObject("function.json");

		// prepare component json for function manipulation
		String componentJSONString = DMPPersistenceUtil.getResourceAsString("component.json");
		final ObjectNode componentJSON = objectMapper.readValue(componentJSONString, ObjectNode.class);

		final String finalFunctionJSONString = objectMapper.writeValueAsString(function);

		Assert.assertNotNull("the function JSON string shouldn't be null", finalFunctionJSONString);

		final ObjectNode finalFunctionJSON = objectMapper.readValue(finalFunctionJSONString, ObjectNode.class);

		Assert.assertNotNull("the function JSON shouldn't be null", finalFunctionJSON);

		componentJSON.put("function", finalFunctionJSON);

		// re-init expect component
		componentJSONString = objectMapper.writeValueAsString(componentJSON);
		final Component expectedComponent = objectMapper.readValue(componentJSONString, Component.class);

		component = componentsResourceTestUtils.createObject(componentJSONString, expectedComponent);

		// prepare transformation json for component manipulation
		final ObjectNode objectJSON = objectMapper.readValue(objectJSONString, ObjectNode.class);

		final String finalComponentJSONString = objectMapper.writeValueAsString(component);

		Assert.assertNotNull("the component JSON string shouldn't be null", finalComponentJSONString);

		final ObjectNode finalComponentJSON = objectMapper.readValue(finalComponentJSONString, ObjectNode.class);

		Assert.assertNotNull("the component JSON shouldn't be null", finalComponentJSON);

		final ArrayNode componentsJSONArray = objectMapper.createArrayNode();

		componentsJSONArray.add(finalComponentJSON);

		objectJSON.put("components", componentsJSONArray);

		// re-init expect object
		objectJSONString = objectMapper.writeValueAsString(objectJSON);
		expectedObject = objectMapper.readValue(objectJSONString, pojoClass);
	}

	@After
	public void tearDown2() throws Exception {

		functionsResourceTestUtils.deleteObject(function);
	}

	// private static final JsonNodeFactory factory = JsonNodeFactory.instance;
	// private static final ObjectMapper mapper;
	//
	// static {
	// mapper = new ObjectMapper();
	// final JaxbAnnotationModule module = new JaxbAnnotationModule();
	// mapper.registerModule(module);
	// }
	//
	// private ObjectNode transformationJSON = null;
	// private String transformationJSONString = null;
	//
	// public TransformationsResourceTest() {
	// super("transformations");
	// }
	//
	// @Before
	// public void prepare() throws IOException {
	// transformationJSONString = DMPPersistenceUtil.getResourceAsString("transformations-post-request.json");
	// transformationJSON = mapper.readValue(transformationJSONString, ObjectNode.class);
	// }
	//
	// /**
	// * test post of transformations
	// */
	// @Ignore
	// @Test
	// public void testEchoJSON() {
	// Response response = target("echo").request(MediaType.APPLICATION_JSON_TYPE)
	// .accept(MediaType.APPLICATION_JSON_TYPE)
	// .post(Entity.json(transformationJSONString));
	// String responseString = response.readEntity(String.class);
	//
	// final ObjectNode expected = new ObjectNode(factory);
	//
	// expected.put("response_message", "this is your response message");
	// expected.put("request_message", transformationJSON);
	//
	// Assert.assertEquals("POST responses are not equal", expected.toString(), responseString);
	// Assert.assertEquals("200 OK was expected", 200, response.getStatus());
	// }
	//
	// @Ignore
	// @Test
	// public void testXML() throws Exception {
	//
	// final Response response = target().request(MediaType.APPLICATION_XML_TYPE)
	// .accept(MediaType.APPLICATION_XML_TYPE)
	// .post(Entity.json(transformationJSONString));
	//
	// Assert.assertEquals("200 OK was expected", 200, response.getStatus());
	//
	// final String responseString = response.readEntity(String.class);
	//
	// final String expected = DMPPersistenceUtil.getResourceAsString("transformations-post-metamorph.xml");
	//
	// Assert.assertEquals("POST responses are not equal", expected, responseString);
	// }
	//
	// @Ignore
	// @Test
	// public void testTransformationDemo() throws Exception {
	//
	// final Response response = target("/demo").request(MediaType.APPLICATION_JSON_TYPE)
	// .accept(MediaType.APPLICATION_JSON_TYPE)
	// .post(Entity.json(transformationJSONString));
	//
	// Assert.assertEquals("200 OK was expected", 200, response.getStatus());
	//
	// final String responseString = response.readEntity(String.class);
	//
	// final String expected = DMPPersistenceUtil.getResourceAsString("transformations-post-result.json");
	//
	// Assert.assertEquals("POST responses are not equal", expected, responseString);
	// }
}
