package de.avgl.dmp.converter.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.*;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.*;

public class TransformationsConverter {

	private static final ObjectMapper mapper;

	private static final DocumentBuilderFactory docFactory;

	static {
		// create once, reuse
		mapper = new ObjectMapper();

		final JaxbAnnotationModule module = new JaxbAnnotationModule();
		// configure as necessary
		mapper.registerModule(module);


		docFactory = DocumentBuilderFactory.newInstance();
	}

	private static Map<String, Parameter> extractParameters(final JsonNode root) {
		if (root == null) {
			return null;
		}

		Iterator<String> jsParameterFields = root.fieldNames();
		Map<String, Parameter> parameters = new HashMap<>();

		while (jsParameterFields.hasNext()) {
			String parameterName = jsParameterFields.next();
			JsonNode jsParameter = root.get(parameterName);

			Parameter parameter = new Parameter();

			parameter.setName(parameterName);
			parameter.setType(jsParameter.has("type")? jsParameter.get("type").asText() : null);
			parameter.setRepeat(jsParameter.hasNonNull("repeat") && jsParameter.get("repeat").asBoolean());

			parameter.setData(jsParameter.hasNonNull("data")? jsParameter.get("data").asText() :null);

			if (jsParameter.hasNonNull("parameters")) {
				parameter.setParameters(extractParameters(jsParameter.get("parameters")));
			}

			parameters.put(parameterName, parameter);
		}

		return parameters;
	}

	private static void createParameters(final Map<String, Parameter> parameters, Document doc, Element component) {
		if (parameters != null) {
			for (Parameter parameter : parameters.values()) {
				if (parameter.getName() != null) {
					if (parameter.getData() != null) {
						final Attr param = doc.createAttribute(parameter.getName());
						param.setValue(parameter.getData());
						component.setAttributeNode(param);
					} else if (parameter.isRepeat()) {
						final Element subEl = doc.createElement(parameter.getName());
						component.appendChild(subEl);
						createParameters(parameter.getParameters(), doc, subEl);
					}
				}
			}
		}
	}

	private static Element createDomFor(final Transformation transformation, final Document doc) throws ParserConfigurationException, IOException {
		Element data = doc.createElement("data");
		data.setAttribute("source", "record." + transformation.getSource().getName());
		data.setAttribute("name", "record." + transformation.getTarget().getName());

		for (Component component : transformation.getComponents()) {
			Element comp = doc.createElement(component.getName());

			createParameters(component.getPayload().getParameters(), doc, comp);
			data.appendChild(comp);
		}

		return data;
	}

	public static File createMorphFile(final String jsonIn) throws IOException, ParserConfigurationException {
		final List<Transformation> pojos = toList(jsonIn);

		return createMorphFile(pojos);
	}

	public static File createMorphFile(final List<Transformation> transformations) throws IOException, ParserConfigurationException {
		final String xml = createDom(transformations);
		return createMorphFile(xml.getBytes("UTF-8"));
	}

	public static File createMorphFile(final Transformation transformation) throws IOException, ParserConfigurationException {
		return createMorphFile(Lists.newArrayList(transformation));
	}

	public static File createMorphFile(final byte[] xmlContent) throws IOException, ParserConfigurationException {
		final File file = File.createTempFile("avgl_dmp", ".tmp");

		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		bw.write(new String(xmlContent, "UTF-8"));
		bw.close();

		return file;
	}

	public static String createDom(final List<Transformation> transformations) throws ParserConfigurationException, IOException {
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		Document doc = docBuilder.newDocument();
		doc.setXmlVersion("1.0");

		Element rootElement = doc.createElement("metamorph");
		rootElement.setAttribute("xmlns", "http://www.culturegraph.org/metamorph");
		rootElement.setAttribute("version", "1");
		rootElement.setAttribute("entityMarker", ".");
		doc.appendChild(rootElement);

		Element meta = doc.createElement("meta");
		rootElement.appendChild(meta);

		Element metaName = doc.createElement("name");
		meta.appendChild(metaName);

		Element rules = doc.createElement("rules");
		rootElement.appendChild(rules);

		final List<String> metas = new ArrayList<>();

		for (Transformation transformation : transformations) {
			metas.add(transformation.getName());
			Element data = createDomFor(transformation, doc);

			rules.appendChild(data);
		}

		metaName.setTextContent(Joiner.on(", ").join(metas));

		OutputFormat format = new OutputFormat(doc);
		format.setEncoding("UTF-8");
		format.setIndenting(true);
		format.setIndent(2);
		format.setLineWidth(120);

		StringWriter out = new StringWriter();
		XMLSerializer serializer = new XMLSerializer(out, format);
		serializer.serialize(doc);

		return out.toString();
	}

	public static String createDom(final Transformation transformation) throws ParserConfigurationException, IOException {
		return createDom(Lists.newArrayList(transformation));
	}

	public static List<Transformation> toList(final String jsonObjectString) throws IOException {
		final JsonNode root = mapper.readTree(jsonObjectString);

		final ImmutableList.Builder<Transformation> transformationsBuilder = ImmutableList.builder();

		final JsonNode jsTransformations = root.get("transformations");

		for (final JsonNode jsTransformation : jsTransformations) {

			final Transformation transformation = new Transformation();

			transformation.setId(jsTransformation.get("id").asText());
			transformation.setName(jsTransformation.get("name").asText());

			final JsonNode jsSource = jsTransformation.get("source");
			final JsonNode jsTarget = jsTransformation.get("target");
			final Component source = extractComponent(jsSource);
			final Component target = extractComponent(jsTarget);

			transformation.setSource(source);
			transformation.setTarget(target);

			final ImmutableList.Builder<Component> componentsBuilder = ImmutableList.builder();

			for (final JsonNode jsComponent : jsTransformation.get("components")) {

				final Component component = extractComponent(jsComponent);
				if (component == null) continue;

				componentsBuilder.add(component);
			}

			transformation.setComponents(componentsBuilder.build());
			transformationsBuilder.add(transformation);
		}

		return transformationsBuilder.build();
	}

	private static Component extractComponent(JsonNode jsComponent) {
		ComponentType componentType;
		try {
			JsonNode jsComponentTyeJs = jsComponent.get("componentType");
			String jsComponentType = jsComponentTyeJs.asText();
			componentType = ComponentType.getComponentTypeByName(jsComponentType);
		} catch (DMPPersistenceException e) {
			e.printStackTrace();
			return null;
		}

		Component component = new Component();

		component.setId(jsComponent.get("id").asText());
		component.setType(componentType);

		JsonNode jsPayload = jsComponent.get("payload");

		Payload payload = new Payload();

		String name = null;
		switch (componentType) {
			case SOURCE:
			case TARGET:
				name = jsPayload.get("path").asText();
				break;
			case FUNCTION:
				name = jsPayload.get("name").asText();
				break;
			case EXTENDED:
			default:
				break;
		}

		payload.setName(name);
		component.setName(name);
		payload.setParameters(extractParameters(jsPayload.get("parameters")));

		component.setPayload(payload);
		return component;
	}

	public static ObjectNode toObjectNode(final String jsonObjectString) {
		ObjectNode jsonObject = null;

		try {
			jsonObject = mapper.readValue(jsonObjectString, ObjectNode.class);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return jsonObject;
	}

	// static object
	private TransformationsConverter() {
	}
}
