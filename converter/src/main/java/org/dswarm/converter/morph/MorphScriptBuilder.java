/**
 * Copyright (C) 2013 – 2015 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dswarm.converter.morph;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.Map.Entry;

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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.CharSource;
import com.google.common.io.Resources;
import org.apache.commons.io.Charsets;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.dswarm.persistence.model.job.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import org.dswarm.converter.DMPConverterException;
import org.dswarm.init.util.DMPStatics;
import org.dswarm.persistence.model.schema.MappingAttributePathInstance;
import scala.util.parsing.json.JSONObject;

/**
 * Creates a metamorph script from a given {@link Task}.
 *
 * @author phorn
 * @author niederl
 * @author tgaengler
 */
public class MorphScriptBuilder {

	private static final Logger LOG = LoggerFactory.getLogger(MorphScriptBuilder.class);

	private static final String MAPPING_PREFIX = "mapping";

	private static final DocumentBuilderFactory DOC_FACTORY = DocumentBuilderFactory.newInstance();

	private static final String SCHEMA_PATH = "schemata/metamorph.xsd";

	private static final TransformerFactory TRANSFORMER_FACTORY;

	private static final String TRANSFORMER_FACTORY_CLASS = "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl";

	private static final String INPUT_VARIABLE_IDENTIFIER = "inputString";

	private static final String OUTPUT_VARIABLE_PREFIX_IDENTIFIER = "__TRANSFORMATION_OUTPUT_VARIABLE__";

	private static final String FILTER_VARIABLE_POSTFIX = ".filtered";

	private static final String OCCURRENCE_VARIABLE_POSTFIX = ".occurrence";

	private static final Set<String> LOOKUP_FUNCTIONS = new HashSet<String>(Arrays.asList(new String[] {"lookup","setreplace","blacklist","whitelist"}));

	private static final String LOOKUP_MAP_DEFINITION = "lookupString";

	static {
		System.setProperty("javax.xml.transform.TransformerFactory", MorphScriptBuilder.TRANSFORMER_FACTORY_CLASS);
		TRANSFORMER_FACTORY = TransformerFactory.newInstance();
		MorphScriptBuilder.TRANSFORMER_FACTORY.setAttribute("indent-number", 4);

		final URL resource = Resources.getResource(MorphScriptBuilder.SCHEMA_PATH);
		final CharSource inputStreamInputSupplier = Resources.asCharSource(resource, Charsets.UTF_8);

		try (final Reader schemaStream = inputStreamInputSupplier.openStream()) {

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

	private Document doc;

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

		final Element maps = doc.createElement("maps");
		rootElement.appendChild(maps);

		final List<String> metas = Lists.newArrayList();

		for (final Mapping mapping : task.getJob().getMappings()) {
			metas.add(MorphScriptBuilder.MAPPING_PREFIX + mapping.getUuid());

			createTransformation(rules, mapping);

			createLookupTable(maps, mapping.getTransformation());
		}

		metaName.setTextContent(Joiner.on(", ").join(metas));

		return this;
	}

	private void createTransformation(final Element rules, final Mapping mapping) throws DMPConverterException {

		// first handle the parameter mapping from the attribute paths of the mapping to the transformation component

		final Component transformationComponent = mapping.getTransformation();

		if (transformationComponent == null) {

			MorphScriptBuilder.LOG.debug("transformation component for mapping '" + mapping.getUuid() + "' was empty. Just delegate mapping input to mapping output.");

			// just delegate input attribute path to output attribute path

			mapMappingInputToMappingOutput(mapping, rules);

			return;
		}

		if (transformationComponent.getParameterMappings() == null || transformationComponent.getParameterMappings().isEmpty()) {

			MorphScriptBuilder.LOG.debug("parameter mappings for transformation component shouldn't be empty, mapping: '" + mapping.getUuid() + "'. Delegate mapping input to mapping output and process transformation.");

			// delegate input attribute path to output attribute path + add possible transformations (components)

			mapMappingInputToMappingOutput(mapping, rules);
			processTransformationComponentFunction(transformationComponent, mapping, null, rules);

			return;
		}

		// get all input attribute paths and create datas for them

		final Set<MappingAttributePathInstance> mappingInputs = mapping.getInputAttributePaths();

		final Map<String, List<String>> mappingInputsVariablesMap = Maps.newLinkedHashMap();

		for (final MappingAttributePathInstance mappingInput : mappingInputs) {

			final String mappingInputName = mappingInput.getName();

			final List<String> variablesFromMappingInput = getParameterMappingKeys(mappingInputName, transformationComponent);

			final Integer ordinal = mappingInput.getOrdinal();

			final String filterExpressionStringUnescaped = getFilterExpression(mappingInput);

			addMappingInputsVars(variablesFromMappingInput, mappingInput, rules, mappingInputsVariablesMap, filterExpressionStringUnescaped, ordinal);
		}

		final String mappingOutputName = mapping.getOutputAttributePath().getName();

		final List<String> variablesFromMappingOutput = getParameterMappingKeys(mappingOutputName, transformationComponent);

		addMappingOutputMapping(variablesFromMappingOutput, mapping.getOutputAttributePath(), rules);

		processTransformationComponentFunction(transformationComponent, mapping, mappingInputsVariablesMap, rules);
	}

	private void createLookupTable(final Element maps, final Component transformationComponent) throws DMPConverterException {

		if (transformationComponent == null)
			return;

		final Function transformationFunction = transformationComponent.getFunction();

		if (transformationFunction != null && transformationFunction.getFunctionType() == FunctionType.Transformation) {

			final Transformation transformation = (Transformation) transformationFunction;

			final Set<Component> components = transformation.getComponents();

			if (components == null)
				return;

			for (final Component component : components) {

				final Map<String, String> componentParameterMapping = component.getParameterMappings();

				if (LOOKUP_FUNCTIONS.contains(component.getFunction().getName()) && componentParameterMapping != null) {

					final Element map = doc.createElement("map");
					maps.appendChild(map);

					for (final Entry<String, String> parameterMapping : componentParameterMapping.entrySet()) {

						if (parameterMapping.getKey() != null && parameterMapping.getValue() != null) {

							switch (parameterMapping.getKey()) {

								case "in":

									map.setAttribute("name", parameterMapping.getValue());
									break;

								case "map":

									map.setAttribute("name", parameterMapping.getValue());
									break;

								case LOOKUP_MAP_DEFINITION:


									if (component.getFunction().getName().equals("whitelist") || component.getFunction().getName().equals("blacklist")) {

										List<String> lookupList = new ArrayList();


										ObjectMapper mapper = new ObjectMapper();

										try {
											lookupList = mapper.readValue(parameterMapping.getValue(), new TypeReference<List<String>>() {
											});
										} catch (final IOException e) {
											MorphScriptBuilder.LOG.debug("lookup map as JSON string in parameter mappings could not convert to a list" + e);
										}

										for (final String lookupEntry : lookupList) {

											final Element lookup = doc.createElement("entry");
											lookup.setAttribute("name", lookupEntry);
											map.appendChild(lookup);
										}
									} else {

										Map<String, String> lookupEntrys = new HashMap<String, String>();

										ObjectMapper mapper = new ObjectMapper();

										try {
											lookupEntrys = mapper.readValue(parameterMapping.getValue(), new TypeReference<HashMap<String, String>>() {
											});
										} catch (final IOException e) {
											MorphScriptBuilder.LOG.debug("lookup map as JSON string in parameter mappings could not convert to a map" + e);
										}

										for (final Entry<String, String> lookupEntry : lookupEntrys.entrySet()) {

											final Element lookup = doc.createElement("entry");
											lookup.setAttribute("name", lookupEntry.getKey());
											lookup.setAttribute("value", lookupEntry.getValue());
											map.appendChild(lookup);
										}
									}
									break;

							}

						}
					}
				}
			}
		}
	}

	private void createParameters(final Map<String, String> parameterMappings, final Element component) {

		// TODO: parse parameter values that can be simple string values, JSON objects or JSON arrays (?)
		// => for now we expect only simple string values

		if (parameterMappings != null) {

			for (final Entry<String, String> parameterMapping : parameterMappings.entrySet()) {

				if (parameterMapping.getKey() != null) {

					if (parameterMapping.getKey().equals(MorphScriptBuilder.INPUT_VARIABLE_IDENTIFIER) ||
						parameterMapping.getKey().equals(MorphScriptBuilder.LOOKUP_MAP_DEFINITION)) {

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

		// convert concat function to combine function because concat are concatenated the
		// values in the order they appear in the input and not in the order of the <data> sources.
		if (multipleInputComponent.getFunction().getName().equals("concat")) {

			final Map<String, String> parameters = multipleInputComponent.getParameterMappings();

			String valueString = "";

			String delimiterString = ", ";

			if (parameters.get("delimiter") != null) {
				delimiterString = parameters.get("delimiter");
			}

			if (parameters.get("prefix") != null) {
				valueString = parameters.get("prefix");
			}

			final Iterator<String> iter = collectionSourceAttributes.iterator();

			int i = 0;

			while (iter.hasNext()) {

				final String sourceAttribute = iter.next();

				valueString += "${" + sourceAttribute + "}";

				if ((i++ + 1) < collectionSourceAttributes.size()) {
					valueString += delimiterString;
				}

			}

			if (parameters.get("postfix") != null) {
				valueString += parameters.get("postfix");
			}

			Map<String, String> extendedParameterMappings = new HashMap<String, String>();

			extendedParameterMappings.put("value", valueString);

			extendedParameterMappings.put("reset", "true");

			multipleInputComponent.setParameterMappings(extendedParameterMappings);
		}

		final String functionName = multipleInputComponent.getFunction().getName();

		if (functionName.equals("concat"))
			collection = doc.createElement("combine");
		else
			collection = doc.createElement(multipleInputComponent.getFunction().getName());

		createParameters(multipleInputComponent.getParameterMappings(), collection);

		collection.setAttribute("name", "@" + collectionNameAttribute);

		for (final String sourceAttribute : collectionSourceAttributes) {

			final Element collectionData = doc.createElement("data");

			collectionData.setAttribute("source", "@" + sourceAttribute);

			collectionData.setAttribute("name", sourceAttribute);

			collection.appendChild(collectionData);
		}

		return collection;
	}

	private List<String> getParameterMappingKeys(final String attributePathInstanceName, final Component transformationComponent) {

		List<String> parameterMappingKeys = null;

		final Map<String, String> transformationParameterMapping = transformationComponent.getParameterMappings();

		for (final Entry<String, String> parameterMapping : transformationParameterMapping.entrySet()) {

			if (StringEscapeUtils.unescapeXml(parameterMapping.getValue()).equals(attributePathInstanceName)) {

				if (parameterMappingKeys == null) {

					parameterMappingKeys = Lists.newArrayList();
				}

				parameterMappingKeys.add(parameterMapping.getKey());
			}
		}

		return parameterMappingKeys;
	}

	private void addMappingInputsVars(final List<String> variables, final MappingAttributePathInstance mappingInput, final Element rules,
			final Map<String, List<String>> mappingInputsVariablesMap, final String filterExpressionString,
			final Integer ordinal) {

		if (variables == null || variables.isEmpty()) {

			return;
		}

		final String inputAttributePathStringXMLEscaped = StringEscapeUtils.escapeXml(mappingInput.getAttributePath().toAttributePath());

		final String manipulatedVariable;

		if (checkOrdinal(ordinal)) {

			manipulatedVariable = addOrdinalFilter(ordinal, mappingInput.getName(), rules);
		} else {

			manipulatedVariable = mappingInput.getName();
		}

		final Map<String, String> filterExpressionMap = extractFilterExpressions(filterExpressionString);

		if (filterExpressionMap == null || filterExpressionMap.isEmpty()) {

			final Element data = doc.createElement("data");
			data.setAttribute("source", inputAttributePathStringXMLEscaped);

			data.setAttribute("name", "@" + mappingInput.getName());

			rules.appendChild(data);
		} else {

			addFilter(inputAttributePathStringXMLEscaped, manipulatedVariable, filterExpressionMap, rules);
		}

		mappingInputsVariablesMap.put(inputAttributePathStringXMLEscaped, variables);

	}

	private void addMappingOutputMapping(final List<String> variables, final MappingAttributePathInstance mappingOutput, final Element rules) {

		if (variables == null || variables.isEmpty()) {

			LOG.debug("there are no variables to map the mapping output");

			return;
		}

		// .ESCAPE_XML11.with(NumericEntityEscaper.between(0x7f, Integer.MAX_VALUE)).translate( <- also doesn't work
		final String outputAttributePathStringXMLEscaped = StringEscapeUtils.escapeXml(mappingOutput.getAttributePath().toAttributePath());

		// TODO: maybe add mapping to default output variable identifier, if output attribute path is not part of the parameter
		// mappings of the transformation component
		// maybe for later: separate parameter mapppings into input parameter mappings and output parameter mappings

		for (final String variable : variables) {

			if (!variable.startsWith(MorphScriptBuilder.OUTPUT_VARIABLE_PREFIX_IDENTIFIER)) {

				continue;
			}

			final Element dataOutput = doc.createElement("data");
			dataOutput.setAttribute("source", "@" + variable);
			dataOutput.setAttribute("name", outputAttributePathStringXMLEscaped);
			rules.appendChild(dataOutput);
		}
	}

	private void mapMappingInputToMappingOutput(final Mapping mapping, final Element rules) {

		final Set<MappingAttributePathInstance> inputMappingAttributePathInstances = mapping.getInputAttributePaths();

		if (inputMappingAttributePathInstances == null || inputMappingAttributePathInstances.isEmpty()) {

			LOG.debug("there are no mapping inputs for mapping '" + mapping.getName() + "'");

			return;
		}

		final MappingAttributePathInstance outputMappingAttributePathInstance = mapping.getOutputAttributePath();

		if (outputMappingAttributePathInstance == null) {

			LOG.debug("there is no mapping output for mapping '" + mapping.getName() + "'");

			return;
		}

		final MappingAttributePathInstance inputMappingAttributePathInstance = inputMappingAttributePathInstances.iterator().next();
		final String inputAttributePathStringXMLEscaped = StringEscapeUtils.escapeXml(inputMappingAttributePathInstance.getAttributePath()
				.toAttributePath());
		final String filterExpression = getFilterExpression(inputMappingAttributePathInstance);
		final Integer ordinal = inputMappingAttributePathInstance.getOrdinal();

		final String inputVariable;
		final boolean isOrdinalValid = checkOrdinal(ordinal);
		final Map<String, String> filterExpressionMap = extractFilterExpressions(filterExpression);

		String var1000 = "var1000";
		boolean takeVariable = false;

		if (isOrdinalValid) {

			var1000 = addOrdinalFilter(ordinal, var1000, rules);
			takeVariable = true;
		}

		if (filterExpressionMap != null && !filterExpressionMap.isEmpty()) {

			addFilter(inputAttributePathStringXMLEscaped, var1000, filterExpressionMap, rules);
			takeVariable = true;
		}

		if (!takeVariable) {

			inputVariable = inputAttributePathStringXMLEscaped;
		} else {

			inputVariable = "@" + var1000;
		}

		final Element data = doc.createElement("data");
		data.setAttribute("source", inputVariable);

		data.setAttribute("name", StringEscapeUtils.escapeXml(outputMappingAttributePathInstance.getAttributePath().toAttributePath()));

		rules.appendChild(data);
	}

	private void processTransformationComponentFunction(final Component transformationComponent, final Mapping mapping,
			final Map<String, List<String>> mappingInputsVariablesMap, final Element rules) throws DMPConverterException {

		final String transformationOutputVariableIdentifier = determineTransformationOutputVariable(transformationComponent);
		final String finalTransformationOutputVariableIdentifier =
				transformationOutputVariableIdentifier == null ? MorphScriptBuilder.OUTPUT_VARIABLE_PREFIX_IDENTIFIER
						: transformationOutputVariableIdentifier;

		final Function transformationFunction = transformationComponent.getFunction();

		if (transformationFunction == null) {

			MorphScriptBuilder.LOG.debug("transformation component's function for mapping '" + mapping.getUuid() + "' was empty");

			// nothing to do - mapping from input attribute path to output attribute path should be fine already

			return;
		}

		switch (transformationFunction.getFunctionType()) {

			case Function:

				// TODO: process simple function

				MorphScriptBuilder.LOG.error("transformation component's function for mapping '" + mapping.getUuid()
						+ "' was a real FUNCTION. this is not supported right now.");

				break;

			case Transformation:

				// TODO: process simple input -> output mapping (?)

				final Transformation transformation = (Transformation) transformationFunction;

				final Set<Component> components = transformation.getComponents();

				if (components == null || components.isEmpty()) {

					MorphScriptBuilder.LOG.debug("transformation component's transformation's components for mapping '" + mapping.getUuid()
							+ "' are empty");

					if (mappingInputsVariablesMap != null && !mappingInputsVariablesMap.isEmpty()) {

						// map input attribute path variable to output attribute path variable

						final String dataSourceAttribute = mappingInputsVariablesMap.entrySet().iterator().next().getValue().iterator().next();

						final Element data = doc.createElement("data");

						data.setAttribute("source", "@" + dataSourceAttribute);

						data.setAttribute("name", "@" + transformationOutputVariableIdentifier);

						rules.appendChild(data);
					}

					return;
				}

				for (final Component component : components) {

					processComponent(component, mappingInputsVariablesMap, finalTransformationOutputVariableIdentifier, rules);
				}

				break;
		}
	}

	private void processComponent(final Component component, final Map<String, List<String>> mappingInputsVariablesMap,
			final String transformationOutputVariableIdentifier, final Element rules) throws DMPConverterException {

		String[] inputStrings = { };

		final Map<String, String> componentParameterMapping = component.getParameterMappings();

		if (componentParameterMapping != null) {

			for (final Entry<String, String> parameterMapping : componentParameterMapping.entrySet()) {

				if (parameterMapping.getKey().equals(MorphScriptBuilder.INPUT_VARIABLE_IDENTIFIER)) {

					inputStrings = parameterMapping.getValue().split(",");

					break;
				}
			}
		}

		// this is a list of input variable names related to current component, which should be unique and ordered
		final Set<String> sourceAttributes = new LinkedHashSet<>();

		Collections.addAll(sourceAttributes, inputStrings);

		// if no inputString is set, take input component name
		if (sourceAttributes.isEmpty() && component.getInputComponents() != null && !component.getInputComponents().isEmpty()) {

			for (final Component inputComponent : component.getInputComponents()) {

				sourceAttributes.add(getComponentName(inputComponent));

			}
		}
		// or input attribute path variable
		else if (sourceAttributes.isEmpty()) {

			// take input attribute path variable

			if (mappingInputsVariablesMap != null && !mappingInputsVariablesMap.isEmpty()) {

				sourceAttributes.add(mappingInputsVariablesMap.entrySet().iterator().next().getValue().iterator().next());
			}
		}

		if (sourceAttributes.isEmpty()) {

			// couldn't identify an input variable or an input attribute path

			MorphScriptBuilder.LOG.debug("couldn't identify any input variable or mapping input");

			return;
		}

		if (sourceAttributes.size() > 1) {

			// TODO: [@tgaengler] multiple identified input variables doesn't really mean that the component refers to a
			// collection, or?

			final String collectionNameAttribute;

			if (component.getOutputComponents() == null || component.getOutputComponents().isEmpty()) {

				// the end has been reached

				collectionNameAttribute = transformationOutputVariableIdentifier;
			} else {

				collectionNameAttribute = getComponentName(component);
			}

			final Element collection = createCollectionTag(component, collectionNameAttribute, sourceAttributes);

			rules.appendChild(collection);

			return;
		}

		final String dataNameAttribute;

		if (component.getOutputComponents() == null || component.getOutputComponents().isEmpty()) {

			dataNameAttribute = transformationOutputVariableIdentifier;
		} else {

			dataNameAttribute = getComponentName(component);
		}

		final Element data = createDataTag(component, dataNameAttribute, sourceAttributes.iterator().next());

		rules.appendChild(data);
	}

	private String getComponentName(final Component component) throws DMPConverterException {

		final String componentName = component.getName();

		if (componentName != null && !componentName.isEmpty()) {

			return componentName;
		} else {

			MorphScriptBuilder.LOG.error("component name (an id assigned by frontend) doesn't exist");

			throw new DMPConverterException("component name doesn't exist");
		}

	}

	private String determineTransformationOutputVariable(final Component transformationComponent) {

		if (transformationComponent == null) {

			MorphScriptBuilder.LOG.error("transformation component is null, couldn't identify transformation output variable identifier");

			return null;
		}

		final Map<String, String> parameterMappings = transformationComponent.getParameterMappings();

		if (parameterMappings == null) {

			MorphScriptBuilder.LOG
					.error("transformation component parameter mappings are null, couldn't identify transformation output variable identifier");

			return null;
		}

		if (parameterMappings.isEmpty()) {

			MorphScriptBuilder.LOG
					.error("transformation component parameter mappings are empty, couldn't identify transformation output variable identifier");

			return null;
		}

		for (final String key : parameterMappings.keySet()) {

			if (key.startsWith(MorphScriptBuilder.OUTPUT_VARIABLE_PREFIX_IDENTIFIER)) {

				// found output variable identifier

				return key;
			}
		}

		MorphScriptBuilder.LOG.error("couldn't find transformation output variable identifier");

		return null;
	}

	private String getFilterExpression(final MappingAttributePathInstance mappingAttributePathInstance) {

		if (mappingAttributePathInstance.getFilter() != null) {

			final String filterExpressionString = mappingAttributePathInstance.getFilter().getExpression();

			if (filterExpressionString != null && !filterExpressionString.isEmpty()) {

				return StringEscapeUtils.unescapeXml(filterExpressionString);
			}
		}

		return null;
	}

	private boolean checkOrdinal(final Integer ordinal) {

		return ordinal != null && ordinal > 0;

	}

	private String addOrdinalFilter(final Integer ordinal, final String variable, final Element rules) {

		final Element occurrenceData = doc.createElement("data");

		occurrenceData.setAttribute("name", "@" + variable);

		final String manipulatedVariable = variable + MorphScriptBuilder.OCCURRENCE_VARIABLE_POSTFIX;

		occurrenceData.setAttribute("source", "@" + manipulatedVariable);

		final Element occurrenceFunction = doc.createElement("occurrence");

		occurrenceFunction.setAttribute("only", String.valueOf(ordinal));

		occurrenceData.appendChild(occurrenceFunction);

		rules.appendChild(occurrenceData);

		return manipulatedVariable;
	}

	private void addFilter(final String inputAttributePathStringXMLEscaped, final String variable, final Map<String, String> filterExpressionMap,
			final Element rules) {

		final Element combineAsFilter = doc.createElement("combine");
		combineAsFilter.setAttribute("reset", "true");
		combineAsFilter.setAttribute("sameEntity", "true");
		combineAsFilter.setAttribute("includeSubEntities", "true");
		combineAsFilter.setAttribute("name", "@" + variable);
		combineAsFilter.setAttribute("value", "${" + variable + MorphScriptBuilder.FILTER_VARIABLE_POSTFIX + "}");

		final String commonAttributePath = validateCommonAttributePath(inputAttributePathStringXMLEscaped, filterExpressionMap.keySet());

		combineAsFilter.setAttribute("flushWith", commonAttributePath);

		final Element filterIf = doc.createElement("if");
		final Element filterAll = doc.createElement("all");
		filterAll.setAttribute("name", "CONDITION_ALL");
		filterAll.setAttribute("reset", "true");
		filterAll.setAttribute("includeSubEntities", "true");
		filterAll.setAttribute("flushWith", StringEscapeUtils.unescapeXml(Iterators.getLast(filterExpressionMap.keySet().iterator())));

		for (final Entry<String, String> filter : filterExpressionMap.entrySet()) {

			final Element combineAsFilterData = doc.createElement("data");
			combineAsFilterData.setAttribute("source", StringEscapeUtils.unescapeXml(filter.getKey()));

			final Element combineAsFilterDataFunction = doc.createElement("regexp");
			combineAsFilterDataFunction.setAttribute("match", filter.getValue());

			combineAsFilterData.appendChild(combineAsFilterDataFunction);
			filterAll.appendChild(combineAsFilterData);
		}

		filterIf.appendChild(filterAll);
		combineAsFilter.appendChild(filterIf);

		final Element combineAsFilterDataOut = doc.createElement("data");
		combineAsFilterDataOut.setAttribute("name", variable + MorphScriptBuilder.FILTER_VARIABLE_POSTFIX);
		combineAsFilterDataOut.setAttribute("source", inputAttributePathStringXMLEscaped);

		combineAsFilter.appendChild(combineAsFilterDataOut);

		rules.appendChild(combineAsFilter);
	}

	private Map<String, String> extractFilterExpressions(final String filterExpressionString) {

		final Map<String, String> filterExpressionMap = Maps.newLinkedHashMap();

		final ObjectMapper objectMapper = new ObjectMapper();

		if (filterExpressionString != null && !filterExpressionString.isEmpty()) {

			ArrayNode filterExpressionArray = null;

			try {

				filterExpressionArray = objectMapper.readValue(filterExpressionString, ArrayNode.class);

			} catch (final IOException e) {

				MorphScriptBuilder.LOG.debug("something went wrong while deserializing filter expression" + e);
			}

			if (filterExpressionArray != null) {

				for (final JsonNode filterExpressionNode : filterExpressionArray) {

					final Iterator<Entry<String, JsonNode>> filterExpressionIter = filterExpressionNode.fields();

					while (filterExpressionIter.hasNext()) {

						final Entry<String, JsonNode> filterExpressionEntry = filterExpressionIter.next();
						filterExpressionMap.put(filterExpressionEntry.getKey(), filterExpressionEntry.getValue().asText());
					}
				}
			}
		}

		return filterExpressionMap;
	}

	private String determineCommonAttributePath(final String valueAttributePath, final Set<String> filterAttributePaths) {

		final String[] attributePaths = new String[filterAttributePaths.size() + 1];

		attributePaths[0] = valueAttributePath;

		int i = 1;

		for (final String filterAttributePath : filterAttributePaths) {

			attributePaths[i] = StringEscapeUtils.unescapeXml(filterAttributePath);

			i++;
		}

		final String commonPrefix = StringUtils.getCommonPrefix(attributePaths);

		if (!commonPrefix.endsWith(DMPStatics.ATTRIBUTE_DELIMITER.toString())) {

			if (!commonPrefix.contains(DMPStatics.ATTRIBUTE_DELIMITER.toString())) {

				return commonPrefix;
			}

			return commonPrefix.substring(0, commonPrefix.lastIndexOf(DMPStatics.ATTRIBUTE_DELIMITER));
		}

		return commonPrefix.substring(0, commonPrefix.length() - 1);
	}

	private String validateCommonAttributePath(final String valueAttributePath, final Set<String> filterAttributePaths) {

		final String commonAttributePath = determineCommonAttributePath(valueAttributePath, filterAttributePaths);

		if (commonAttributePath == null || commonAttributePath.isEmpty()) {

			// to flush at record level
			return "record";
		}

		final String[] commonAttributePathAttributes = determineAttributes(commonAttributePath);
		final String[] valueAttributePathAttributes = determineAttributes(valueAttributePath);

		boolean isValid = true;

		for (int i = 0; i < commonAttributePathAttributes.length; i++) {

			final String commonAttributePathAttribute = commonAttributePathAttributes[i];
			final String valueAttributePathAttribute = valueAttributePathAttributes[i];

			if (!commonAttributePathAttribute.equals(valueAttributePathAttribute)) {

				isValid = false;

				break;
			}
		}

		if (isValid) {

			return commonAttributePath;
		} else {

			// to flush at record level
			return "record";
		}
	}

	private String[] determineAttributes(final String attributePath) {

		final String[] attributes;

		if (attributePath.contains(DMPStatics.ATTRIBUTE_DELIMITER.toString())) {

			attributes = attributePath.split(DMPStatics.ATTRIBUTE_DELIMITER.toString());
		} else {

			attributes = new String[] { attributePath };
		}

		return attributes;
	}
}
