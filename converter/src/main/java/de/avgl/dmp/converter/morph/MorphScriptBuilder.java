package de.avgl.dmp.converter.morph;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.lang3.StringEscapeUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.InputSupplier;
import com.google.common.io.Resources;

import de.avgl.dmp.converter.DMPConverterException;
import de.avgl.dmp.init.util.DMPStatics;
import de.avgl.dmp.persistence.model.job.Component;
import de.avgl.dmp.persistence.model.job.Function;
import de.avgl.dmp.persistence.model.job.Mapping;
import de.avgl.dmp.persistence.model.job.Task;
import de.avgl.dmp.persistence.model.job.Transformation;
import de.avgl.dmp.persistence.model.schema.MappingAttributePathInstance;

/**
 * Creates a metamorph script from a given {@link Task}.
 *
 * @author phorn
 * @author tgaengler
 */
public class MorphScriptBuilder {

	private static final org.apache.log4j.Logger	LOG							= org.apache.log4j.Logger.getLogger(MorphScriptBuilder.class);

	private static final String						MAPPING_PREFIX				= "mapping";

	private static final DocumentBuilderFactory		DOC_FACTORY					= DocumentBuilderFactory.newInstance();

	private static final String						SCHEMA_PATH					= "schemata/metamorph.xsd";

	private static final TransformerFactory			TRANSFORMER_FACTORY;

	private static final String						TRANSFORMER_FACTORY_CLASS	= "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl";

	private static final String						INPUT_VARIABLE_IDENTIFIER	= "inputString";

	private static final String						OUTPUT_VARIABLE_IDENTIFIER	= "transformationOutputVariable";

	static {
		System.setProperty("javax.xml.transform.TransformerFactory", MorphScriptBuilder.TRANSFORMER_FACTORY_CLASS);
		TRANSFORMER_FACTORY = TransformerFactory.newInstance();
		MorphScriptBuilder.TRANSFORMER_FACTORY.setAttribute("indent-number", 4);

		final URL resource = Resources.getResource(MorphScriptBuilder.SCHEMA_PATH);
		final InputSupplier<InputStream> inputStreamInputSupplier = Resources.newInputStreamSupplier(resource);

		try (final InputStream schemaStream = inputStreamInputSupplier.getInput()) {

			// final StreamSource SCHEMA_SOURCE = new StreamSource(schemaStream);
			final SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = null;

			try {

				// TODO: dummy schema right now, since it couldn't parse the metamorph schema for some reason
				schema = sf.newSchema();
			} catch (final SAXException e) {

				e.printStackTrace();
			}

			if (schema == null) {

				MorphScriptBuilder.LOG.error("couldn't parse schema");
			}

			MorphScriptBuilder.DOC_FACTORY.setSchema(schema);

		} catch (final IOException e1) {
			MorphScriptBuilder.LOG.error("couldn't read schema resource", e1);
		}
	}

	private Document								doc;

	private Element varDefinition(final String key, final String value) {
		final Element var = doc.createElement("var");
		var.setAttribute("name", key);
		var.setAttribute("value", value);

		return var;
	}

	public String render(final boolean indent, final Charset encoding) {
		final String defaultEncoding = encoding.name();
		final Transformer transformer;
		try {
			transformer = MorphScriptBuilder.TRANSFORMER_FACTORY.newTransformer();
		} catch (final TransformerConfigurationException e) {
			e.printStackTrace();
			return null;
		}

		transformer.setOutputProperty(OutputKeys.INDENT, indent ? "yes" : "no");

		transformer.setOutputProperty(OutputKeys.ENCODING, defaultEncoding);

		final ByteArrayOutputStream stream = new ByteArrayOutputStream();

		final StreamResult result;
		try {
			result = new StreamResult(new OutputStreamWriter(stream, defaultEncoding));
		} catch (final UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}

		try {
			transformer.transform(new DOMSource(doc), result);
		} catch (final TransformerException e) {
			e.printStackTrace();
			return null;
		}

		try {
			return stream.toString(defaultEncoding);
		} catch (final UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	public String render(final boolean indent) {
		return render(indent, Charset.forName("UTF-8"));
	}

	@Override
	public String toString() {
		return render(true);
	}

	public File toFile() throws IOException {
		final String str = render(false);

		final File file = File.createTempFile("avgl_dmp", ".tmp");

		final BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		bw.write(str);
		bw.close();

		return file;
	}

	// public MorphScriptBuilder apply(final List<Transformation> transformations) throws DMPConverterException {
	// final DocumentBuilder docBuilder;
	// try {
	// docBuilder = DOC_FACTORY.newDocumentBuilder();
	// } catch (ParserConfigurationException e) {
	// throw new DMPConverterException(e.getMessage());
	// }
	//
	// doc = docBuilder.newDocument();
	// doc.setXmlVersion("1.0");
	//
	// final Element rootElement = doc.createElement("metamorph");
	// rootElement.setAttribute("xmlns", "http://www.culturegraph.org/metamorph");
	// rootElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
	// rootElement.setAttribute("xsi:schemaLocation", "http://www.culturegraph.org/metamorph metamorph.xsd");
	// rootElement.setAttribute("entityMarker", ".");
	// rootElement.setAttribute("version", "1");
	// doc.appendChild(rootElement);
	//
	// final Element meta = doc.createElement("meta");
	// rootElement.appendChild(meta);
	//
	// final Element metaName = doc.createElement("name");
	// meta.appendChild(metaName);
	//
	// // final Element vars = doc.createElement("vars");
	// // rootElement.appendChild(vars);
	//
	// final Element rules = doc.createElement("rules");
	// rootElement.appendChild(rules);
	//
	// final List<String> metas = Lists.newArrayList();
	//
	// for (final Transformation transformation : transformations) {
	// metas.add(transformation.getName());
	//
	// // for (final Element var: createVarDefinitions(transformation)) {
	// // vars.appendChild(var);
	// // }
	//
	// final Element data = createTransformation(transformation);
	//
	// rules.appendChild(data);
	// }
	//
	// metaName.setTextContent(Joiner.on(", ").join(metas));
	//
	// return this;
	// }

	// public MorphScriptBuilder apply(final Transformation transformation) throws DMPConverterException {
	//
	// return apply(Lists.newArrayList(transformation));
	// }

	public MorphScriptBuilder apply(final Task task) throws DMPConverterException {

		final DocumentBuilder docBuilder;
		try {
			docBuilder = MorphScriptBuilder.DOC_FACTORY.newDocumentBuilder();
		} catch (final ParserConfigurationException e) {
			throw new DMPConverterException(e.getMessage());
		}

		doc = docBuilder.newDocument();
		doc.setXmlVersion("1.1");

		final Element rootElement = doc.createElement("metamorph");
		rootElement.setAttribute("xmlns", "http://www.culturegraph.org/metamorph");
		rootElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		rootElement.setAttribute("xsi:schemaLocation", "http://www.culturegraph.org/metamorph metamorph.xsd");
		rootElement.setAttribute("entityMarker", DMPStatics.ATTRIBUTE_DELIMITER.toString());
		rootElement.setAttribute("version", "1");
		doc.appendChild(rootElement);

		final Element meta = doc.createElement("meta");
		rootElement.appendChild(meta);

		final Element metaName = doc.createElement("name");
		meta.appendChild(metaName);

		final Element rules = doc.createElement("rules");
		rootElement.appendChild(rules);

		final List<String> metas = Lists.newArrayList();

		for (final Mapping mapping : task.getJob().getMappings()) {
			metas.add(MorphScriptBuilder.MAPPING_PREFIX + mapping.getId());

			createTransformation(rules, mapping);

		}

		metaName.setTextContent(Joiner.on(", ").join(metas));

		return this;
	}

	private void createTransformation(final Element rules, final Mapping mapping) {

		// first handle the parameter mapping from the attribute paths of the mapping to the transformation component

		final Component transformationComponent = mapping.getTransformation();

		if (transformationComponent == null) {

			MorphScriptBuilder.LOG.debug("transformation component for mapping '" + mapping.getId() + "' was empty");

			// just delegate input attribute path to output attribute path

			mapInputAttributePathToOutputAttributePath(mapping, rules);

			return;
		}

		if (transformationComponent.getParameterMappings() == null || transformationComponent.getParameterMappings().isEmpty()) {

			MorphScriptBuilder.LOG.debug("parameter mappings for transformation component shouldn't be empty, mapping: '" + mapping.getId() + "'");

			// delegate input attribute path to output attribute path + add possible transformations (components)

			mapInputAttributePathToOutputAttributePath(mapping, rules);
			processTransformationComponentFunction(transformationComponent, mapping, null, rules);

			return;
		}

		// get all input attribute paths and create datas for them

		final Set<MappingAttributePathInstance> inputAttributePathInstances = mapping.getInputAttributePaths();

		final Map<String, List<String>> inputAttributePaths = Maps.newLinkedHashMap();

		for (final Iterator<MappingAttributePathInstance> iterator = inputAttributePathInstances.iterator(); iterator.hasNext();) {

			final MappingAttributePathInstance mappingAttributePathInstance = iterator.next();

			final String inputAttributePathString = mappingAttributePathInstance.getAttributePath().toAttributePath();

			final List<String> variablesFromInputAttributePaths = getParameterMappingKeys(inputAttributePathString, transformationComponent);

			final List<Element> datas = addInputAttributePathMappings(variablesFromInputAttributePaths, inputAttributePathString, rules,
					inputAttributePaths);

			// TODO: filter inputAttributePath in data section
		}

		final String outputAttributePath = mapping.getOutputAttributePath().getAttributePath().toAttributePath();

		final List<String> variablesFromOutputAttributePath = getParameterMappingKeys(outputAttributePath, transformationComponent);

		final Element dataOutput = addOutputAttributePathMapping(variablesFromOutputAttributePath, outputAttributePath, rules);

		processTransformationComponentFunction(transformationComponent, mapping, inputAttributePaths, rules);
	}

	private void createParameters(final Map<String, String> parameterMappings, final Element component) {

		// TODO: parse parameter values that can be simple string values, JSON objects or JSON arrays (?)
		// => for now we expect only simple string values

		if (parameterMappings != null) {

			for (final Entry<String, String> parameterMapping : parameterMappings.entrySet()) {

				if (parameterMapping.getKey() != null) {

					if (parameterMapping.getKey().equals(INPUT_VARIABLE_IDENTIFIER)) {

						continue;
					}

					if (parameterMapping.getValue() != null) {

						final Attr param = doc.createAttribute(parameterMapping.getKey());
						param.setValue(parameterMapping.getValue());
						component.setAttributeNode(param);
					}
				}
			}
		}
	}

	private Element createDataTag(final Component singleInputComponent, final String dataNameAttribute, final String dataSourceAttribute) {

		final Element data = doc.createElement("data");

		data.setAttribute("source", "@" + dataSourceAttribute);

		data.setAttribute("name", "@" + dataNameAttribute);

		final Element comp = doc.createElement(singleInputComponent.getFunction().getName());

		createParameters(singleInputComponent.getParameterMappings(), comp);

		data.appendChild(comp);

		return data;
	}

	private Element createCollectionTag(final Component multipleInputComponent, final String collectionNameAttribute,
			final Set<String> collectionSourceAttributes) {

		final Element collection;

		if (multipleInputComponent.getFunction().getName().equals("concat")) {

			final Map<String, String> parameters = multipleInputComponent.getParameterMappings();

			String valueString = "";

			String delimiterString = ", ";

			if (parameters.get("prefix") != null) {
				valueString = parameters.get("prefix").toString();
			}

			if (parameters.get("delimiter") != null) {
				delimiterString = parameters.get("delimiter").toString();
			}

			collection = doc.createElement("combine");

			collection.setAttribute("name", "@" + collectionNameAttribute);

			final Iterator<String> iter = collectionSourceAttributes.iterator();

			int i = 0;

			while (iter.hasNext()) {

				final String sourceAttribute = iter.next();

				valueString += "${" + sourceAttribute + "}";

				if ((i + 1) < collectionSourceAttributes.size()) {
					valueString += delimiterString;
				}

				final Element collectionData = doc.createElement("data");

				collectionData.setAttribute("source", "@" + sourceAttribute);

				collectionData.setAttribute("name", sourceAttribute);

				collection.appendChild(collectionData);

			}

			if (parameters.get("postfix") != null) {
				valueString += parameters.get("postfix").toString();
			}

			collection.setAttribute("value", valueString);

		} else {

			collection = doc.createElement(multipleInputComponent.getFunction().getName());

			createParameters(multipleInputComponent.getParameterMappings(), collection);

			collection.setAttribute("name", "@" + collectionNameAttribute);

			for (final String sourceAttribute : collectionSourceAttributes) {

				final Element collectionData = doc.createElement("data");

				collectionData.setAttribute("source", "@" + sourceAttribute);

				collection.appendChild(collectionData);
			}
		}
		return collection;
	}

	private List<String> getParameterMappingKeys(final String attributePathString, final Component transformationComponent) {

		List<String> parameterMappingKeys = null;

		final Map<String, String> transformationParameterMapping = transformationComponent.getParameterMappings();

		for (final Entry<String, String> parameterMapping : transformationParameterMapping.entrySet()) {

			if (StringEscapeUtils.unescapeXml(parameterMapping.getValue()).equals(attributePathString)) {

				if (parameterMappingKeys == null) {

					parameterMappingKeys = Lists.newArrayList();
				}

				parameterMappingKeys.add(parameterMapping.getKey());
			}
		}

		return parameterMappingKeys;
	}

	private List<Element> addInputAttributePathMappings(final List<String> variables, final String inputAttributePathString, final Element rules,
			final Map<String, List<String>> inputAttributePaths) {

		if (variables == null || variables.isEmpty()) {

			return null;
		}

		List<Element> datas = null;

		final String inputAttributePathStringXMLEscaped = StringEscapeUtils.escapeXml(inputAttributePathString);

		for (final String variable : variables) {

			if (variable.equals(OUTPUT_VARIABLE_IDENTIFIER)) {

				continue;
			}

			final Element data = doc.createElement("data");
			data.setAttribute("source", inputAttributePathStringXMLEscaped);

			data.setAttribute("name", "@" + variable);

			rules.appendChild(data);

			if (datas == null) {

				datas = Lists.newArrayList();
			}

			inputAttributePaths.put(inputAttributePathStringXMLEscaped, variables);
		}

		return datas;
	}

	private Element addOutputAttributePathMapping(final List<String> variables, final String outputAttributePathString, final Element rules) {

		if (variables == null || variables.isEmpty()) {

			return null;
		}

		final String outputAttributePathStringXMLEscaped = StringEscapeUtils.escapeXml(outputAttributePathString);

		// TODO: maybe add mapping to default output variable identifier, if output attribute path is not part of the parameter
		// mappings of the transformation component
		// maybe for later: separate parameter mapppings into input parameter mappings and output parameter mappings

		for (final String variable : variables) {

			if (!variable.equals(OUTPUT_VARIABLE_IDENTIFIER)) {

				continue;
			}

			final Element dataOutput = doc.createElement("data");
			dataOutput.setAttribute("source", "@" + variable);
			dataOutput.setAttribute("name", outputAttributePathStringXMLEscaped);
			rules.appendChild(dataOutput);
		}

		return null;
	}

	private void mapInputAttributePathToOutputAttributePath(final Mapping mapping, final Element rules) {

		final Set<MappingAttributePathInstance> inputMappingAttributePathInstances = mapping.getInputAttributePaths();

		if (inputMappingAttributePathInstances == null || inputMappingAttributePathInstances.isEmpty()) {

			return;
		}

		final MappingAttributePathInstance outputMappingAttributePathInstance = mapping.getOutputAttributePath();

		if (outputMappingAttributePathInstance == null) {

			return;
		}

		final Element data = doc.createElement("data");
		data.setAttribute("source", StringEscapeUtils.escapeXml(inputMappingAttributePathInstances.iterator().next().getAttributePath().toAttributePath()));

		data.setAttribute("name", StringEscapeUtils.escapeXml(outputMappingAttributePathInstance.getAttributePath().toAttributePath()));

		rules.appendChild(data);
	}

	private void processTransformationComponentFunction(final Component transformationComponent, final Mapping mapping,
			final Map<String, List<String>> inputAttributePathVariablesMap, final Element rules) {

		final Function transformationFunction = transformationComponent.getFunction();

		if (transformationFunction == null) {

			MorphScriptBuilder.LOG.debug("transformation component's function for mapping '" + mapping.getId() + "' was empty");

			// nothing to do - mapping from input attribute path to output attribute path should be fine already

			return;
		}

		switch (transformationFunction.getFunctionType()) {

			case Function:

				// TODO: process simple function

				MorphScriptBuilder.LOG.error("transformation component's function for mapping '" + mapping.getId()
						+ "' was a real FUNCTION. this is not supported right now.");

				break;

			case Transformation:

				// TODO: process simple input -> output mapping (?)

				final Transformation transformation = (Transformation) transformationFunction;

				final Set<Component> components = transformation.getComponents();

				if (components == null) {

					MorphScriptBuilder.LOG.debug("transformation component's transformation's components for mapping '" + mapping.getId()
							+ "' are empty");
					return;
				}

				for (final Component component : components) {

					processComponent(component, inputAttributePathVariablesMap, rules);
				}

				break;
		}
	}

	private void processComponent(final Component component, final Map<String, List<String>> inputAttributePathVariablesMap, final Element rules) {

		String[] inputStrings = {};

		final Map<String, String> componentParameterMapping = component.getParameterMappings();

		if (componentParameterMapping != null) {

			for (final Entry<String, String> parameterMapping : componentParameterMapping.entrySet()) {

				if (parameterMapping.getKey().equals(INPUT_VARIABLE_IDENTIFIER)) {

					inputStrings = parameterMapping.getValue().split(",");

					break;
				}
			}
		}

		// this is a list of variable names, which should be unique
		final Set<String> sourceAttributes = Sets.newHashSet();

		for (final String inputString : inputStrings) {

			sourceAttributes.add(inputString);
		}

		if (component.getInputComponents() != null && !component.getInputComponents().isEmpty()) {

			for (final Component inputComponent : component.getInputComponents()) {

				sourceAttributes.add("component" + inputComponent.getId());
			}
		} else {

			// take input attribute path variable

			if (inputAttributePathVariablesMap != null && !inputAttributePathVariablesMap.isEmpty()) {

				sourceAttributes.add(inputAttributePathVariablesMap.entrySet().iterator().next().getValue().iterator().next());
			}
		}

		if (sourceAttributes.isEmpty()) {

			// couldn't identify an input variable or an input attribute path

			return;
		}

		if (sourceAttributes.size() > 1) {

			// TODO: [@tgaengler] multiple identified input variables doesn't really mean that the component refers to a
			// collection, or?

			String collectionNameAttribute = null;

			if (component.getOutputComponents() == null || component.getOutputComponents().isEmpty()) {

				// the end has been reached

				// collectionNameAttribute = getKeyParameterMapping(outputAttributePath, transformationComponent);
				collectionNameAttribute = OUTPUT_VARIABLE_IDENTIFIER;
			} else {

				collectionNameAttribute = "component" + component.getId();
			}

			final Element collection = createCollectionTag(component, collectionNameAttribute, sourceAttributes);

			rules.appendChild(collection);

			return;
		}

		String dataNameAttribute = null;

		if (component.getOutputComponents() == null || component.getOutputComponents().isEmpty()) {

			// dataNameAttribute = getKeyParameterMapping(outputAttributePath, transformationComponent);
			dataNameAttribute = OUTPUT_VARIABLE_IDENTIFIER;
		} else {

			dataNameAttribute = "component" + component.getId();
		}

		final Element data = createDataTag(component, dataNameAttribute, sourceAttributes.iterator().next());

		rules.appendChild(data);
	}
}
