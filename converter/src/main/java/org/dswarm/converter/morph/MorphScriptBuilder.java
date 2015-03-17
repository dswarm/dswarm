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

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringEscapeUtils;

import org.dswarm.converter.DMPConverterException;
import org.dswarm.persistence.model.job.*;
import org.dswarm.persistence.model.schema.MappingAttributePathInstance;
import org.dswarm.persistence.util.DMPPersistenceUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

/**
 * Creates a metamorph script from a given {@link Task}.
 *
 * @author phorn
 * @author niederl
 * @author tgaengler
 */
public class MorphScriptBuilder extends AbstractMorphScriptBuilder<MorphScriptBuilder> {

	private static final Logger LOG = LoggerFactory.getLogger(MorphScriptBuilder.class);

	private static final String MAPPING_PREFIX = "mapping";

	private static final String METAMORPH_ELEMENT_SINGLE_MAP = "map";

	private static final String METAMORPH_ELEMENT_MAP_ENTRY = "entry";

	private static final String METAMORPH_FUNCTION_WHITELIST = "whitelist";

	private static final String METAMORPH_FUNCTION_BLACKLIST = "blacklist";

	private static final String METAMORPH_FUNCTION_LOOKUP = "lookup";

	private static final String METAMORPH_FUNCTION_SETREPLACE = "setreplace";

	private static final String METAMORPH_FUNCTION_CONCAT = "concat";

	private static final String METAMORPH_FUNCTION_OCCURRENCE = "occurrence";

	private static final String METAMORPH_LOOKUP_ATTRIBUTE_MAP = "map";

	private static final String METAMORPH_LOOKUP_ATTRIBUTE_IN = "in";

	private static final String METAMORPH_MAP_KEY = "name";

	private static final String METAMORPH_MAP_VALUE = "value";

	private static final String METAMORPH_MAP_NAME = "name";

	private static final String INPUT_VARIABLE_IDENTIFIER = "inputString";

	private static final String OUTPUT_VARIABLE_PREFIX_IDENTIFIER = "__TRANSFORMATION_OUTPUT_VARIABLE__";

	private static final String OCCURRENCE_VARIABLE_POSTFIX = ".occurrence";

	private static final Set<String> LOOKUP_FUNCTIONS = new HashSet<>(Arrays.asList(
			new String[] { METAMORPH_FUNCTION_LOOKUP, METAMORPH_FUNCTION_SETREPLACE, METAMORPH_FUNCTION_BLACKLIST, METAMORPH_FUNCTION_WHITELIST }));

	private static final String LOOKUP_MAP_DEFINITION = "lookupString";

	private static final String MF_OCCURRENCE_FUNCTION_ONLY_ATTRIBUTE_IDENTIFIER = "only";

	private static final String MF_CONCAT_FUNCTION_DELIMITER_ATTRIBUTE_IDENTIFIER = "delimiter";

	private static final String MF_CONCAT_FUNCTION_PREFIX_ATTRIBUTE_IDENTIFIER = "prefix";

	private static final String MF_CONCAT_FUNCTION_POSTFIX_ATTRIBUTE_IDENTIFIER = "postfix";

	@Override
	public MorphScriptBuilder apply(final Task task) throws DMPConverterException {

		super.apply(task);

		final List<String> metas = Lists.newArrayList();

		for (final Mapping mapping : task.getJob().getMappings()) {

			metas.add(MorphScriptBuilder.MAPPING_PREFIX + mapping.getUuid());

			createTransformation(rules, mapping);

			createLookupTable(maps, mapping.getTransformation());
		}

		metaName.setTextContent(Joiner.on(", ").join(metas));

		return this;
	}

	@Override protected Element createFilterDataElement(final String variable, final String attributePathString) {

		final Element combineAsFilterDataOut = doc.createElement(METAMORPH_ELEMENT_DATA);
		combineAsFilterDataOut.setAttribute(METAMORPH_DATA_TARGET, variable);
		combineAsFilterDataOut.setAttribute(METAMORPH_DATA_SOURCE, attributePathString);

		return combineAsFilterDataOut;
	}

	private void createTransformation(final Element rules, final Mapping mapping) throws DMPConverterException {

		// first handle the parameter mapping from the attribute paths of the mapping to the transformation component

		final Component transformationComponent = mapping.getTransformation();

		if (transformationComponent == null) {

			MorphScriptBuilder.LOG
					.debug("transformation component for mapping '{}' was empty. Just delegate mapping input to mapping output.", mapping.getUuid());

			// just delegate input attribute path to output attribute path

			mapMappingInputToMappingOutput(mapping, rules);

			return;
		}

		if (transformationComponent.getParameterMappings() == null || transformationComponent.getParameterMappings().isEmpty()) {

			MorphScriptBuilder.LOG
					.debug("parameter mappings for transformation component shouldn't be empty, mapping: '{}'. Delegate mapping input to mapping output and process transformation.",
							mapping.getUuid());

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

		if (transformationFunction != null && transformationFunction.getFunctionType().equals(FunctionType.Transformation)) {

			final Transformation transformation = (Transformation) transformationFunction;

			final Set<Component> components = transformation.getComponents();

			if (components == null)
				return;

			for (final Component component : components) {

				final Map<String, String> componentParameterMapping = component.getParameterMappings();

				if (LOOKUP_FUNCTIONS.contains(component.getFunction().getName()) && componentParameterMapping != null) {

					final Element map = doc.createElement(METAMORPH_ELEMENT_SINGLE_MAP);
					map.setAttribute(METAMORPH_MAP_NAME, component.getName());
					maps.appendChild(map);

					for (final Entry<String, String> parameterMapping : componentParameterMapping.entrySet()) {

						if (parameterMapping.getKey().equals(LOOKUP_MAP_DEFINITION) && parameterMapping.getValue() != null) {

							switch (component.getFunction().getName()) {

								case METAMORPH_FUNCTION_WHITELIST:
								case METAMORPH_FUNCTION_BLACKLIST:

									try {

										final List<String> lookupList = DMPPersistenceUtil.getJSONObjectMapper()
												.readValue(parameterMapping.getValue(),
														new TypeReference<List<String>>() {

														});

										for (final String lookupEntry : lookupList) {

											final Element lookup = doc.createElement(METAMORPH_ELEMENT_MAP_ENTRY);
											lookup.setAttribute(METAMORPH_MAP_KEY, lookupEntry);
											map.appendChild(lookup);
										}
									} catch (final IOException e) {

										MorphScriptBuilder.LOG
												.debug("lookup map as JSON string in parameter mappings could not convert to a list", e);
									}
									break;

								case METAMORPH_FUNCTION_LOOKUP:
								case METAMORPH_FUNCTION_SETREPLACE:

									try {
										//
										final Map<String, String> lookupEntrys = DMPPersistenceUtil.getJSONObjectMapper()
												.readValue(parameterMapping.getValue(),
														new TypeReference<HashMap<String, String>>() {

														});

										for (final Entry<String, String> lookupEntry : lookupEntrys.entrySet()) {

											final Element lookup = doc.createElement(METAMORPH_ELEMENT_MAP_ENTRY);
											lookup.setAttribute(METAMORPH_MAP_KEY, lookupEntry.getKey());
											lookup.setAttribute(METAMORPH_MAP_VALUE, lookupEntry.getValue());
											map.appendChild(lookup);
										}
									} catch (final IOException e) {

										MorphScriptBuilder.LOG
												.debug("lookup map as JSON string in parameter mappings could not convert to a map", e);
									}
									break;
							}
						}
					}
				}
			}
		}
	}

	private void createParameters(final Component component, final Element componentElement) {

		// TODO: parse parameter values that can be simple string values, JSON objects or JSON arrays (?)
		// => for now we expect only simple string values

		final String funtionName = component.getFunction().getName();

		if (LOOKUP_FUNCTIONS.contains(funtionName)) {

			final String lookupNameAttr = funtionName.equals(METAMORPH_FUNCTION_LOOKUP) ? METAMORPH_LOOKUP_ATTRIBUTE_IN
					: METAMORPH_LOOKUP_ATTRIBUTE_MAP;

			final Attr param = doc.createAttribute(lookupNameAttr);
			param.setValue(component.getName());
			componentElement.setAttributeNode(param);
		}

		final Map<String, String> parameterMappings = component.getParameterMappings();

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
						componentElement.setAttributeNode(param);
					}
				}
			}
		}
	}

	private Element createDataTag(final Component singleInputComponent, final String dataNameAttribute, final String dataSourceAttribute) {

		final Element data = doc.createElement(METAMORPH_ELEMENT_DATA);

		data.setAttribute(METAMORPH_DATA_SOURCE, "@" + dataSourceAttribute);

		data.setAttribute(METAMORPH_DATA_TARGET, "@" + dataNameAttribute);

		final Element comp = doc.createElement(singleInputComponent.getFunction().getName());

		createParameters(singleInputComponent, comp);

		data.appendChild(comp);

		return data;
	}

	private Element createCollectionTag(final Component multipleInputComponent, final String collectionNameAttribute,
			final Set<String> collectionSourceAttributes) {

		final Element collection;

		// convert concat function to combine function because concat are concatenated the
		// values in the order they appear in the input and not in the order of the <data> sources.
		if (multipleInputComponent.getFunction().getName().equals(METAMORPH_FUNCTION_CONCAT)) {

			final Map<String, String> parameters = multipleInputComponent.getParameterMappings();

			String valueString;

			final String delimiterString;

			if (parameters.get(MF_CONCAT_FUNCTION_DELIMITER_ATTRIBUTE_IDENTIFIER) != null) {

				delimiterString = parameters.get(MF_CONCAT_FUNCTION_DELIMITER_ATTRIBUTE_IDENTIFIER);
			} else {

				// fallback default
				delimiterString = "";
			}

			if (parameters.get(MF_CONCAT_FUNCTION_PREFIX_ATTRIBUTE_IDENTIFIER) != null) {

				valueString = parameters.get(MF_CONCAT_FUNCTION_PREFIX_ATTRIBUTE_IDENTIFIER);
			} else {

				// fallback default
				valueString = "";
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

			if (parameters.get(MF_CONCAT_FUNCTION_POSTFIX_ATTRIBUTE_IDENTIFIER) != null) {

				valueString += parameters.get(MF_CONCAT_FUNCTION_POSTFIX_ATTRIBUTE_IDENTIFIER);
			}

			Map<String, String> extendedParameterMappings = new HashMap<>();

			extendedParameterMappings.put(MF_ELEMENT_VALUE_ATTRIBUTE_IDENTIFIER, valueString);

			extendedParameterMappings.put(MF_COLLECTOR_RESET_ATTRIBUTE_IDENTIFIER, BOOLEAN_VALUE_TRUE);

			multipleInputComponent.setParameterMappings(extendedParameterMappings);
		}

		final String functionName = multipleInputComponent.getFunction().getName();

		if (functionName.equals(METAMORPH_FUNCTION_CONCAT))
			collection = doc.createElement(METAMORPH_FUNCTION_COMBINE);
		else
			collection = doc.createElement(multipleInputComponent.getFunction().getName());

		createParameters(multipleInputComponent, collection);

		collection.setAttribute(METAMORPH_DATA_TARGET, "@" + collectionNameAttribute);

		for (final String sourceAttribute : collectionSourceAttributes) {

			final Element collectionData = doc.createElement(METAMORPH_ELEMENT_DATA);

			collectionData.setAttribute(METAMORPH_DATA_SOURCE, "@" + sourceAttribute);

			collectionData.setAttribute(METAMORPH_DATA_TARGET, sourceAttribute);

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

			final Element data = doc.createElement(METAMORPH_ELEMENT_DATA);
			data.setAttribute(METAMORPH_DATA_SOURCE, inputAttributePathStringXMLEscaped);

			data.setAttribute(METAMORPH_DATA_TARGET, "@" + mappingInput.getName());

			rules.appendChild(data);
		} else {

			addFilter(inputAttributePathStringXMLEscaped, manipulatedVariable, filterExpressionMap, rules, true);
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

			final Element dataOutput = doc.createElement(METAMORPH_ELEMENT_DATA);
			dataOutput.setAttribute(METAMORPH_DATA_SOURCE, "@" + variable);
			dataOutput.setAttribute(METAMORPH_DATA_TARGET, outputAttributePathStringXMLEscaped);
			rules.appendChild(dataOutput);
		}
	}

	private void mapMappingInputToMappingOutput(final Mapping mapping, final Element rules) {

		final Set<MappingAttributePathInstance> inputMappingAttributePathInstances = mapping.getInputAttributePaths();

		if (inputMappingAttributePathInstances == null || inputMappingAttributePathInstances.isEmpty()) {

			LOG.debug("there are no mapping inputs for mapping '{}'", mapping.getName());

			return;
		}

		final MappingAttributePathInstance outputMappingAttributePathInstance = mapping.getOutputAttributePath();

		if (outputMappingAttributePathInstance == null) {

			LOG.debug("there is no mapping output for mapping '{}'", mapping.getName());

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

			addFilter(inputAttributePathStringXMLEscaped, var1000, filterExpressionMap, rules, true);
			takeVariable = true;
		}

		if (!takeVariable) {

			inputVariable = inputAttributePathStringXMLEscaped;
		} else {

			inputVariable = "@" + var1000;
		}

		final Element data = doc.createElement(METAMORPH_ELEMENT_DATA);
		data.setAttribute(METAMORPH_DATA_SOURCE, inputVariable);

		data.setAttribute(METAMORPH_DATA_TARGET,
				StringEscapeUtils.escapeXml(outputMappingAttributePathInstance.getAttributePath().toAttributePath()));

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

			MorphScriptBuilder.LOG.debug("transformation component's function for mapping '{}' was empty", mapping.getUuid());

			// nothing to do - mapping from input attribute path to output attribute path should be fine already

			return;
		}

		switch (transformationFunction.getFunctionType()) {

			case Function:

				// TODO: process simple function

				MorphScriptBuilder.LOG
						.error("transformation component's function for mapping '{}' was a real FUNCTION. this is not supported right now.",
								mapping.getUuid());

				break;

			case Transformation:

				// TODO: process simple input -> output mapping (?)

				final Transformation transformation = (Transformation) transformationFunction;

				final Set<Component> components = transformation.getComponents();

				if (components == null || components.isEmpty()) {

					MorphScriptBuilder.LOG
							.debug("transformation component's transformation's components for mapping '{}' are empty", mapping.getUuid());

					if (mappingInputsVariablesMap != null && !mappingInputsVariablesMap.isEmpty()) {

						// map input attribute path variable to output attribute path variable

						final String dataSourceAttribute = mappingInputsVariablesMap.entrySet().iterator().next().getValue().iterator().next();

						final Element data = doc.createElement(METAMORPH_ELEMENT_DATA);

						data.setAttribute(METAMORPH_DATA_SOURCE, "@" + dataSourceAttribute);

						data.setAttribute(METAMORPH_DATA_TARGET, "@" + transformationOutputVariableIdentifier);

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

		return getFilterExpression(mappingAttributePathInstance.getFilter());
	}

	private boolean checkOrdinal(final Integer ordinal) {

		return ordinal != null && ordinal > 0;

	}

	private String addOrdinalFilter(final Integer ordinal, final String variable, final Element rules) {

		final Element occurrenceData = doc.createElement(METAMORPH_ELEMENT_DATA);

		occurrenceData.setAttribute(METAMORPH_DATA_TARGET, "@" + variable);

		final String manipulatedVariable = variable + MorphScriptBuilder.OCCURRENCE_VARIABLE_POSTFIX;

		occurrenceData.setAttribute(METAMORPH_DATA_SOURCE, "@" + manipulatedVariable);

		final Element occurrenceFunction = doc.createElement(METAMORPH_FUNCTION_OCCURRENCE);

		occurrenceFunction.setAttribute(MF_OCCURRENCE_FUNCTION_ONLY_ATTRIBUTE_IDENTIFIER, String.valueOf(ordinal));

		occurrenceData.appendChild(occurrenceFunction);

		rules.appendChild(occurrenceData);

		return manipulatedVariable;
	}
}
