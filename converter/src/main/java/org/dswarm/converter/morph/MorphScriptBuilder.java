/**
 * Copyright (C) 2013 – 2017 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Joiner;
import javaslang.Tuple;
import javaslang.Tuple2;
import javaslang.Tuple3;
import javaslang.Tuple4;
import javaslang.Tuple5;
import javaslang.collection.HashSet;
import javaslang.control.Option;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.dswarm.common.DMPStatics;
import org.dswarm.common.xml.utils.XMLUtils;
import org.dswarm.converter.DMPConverterError;
import org.dswarm.converter.DMPConverterException;
import org.dswarm.converter.morph.model.FilterExpression;
import org.dswarm.persistence.model.DMPObject;
import org.dswarm.persistence.model.job.Component;
import org.dswarm.persistence.model.job.Function;
import org.dswarm.persistence.model.job.FunctionType;
import org.dswarm.persistence.model.job.Mapping;
import org.dswarm.persistence.model.job.Task;
import org.dswarm.persistence.model.job.Transformation;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.model.resource.utils.ConfigurationStatics;
import org.dswarm.persistence.model.resource.utils.ConfigurationUtils;
import org.dswarm.persistence.model.schema.AttributePath;
import org.dswarm.persistence.model.schema.ContentSchema;
import org.dswarm.persistence.model.schema.MappingAttributePathInstance;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.util.DMPPersistenceUtil;
import org.dswarm.persistence.util.GDMUtil;

/**
 * Creates a metamorph script from a given {@link Task}.
 *
 * @author phorn
 * @author niederl
 * @author tgaengler
 */
public class MorphScriptBuilder extends AbstractMorphScriptBuilder<MorphScriptBuilder> {

	private static final Logger LOG = LoggerFactory.getLogger(MorphScriptBuilder.class);

	private static final String ATTRIBUTE_DELIMITER = String.valueOf(DMPStatics.ATTRIBUTE_DELIMITER);

	private static final String UNDERSCORE = "_";

	private static final String MAPPING_PREFIX = "mapping";

	private static final String METAMORPH_ELEMENT_SINGLE_MAP = "map";

	private static final String METAMORPH_ELEMENT_MAP_ENTRY = "entry";

	private static final String METAMORPH_FUNCTION_WHITELIST = "whitelist";

	private static final String METAMORPH_FUNCTION_BLACKLIST = "blacklist";

	private static final String METAMORPH_FUNCTION_LOOKUP = "lookup";

	private static final String METAMORPH_FUNCTION_REGEXLOOKUP = "regexlookup";

	private static final String METAMORPH_FUNCTION_SQLMAP = "sqlmap";

	private static final String METAMORPH_FUNCTION_SETREPLACE = "setreplace";

	private static final String METAMORPH_FUNCTION_CONCAT = "concat";

	private static final String DSWARM_FUNCTION_COLLECT = "collect";

	private static final String DSWARM_FUNCTION_MULTI_COLLECT = "multi-collect";

	private static final String DSWARM_FUNCTION_IFELSE = "ifelse";

	private static final String IF_VARIABLE_IDENTIFIER = "if";

	private static final String ELSE_VARIABLE_IDENTIFIER = "else";

	private static final String IF_ELSE_COMPONENT_NAME_PREFIX = "ifelse-component_";

	private static final String IF_BRANCH_POSTFIX = "_if-branch";

	private static final String ELSE_BRANCH_POSTFIX = "_else-branch";

	private static final String METAMORPH_FUNCTION_OCCURRENCE = "occurrence";

	private static final String METAMORPH_LOOKUP_ATTRIBUTE_MAP = "map";

	private static final String METAMORPH_LOOKUP_ATTRIBUTE_IN = "in";

	private static final String METAMORPH_MAP_KEY = "name";

	private static final String METAMORPH_MAP_VALUE = "value";

	private static final String METAMORPH_MAP_NAME = "name";

	private static final String INPUT_VARIABLE_IDENTIFIER = "inputString";

	private static final String OUTPUT_VARIABLE_PREFIX_IDENTIFIER = "__TRANSFORMATION_OUTPUT_VARIABLE__";

	private static final String OCCURRENCE_VARIABLE_POSTFIX = ".occurrence";

	private static final javaslang.collection.Set<String> LOOKUP_FUNCTIONS = HashSet.of(METAMORPH_FUNCTION_LOOKUP,
			METAMORPH_FUNCTION_SETREPLACE,
			METAMORPH_FUNCTION_BLACKLIST,
			METAMORPH_FUNCTION_WHITELIST,
			METAMORPH_FUNCTION_REGEXLOOKUP,
			METAMORPH_FUNCTION_SQLMAP);

	private static final String LOOKUP_MAP_DEFINITION = "lookupString";

	private static final String MF_OCCURRENCE_FUNCTION_ONLY_ATTRIBUTE_IDENTIFIER = "only";

	private static final String MF_CONCAT_FUNCTION_DELIMITER_ATTRIBUTE_IDENTIFIER = "delimiter";

	private static final String MF_CONCAT_FUNCTION_PREFIX_ATTRIBUTE_IDENTIFIER = "prefix";

	private static final String MF_CONCAT_FUNCTION_POSTFIX_ATTRIBUTE_IDENTIFIER = "postfix";

	private static final String DUMMY_VARIABLE_IDENTIFIER = "var1000";

	private static final Map<String, AtomicInteger> mapiVarCounters = new HashMap<>();

	@Override
	public MorphScriptBuilder apply(final Task task) throws DMPConverterException {

		super.apply(task);

		final List<String> metas = new ArrayList<>();

		final Optional<Map<String, Integer>> optionalInputSchemaMap = generateInputSchemaMap(task);
		final Optional<DataModel> optionalInputDataModel = Optional.ofNullable(task)
				.flatMap(task2 -> Optional.ofNullable(task2.getInputDataModel()));
		final Optional<Schema> optionalInputSchema = optionalInputDataModel
				.flatMap(inputDataModel -> Optional.ofNullable(inputDataModel.getSchema()));
		final boolean isXmlSchema = isXmlSchema(optionalInputDataModel);

		final List<Tuple5<Optional<String>, javaslang.collection.List<String>, String, Element, Optional<String>>> mappingOutputs = new ArrayList<>();

		for (final Mapping mapping : task.getJob().getMappings()) {

			metas.add(MorphScriptBuilder.MAPPING_PREFIX + mapping.getUuid());

			final Optional<String> optionalDeepestMappingInput = determineDeepestMappingInputAttributePath(mapping, optionalInputSchemaMap);
			final Optional<Boolean> optionalSelectValueFromSameSubEntity = determineFilterUseCase(mapping, optionalInputSchema);
			final Optional<String> optionalCommonAttributePathOfMappingInputs = determineCommonAttributePathOfMappingInputs(mapping, optionalInputSchema, isXmlSchema);

			createTransformation(rules, mapping, optionalDeepestMappingInput, optionalSelectValueFromSameSubEntity, optionalCommonAttributePathOfMappingInputs)
					.ifPresent(currentMappingOutputs -> currentMappingOutputs
							.forEach(currentMappingOutput -> mappingOutputs.add(Tuple.of(currentMappingOutput._1,
									currentMappingOutput._2,
									currentMappingOutput._3,
									currentMappingOutput._4,
									optionalCommonAttributePathOfMappingInputs))));

			createLookupTable(maps, mapping.getTransformation());
		}

		createMappingOutputs(mappingOutputs, rules, doc);

		metaName.setTextContent(Joiner.on(", ").join(metas));

		return this;
	}

	/**
	 * determines the filter use case, i.e., when multiple mapping inputs are reduced (collected) via a collector (especially all ("and") or any ("or") collector)
	 * whether the select value of the mapping input needs to be taken from the the same sub entity or not.
	 *
	 * @param mapping             the mapping
	 * @param optionalInputSchema the optional input schema
	 * @return true, if the select value should be taken from the same sub entity; otherwise, false (i.e. the mapping inputs filter for different sub entities)
	 */
	private Optional<Boolean> determineFilterUseCase(final Mapping mapping,
	                                                 final Optional<Schema> optionalInputSchema) throws DMPConverterException {

		if (mapping == null) {

			// no mapping available

			return Optional.empty();
		}

		final Set<MappingAttributePathInstance> mappingInputAttributePaths = mapping.getInputAttributePaths();

		if (mappingInputAttributePaths == null || mappingInputAttributePaths.size() < 2) {

			// no mapping inputs available or only one mapping input is available

			return Optional.empty();
		}

		if (!optionalInputSchema.isPresent()) {

			// no input schema available

			return Optional.empty();
		}

		// determine use case:

		final Schema inputSchema = optionalInputSchema.get();
		final ContentSchema inputSchemaContentSchema = inputSchema.getContentSchema();

		// 1. check input schema, whether it contains a content schema

		if (inputSchemaContentSchema == null) {

			// no input content schema available

			return Optional.empty();
		}

		final LinkedList<AttributePath> keyAttributePaths = inputSchemaContentSchema.getKeyAttributePaths();

		// 1.1 if input schema has a content schema, check whether it contains key attribute paths

		if (keyAttributePaths == null || keyAttributePaths.isEmpty()) {

			// no key attribute path available

			return Optional.empty();
		}

		final AttributePath valueAttributePath = inputSchemaContentSchema.getValueAttributePath();

		if (valueAttributePath == null) {

			// no value attribute path available

			return Optional.empty();
		}

		// 1.2 determine number of mapping inputs that make use of the value attribute path of the content schema

		final List<MappingAttributePathInstance> mapiCandidates1 = new ArrayList<>();

		for (final MappingAttributePathInstance mapi : mappingInputAttributePaths) {

			final AttributePath attributePath = mapi.getAttributePath();

			if (valueAttributePath.equals(attributePath)) {

				mapiCandidates1.add(mapi);
			}
		}

		if (mapiCandidates1.size() < 2) {

			// no more then max. 1 mapping input make use of the value attribute path

			return Optional.empty();
		}

		// 1.3 if content schema contains key attribute paths, determine lowest key attribute path (i.e. this one that builds the outer closure of the subentity)

		final Iterator<AttributePath> keyAttributePathsIterator = keyAttributePaths.iterator();
		AttributePath lowestKeyAttributePath = keyAttributePathsIterator.next();

		while (keyAttributePathsIterator.hasNext()) {

			final AttributePath nextKeyAttributePath = keyAttributePathsIterator.next();

			if (nextKeyAttributePath.getAttributePath().size() < lowestKeyAttributePath.getAttributePath().size()) {

				lowestKeyAttributePath = nextKeyAttributePath;
			}
		}

		// 2. determine filters of mapping input candidates
		// 3. check whether filters of mapping intput candidates, make use of lowest key attribute path + value attribute path

		final Map<MappingAttributePathInstance, FilterExpression> mapiCandidates = new LinkedHashMap<>();
		final String lowestKeyAttributePathString = lowestKeyAttributePath.toAttributePath();

		for (final MappingAttributePathInstance mapi : mapiCandidates1) {

			final String filterExpression = getFilterExpression(mapi);
			final Map<String, FilterExpression> filterExpressionMap = extractFilterExpressions(filterExpression);

			if (filterExpressionMap == null || filterExpressionMap.isEmpty()) {

				continue;
			}

			if (!filterExpressionMap.containsKey(lowestKeyAttributePathString)) {

				// filter does not contain lowest key attribute path

				continue;
			}

			mapiCandidates.put(mapi, filterExpressionMap.get(lowestKeyAttributePathString));
		}

		if (mapiCandidates.size() < 2) {

			// there are no more then one mapping input candidate available, cannot do comparison

			return Optional.empty();
		}

		// 3.1 if filters of mapping input candidates make use of lowest key attribute path, then
		//     compare the values of the filter expression for the lowest key attribute path

		boolean selectValueFromSameSubEntity = true;
		final String filterExpressionCompareValue = mapiCandidates.values().iterator().next().getExpression();

		for (final FilterExpression filterExpression : mapiCandidates.values()) {

			if (!filterExpressionCompareValue.equals(filterExpression.getExpression())) {

				// filter expressions for lowest key attribute path differ

				selectValueFromSameSubEntity = false;

				break;
			}
		}

		return Optional.of(selectValueFromSameSubEntity);
	}

	@Override
	protected Tuple2<Optional<Map<String, FilterExpression>>, Optional<FilterExpression>> determineCombineAsFilterDataOutFilter(final Map<String, FilterExpression> filterExpressionMap,
	                                                                                                                            final String inputAttributePathStringXMLEscaped) {

		final Optional<Map<String, FilterExpression>> mappingInputIsUtilisedInFilterExpression = Optional.ofNullable(filterExpressionMap)
				.filter(filterExpressionMap1 -> !filterExpressionMap1.isEmpty())
				.filter(filterExpressionMap2 -> filterExpressionMap2.containsKey(inputAttributePathStringXMLEscaped));

		if (mappingInputIsUtilisedInFilterExpression.isPresent() && filterExpressionMap.size() > 1) {

			final FilterExpression mappingInputFilter = filterExpressionMap.remove(inputAttributePathStringXMLEscaped);

			return Tuple.of(Optional.ofNullable(filterExpressionMap).filter(filterExpressionMap2 -> !filterExpressionMap2.isEmpty()), Optional.of(mappingInputFilter));
		}

		return super.determineCombineAsFilterDataOutFilter(filterExpressionMap, inputAttributePathStringXMLEscaped);
	}

	@Override
	protected Element createFilterDataElement(final String variable,
	                                          final String attributePathString,
	                                          final Optional<FilterExpression> optionalCombineAsFilterDataOutFilter) throws DMPConverterException {

		final Element combineAsFilterDataOut = doc.createElement(METAMORPH_ELEMENT_DATA);
		combineAsFilterDataOut.setAttribute(METAMORPH_DATA_TARGET, variable);
		combineAsFilterDataOut.setAttribute(METAMORPH_DATA_SOURCE, attributePathString);

		if (optionalCombineAsFilterDataOutFilter.isPresent()) {

			FilterExpression combineAsFilterDataOutFilter = optionalCombineAsFilterDataOutFilter.get();

			final Element combineAsFilterDataFunction = createFilterFunction(combineAsFilterDataOutFilter);

			combineAsFilterDataOut.appendChild(combineAsFilterDataFunction);
		}

		return combineAsFilterDataOut;
	}

	/**
	 * _1 = mapping output
	 * _2 = mapping output attributes - last attribute
	 * _3 = last attribute of mapping output
	 * _4 = mapping output 'data' element
	 *
	 * @param rules
	 * @param mapping
	 * @param optionalDeepestMappingInput
	 * @param optionalSelectValueFromSameSubEntity
	 * @param optionalCommonAttributePathOfMappingInputs
	 * @return
	 * @throws DMPConverterException
	 */
	private Optional<List<Tuple4<Optional<String>, javaslang.collection.List<String>, String, Element>>> createTransformation(final Element rules,
	                                                                                                                          final Mapping mapping,
	                                                                                                                          final Optional<String> optionalDeepestMappingInput,
	                                                                                                                          final Optional<Boolean> optionalSelectValueFromSameSubEntity,
	                                                                                                                          final Optional<String> optionalCommonAttributePathOfMappingInputs) throws DMPConverterException {

		// first handle the parameter mapping from the attribute paths of the mapping to the transformation component

		final Component transformationComponent = mapping.getTransformation();

		if (transformationComponent == null) {

			MorphScriptBuilder.LOG
					.debug("transformation component for mapping '{}' was empty. Just delegate mapping input to mapping output.", mapping.getUuid());

			// just delegate input attribute path to output attribute path

			return mapMappingInputToMappingOutput(mapping, rules).map(mappingOutput -> javaslang.collection.List.of(mappingOutput).toJavaList());
		}

		if (transformationComponent.getParameterMappings() == null || transformationComponent.getParameterMappings().isEmpty()) {

			MorphScriptBuilder.LOG
					.debug("parameter mappings for transformation component shouldn't be empty, mapping: '{}'. Delegate mapping input to mapping output and process transformation.",
							mapping.getUuid());

			// delegate input attribute path to output attribute path + add possible transformations (components)

			final Optional<Tuple4<Optional<String>, javaslang.collection.List<String>, String, Element>> optionalMappingOutputTuple = mapMappingInputToMappingOutput(mapping, rules);
			processTransformationComponentFunction(transformationComponent, mapping, null, optionalDeepestMappingInput, optionalSelectValueFromSameSubEntity, optionalCommonAttributePathOfMappingInputs, rules);

			return optionalMappingOutputTuple.map(mappingOutput -> javaslang.collection.List.of(mappingOutput).toJavaList());
		}

		// get all input attribute paths and create datas for them

		final Set<MappingAttributePathInstance> mappingInputs = mapping.getInputAttributePaths();

		final Map<String, List<String>> mappingInputsVariablesMap = new LinkedHashMap<>();

		for (final MappingAttributePathInstance mappingInput : mappingInputs) {

			final String mappingInputName = mappingInput.getName();

			final List<String> variablesFromMappingInput = getParameterMappingKeys(mappingInputName, transformationComponent);

			final Integer ordinal = mappingInput.getOrdinal();

			final String filterExpressionStringUnescaped = getFilterExpression(mappingInput);

			addMappingInputsVars(variablesFromMappingInput, mappingInput, rules, mappingInputsVariablesMap, filterExpressionStringUnescaped, ordinal);
		}

		final String mappingOutputName = mapping.getOutputAttributePath().getName();

		final List<String> variablesFromMappingOutput = getParameterMappingKeys(mappingOutputName, transformationComponent);

		final Optional<List<Tuple4<Optional<String>, javaslang.collection.List<String>, String, Element>>> mappingOutputs = addMappingOutputMapping(variablesFromMappingOutput, mapping.getOutputAttributePath());

		processTransformationComponentFunction(transformationComponent, mapping, mappingInputsVariablesMap, optionalDeepestMappingInput, optionalSelectValueFromSameSubEntity, optionalCommonAttributePathOfMappingInputs, rules);

		return mappingOutputs;
	}

	private void createLookupTable(final Element maps,
	                               final Component transformationComponent) throws DMPConverterException {

		if (transformationComponent == null)
			return;

		final Function transformationFunction = transformationComponent.getFunction();

		if (transformationFunction != null && transformationFunction.getFunctionType().equals(FunctionType.Transformation)) {

			final Transformation transformation = (Transformation) transformationFunction;

			final Set<Component> components = transformation.getComponents();

			if (components == null)
				return;

			for (final Component component : components) {

				final Map<String, String> parameterMappings = component.getParameterMappings();

				final String functionName = component.getFunction().getName();

				if (!LOOKUP_FUNCTIONS.contains(functionName) || parameterMappings == null) {

					continue;
				}

				final String componentName = component.getName();

				switch (functionName) {

					case METAMORPH_FUNCTION_SQLMAP:

						if (parameterMappings.containsKey(INPUT_VARIABLE_IDENTIFIER)) {

							// input variable mapping won't be needed at the processing here

							parameterMappings.remove(INPUT_VARIABLE_IDENTIFIER);
						}

						createSqlMap(functionName, componentName, parameterMappings, maps);

						break;
					default:

						createMap(functionName, componentName, parameterMappings, maps);
				}
			}
		}
	}

	private void createSqlMap(final String functionName,
	                          final String componentName,
	                          final Map<String, String> parameterMappings,
	                          final Element maps) throws DMPConverterException {

		final Element map = doc.createElement(functionName);
		map.setAttribute(METAMORPH_MAP_NAME, componentName);
		maps.appendChild(map);

		if (parameterMappings == null) {

			throw new DMPConverterException("parameter mappings for sqlmap component are not available");
		}

		for (final Entry<String, String> parameterMapping : parameterMappings.entrySet()) {

			if (parameterMapping.getKey() == null) {

				continue;
			}

			if (parameterMapping.getValue() == null) {

				continue;
			}

			final Attr param = doc.createAttribute(parameterMapping.getKey());
			param.setValue(parameterMapping.getValue());
			map.setAttributeNode(param);
		}
	}

	private void createMap(final String functionName,
	                       final String componentName,
	                       final Map<String, String> parameterMappings,
	                       final Element maps) throws DMPConverterException {

		final Element map = doc.createElement(METAMORPH_ELEMENT_SINGLE_MAP);
		map.setAttribute(METAMORPH_MAP_NAME, componentName);
		maps.appendChild(map);

		for (final Entry<String, String> parameterMapping : parameterMappings.entrySet()) {

			final String parameterMappingValue = parameterMapping.getValue();

			if (!parameterMapping.getKey().equals(LOOKUP_MAP_DEFINITION) || parameterMappingValue == null) {

				continue;
			}

			switch (functionName) {

				case METAMORPH_FUNCTION_WHITELIST:
				case METAMORPH_FUNCTION_BLACKLIST:

					try {

						final List<String> lookupList = DMPPersistenceUtil.getJSONObjectMapper()
								.readValue(parameterMappingValue,
										new TypeReference<List<String>>() {

										});

						for (final String lookupEntry : lookupList) {

							final Element lookup = doc.createElement(METAMORPH_ELEMENT_MAP_ENTRY);
							lookup.setAttribute(METAMORPH_MAP_KEY, lookupEntry);
							map.appendChild(lookup);
						}
					} catch (final IOException e) {

						final String message = "lookup map as JSON string in parameter mappings could not convert to a list";

						MorphScriptBuilder.LOG.error(message, e);

						throw new DMPConverterException(message, e);
					}

					break;
				case METAMORPH_FUNCTION_LOOKUP:
				case METAMORPH_FUNCTION_REGEXLOOKUP:
				case METAMORPH_FUNCTION_SETREPLACE:

					try {

						final Map<String, String> lookupEntrys = DMPPersistenceUtil.getJSONObjectMapper()
								.readValue(parameterMappingValue,
										new TypeReference<HashMap<String, String>>() {

										});

						for (final Entry<String, String> lookupEntry : lookupEntrys.entrySet()) {

							final Element lookup = doc.createElement(METAMORPH_ELEMENT_MAP_ENTRY);
							lookup.setAttribute(METAMORPH_MAP_KEY, lookupEntry.getKey());
							lookup.setAttribute(METAMORPH_MAP_VALUE, lookupEntry.getValue());
							map.appendChild(lookup);
						}
					} catch (final IOException e) {

						final String message = "lookup map as JSON string in parameter mappings could not convert to a map";

						MorphScriptBuilder.LOG.error(message, e);

						throw new DMPConverterException(message, e);
					}

					break;
			}
		}
	}

	private void createParameters(final Component component,
	                              final Element componentElement) {

		// TODO: parse parameter values that can be simple string values, JSON objects or JSON arrays (?)
		// => for now we expect only simple string values

		final String funtionName = component.getFunction().getName();

		if (LOOKUP_FUNCTIONS.contains(funtionName)) {

			final boolean isSqlMap = funtionName.equals(METAMORPH_FUNCTION_SQLMAP);

			final String lookupNameAttr =
					funtionName.equals(METAMORPH_FUNCTION_LOOKUP) || isSqlMap ? METAMORPH_LOOKUP_ATTRIBUTE_IN : METAMORPH_LOOKUP_ATTRIBUTE_MAP;

			final Attr param = doc.createAttribute(lookupNameAttr);
			param.setValue(component.getName());
			componentElement.setAttributeNode(param);

			if (isSqlMap) {

				return;
			}
		}

		final Map<String, String> parameterMappings = component.getParameterMappings();

		if (parameterMappings == null) {

			return;
		}

		for (final Entry<String, String> parameterMapping : parameterMappings.entrySet()) {

			if (parameterMapping.getKey() == null) {

				continue;
			}

			if (parameterMapping.getKey().equals(MorphScriptBuilder.INPUT_VARIABLE_IDENTIFIER) ||
					parameterMapping.getKey().equals(MorphScriptBuilder.LOOKUP_MAP_DEFINITION)) {

				continue;
			}

			if (parameterMapping.getValue() == null) {

				continue;
			}

			final Attr param = doc.createAttribute(parameterMapping.getKey());
			param.setValue(parameterMapping.getValue());
			componentElement.setAttributeNode(param);
		}
	}

	private Element createDataTag(final Component singleInputComponent,
	                              final String dataNameAttribute,
	                              final String dataSourceAttribute)
			throws DMPConverterException {

		final Element data = doc.createElement(METAMORPH_ELEMENT_DATA);

		data.setAttribute(METAMORPH_DATA_SOURCE, MF_VARIABLE_PREFIX + dataSourceAttribute);

		data.setAttribute(METAMORPH_DATA_TARGET, MF_VARIABLE_PREFIX + dataNameAttribute);

		final Element comp = doc.createElement(getComponentFunctionName(singleInputComponent));

		createParameters(singleInputComponent, comp);

		data.appendChild(comp);

		return data;
	}

	private static List<String> getParameterMappingKeys(final String variableName,
	                                                    final Component transformationComponent) {

		List<String> parameterMappingKeys = null;

		final Map<String, String> transformationParameterMapping = transformationComponent.getParameterMappings();

		for (final Entry<String, String> parameterMapping : transformationParameterMapping.entrySet()) {

			if (StringEscapeUtils.unescapeXml(parameterMapping.getValue()).equals(variableName)) {

				if (parameterMappingKeys == null) {

					parameterMappingKeys = new ArrayList<>();
				}

				parameterMappingKeys.add(parameterMapping.getKey());
			}
		}

		return parameterMappingKeys;
	}

	private void addMappingInputsVars(final List<String> variables,
	                                  final MappingAttributePathInstance mappingInput,
	                                  final Element rules,
	                                  final Map<String, List<String>> mappingInputsVariablesMap,
	                                  final String filterExpressionString,
	                                  final Integer ordinal) throws DMPConverterException {

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

		final Map<String, FilterExpression> filterExpressionMap = extractFilterExpressions(filterExpressionString);

		if (filterExpressionMap == null || filterExpressionMap.isEmpty()) {

			final Element data = doc.createElement(METAMORPH_ELEMENT_DATA);
			data.setAttribute(METAMORPH_DATA_SOURCE, inputAttributePathStringXMLEscaped);

			data.setAttribute(METAMORPH_DATA_TARGET, MF_VARIABLE_PREFIX + mappingInput.getName());

			rules.appendChild(data);
		} else {

			addFilter(inputAttributePathStringXMLEscaped, manipulatedVariable, filterExpressionMap, rules, true);
		}

		if (!mappingInputsVariablesMap.containsKey(inputAttributePathStringXMLEscaped)) {

			mappingInputsVariablesMap.put(inputAttributePathStringXMLEscaped, new ArrayList<>());
		}

		mappingInputsVariablesMap.get(inputAttributePathStringXMLEscaped).addAll(variables);
	}

	/**
	 * _1 = mapping output
	 * _2 = mapping output attributes - last attribute
	 * _3 = last attribute of mapping output
	 * _4 = mapping output 'data' element
	 *
	 * @param variables
	 * @param mappingOutput
	 * @return
	 */
	private Optional<List<Tuple4<Optional<String>, javaslang.collection.List<String>, String, Element>>> addMappingOutputMapping(final List<String> variables,
	                                                                                                                             final MappingAttributePathInstance mappingOutput) {

		if (variables == null || variables.isEmpty()) {

			LOG.debug("there are no variables to map the mapping output");

			return Optional.empty();
		}

		final Tuple3<Optional<String>, javaslang.collection.List<String>, String> mappingOutputTuple = determineMappingOutputAttribute(mappingOutput).get();

		// TODO: maybe add mapping to default output variable identifier, if output attribute path is not part of the parameter
		// mappings of the transformation component
		// maybe for later: separate parameter mapppings into input parameter mappings and output parameter mappings

		final List<Tuple4<Optional<String>, javaslang.collection.List<String>, String, Element>> mappingOutputTuples = new ArrayList<>();

		for (final String variable : variables) {

			if (!variable.startsWith(MorphScriptBuilder.OUTPUT_VARIABLE_PREFIX_IDENTIFIER)) {

				continue;
			}

			final Element mappingOutputElement = doc.createElement(METAMORPH_ELEMENT_DATA);
			mappingOutputElement.setAttribute(METAMORPH_DATA_SOURCE, MF_VARIABLE_PREFIX + variable);

			mappingOutputTuples.add(Tuple.of(mappingOutputTuple._1, mappingOutputTuple._2, mappingOutputTuple._3, mappingOutputElement));
		}

		if (mappingOutputTuples.isEmpty()) {

			return Optional.empty();
		}

		return Optional.of(mappingOutputTuples);
	}

	/**
	 * _1 = mapping output
	 * _2 = mapping output attributes - last attribute
	 * _3 = last attribute of mapping output
	 * _4 = mapping output 'data' element
	 *
	 * @param mapping
	 * @param rules
	 * @return
	 * @throws DMPConverterException
	 */
	private Optional<Tuple4<Optional<String>, javaslang.collection.List<String>, String, Element>> mapMappingInputToMappingOutput(final Mapping mapping,
	                                                                                                                              final Element rules) throws DMPConverterException {

		final Set<MappingAttributePathInstance> inputMappingAttributePathInstances = mapping.getInputAttributePaths();

		if (inputMappingAttributePathInstances == null || inputMappingAttributePathInstances.isEmpty()) {

			LOG.debug("there are no mapping inputs for mapping '{}'", mapping.getName());

			return Optional.empty();
		}

		final MappingAttributePathInstance mappingOutput = mapping.getOutputAttributePath();

		if (mappingOutput == null) {

			LOG.debug("there is no mapping output for mapping '{}'", mapping.getName());

			return Optional.empty();
		}

		final MappingAttributePathInstance inputMappingAttributePathInstance = inputMappingAttributePathInstances.iterator().next();
		final String inputAttributePathStringXMLEscaped = StringEscapeUtils.escapeXml(inputMappingAttributePathInstance.getAttributePath()
				.toAttributePath());
		final String filterExpression = getFilterExpression(inputMappingAttributePathInstance);
		final Integer ordinal = inputMappingAttributePathInstance.getOrdinal();

		final String inputVariable;
		final boolean isOrdinalValid = checkOrdinal(ordinal);
		final Map<String, FilterExpression> filterExpressionMap = extractFilterExpressions(filterExpression);

		final String mapiIdentifier = getMAPIIdentifier(inputMappingAttributePathInstance);
		final int mapiVarCount = getMAPICount(mapiIdentifier);
		String var1000 = DUMMY_VARIABLE_IDENTIFIER + UNDERSCORE + mapiIdentifier + UNDERSCORE + mapiVarCount;
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

			inputVariable = MF_VARIABLE_PREFIX + var1000;
		}

		final Element mappingOutputElement = doc.createElement(METAMORPH_ELEMENT_DATA);
		mappingOutputElement.setAttribute(METAMORPH_DATA_SOURCE, inputVariable);

		final Tuple3<Optional<String>, javaslang.collection.List<String>, String> mappingOutputTuple = determineMappingOutputAttribute(mappingOutput).get();

		return Optional.of(Tuple.of(mappingOutputTuple._1, mappingOutputTuple._2, mappingOutputTuple._3, mappingOutputElement));
	}

	private void processTransformationComponentFunction(final Component transformationComponent,
	                                                    final Mapping mapping,
	                                                    final Map<String, List<String>> mappingInputsVariablesMap,
	                                                    final Optional<String> optionalDeepestMappingInput,
	                                                    final Optional<Boolean> optionalSelectValueFromSameSubEntity,
	                                                    final Optional<String> optionalCommonAttributePathOfMappingInputs,
	                                                    final Element rules) throws DMPConverterException {

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

						data.setAttribute(METAMORPH_DATA_SOURCE, MF_VARIABLE_PREFIX + dataSourceAttribute);

						data.setAttribute(METAMORPH_DATA_TARGET, MF_VARIABLE_PREFIX + transformationOutputVariableIdentifier);

						rules.appendChild(data);
					}

					return;
				}

				final javaslang.collection.List<Component> sortedComponents = sortComponents(components);

				for (final Component component : sortedComponents) {

					processComponent(component, mappingInputsVariablesMap, finalTransformationOutputVariableIdentifier, optionalDeepestMappingInput, optionalSelectValueFromSameSubEntity, optionalCommonAttributePathOfMappingInputs, rules);
				}

				break;
		}
	}

	private void processComponent(final Component component,
	                              final Map<String, List<String>> mappingInputsVariablesMap,
	                              final String transformationOutputVariableIdentifier,
	                              final Optional<String> optionalDeepestMappingInput,
	                              final Optional<Boolean> optionalSelectValueFromSameSubEntity,
	                              final Optional<String> optionalCommonAttributePathOfMappingInputs,
	                              final Element rules) throws DMPConverterException {

		String[] inputStrings = {};

		final String functionName = component.getFunction().getName();

		final Map<String, String> componentParameterMapping = component.getParameterMappings();

		if (componentParameterMapping != null) {

			for (final Entry<String, String> parameterMapping : componentParameterMapping.entrySet()) {

				if (parameterMapping.getKey().equals(MorphScriptBuilder.INPUT_VARIABLE_IDENTIFIER)) {

					inputStrings = parameterMapping.getValue().split(",");

					break;
				}
			}
		}
		final Set<String> sourceAttributes = determineSourceAttributes(component, mappingInputsVariablesMap, inputStrings);

		if (sourceAttributes.isEmpty()) {

			// couldn't identify an input variable or an input attribute path

			MorphScriptBuilder.LOG.debug("couldn't identify any input variable or mapping input");

			return;
		}

		if (sourceAttributes.size() > 1 || DSWARM_FUNCTION_COLLECT.equals(functionName)) {

			// TODO: [@tgaengler] multiple identified input variables doesn't really mean that the component refers to a
			// collection, or?

			final String collectionNameAttribute;

			if (component.getOutputComponents() == null || component.getOutputComponents().isEmpty()) {

				// the end has been reached

				collectionNameAttribute = transformationOutputVariableIdentifier;
			} else {

				collectionNameAttribute = getComponentName(component);
			}

			final Element collection = createCollectionTag(component, collectionNameAttribute, sourceAttributes, optionalDeepestMappingInput, optionalSelectValueFromSameSubEntity, optionalCommonAttributePathOfMappingInputs);

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

	private static Set<String> determineSourceAttributes(final Component component,
	                                                     final Map<String, List<String>> mappingInputsVariablesMap,
	                                                     final String[] inputStrings)
			throws DMPConverterException {

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

			// take input attribute path variables

			if (mappingInputsVariablesMap != null && !mappingInputsVariablesMap.isEmpty()) {

				final Set<Entry<String, List<String>>> mappingInputVariablesEntries = mappingInputsVariablesMap.entrySet();

				for (final Entry<String, List<String>> mappingInputVariablesEntry : mappingInputVariablesEntries) {

					final List<String> mappingInputVariables = mappingInputVariablesEntry.getValue();

					sourceAttributes.addAll(mappingInputVariables);
				}
			}
		}

		return sourceAttributes;
	}

	private static int determineAttributePathLength(final String attributePath) {

		final String[] attributes = attributePath.split(DMPStatics.ATTRIBUTE_DELIMITER.toString());

		if (attributes.length <= 0) {

			if (attributePath.length() > 0) {

				return 1;
			}

			return 0;
		}

		return attributes.length;
	}

	private static boolean isXmlSchema(Optional<DataModel> optionalInputDataModel) {

		if (!optionalInputDataModel.isPresent()) {

			return false;
		}

		final DataModel dataModel = optionalInputDataModel.get();

		final Optional<String> optionalStorageType = Optional.ofNullable(dataModel.getConfiguration())
				.flatMap(configuration -> Optional.ofNullable(configuration.getParameter(ConfigurationStatics.STORAGE_TYPE)))
				.map(JsonNode::asText);

		if (optionalStorageType.isPresent()
				&& ConfigurationUtils.getXMLStorageTypes().contains(optionalStorageType.get())) {

			return true;
		}

		return false;
	}

	private Element createCollectionTag(final Component multipleInputComponent,
	                                    final String collectionNameAttribute,
	                                    final Set<String> collectionSourceAttributes,
	                                    final Optional<String> optionalDeepestMappingInput,
	                                    final Optional<Boolean> optionalSelectValueFromSameSubEntity,
	                                    final Optional<String> optionalCommonAttributePathOfMappingInputs) throws DMPConverterException {

		final Element collection;

		final String functionName = multipleInputComponent.getFunction().getName();

		switch (functionName) {

			case METAMORPH_FUNCTION_CONCAT:

				convertConcatFunction(multipleInputComponent, collectionSourceAttributes);

				collection = doc.createElement(METAMORPH_FUNCTION_COMBINE);

				return convertCollectionFunction(multipleInputComponent, collectionNameAttribute, collectionSourceAttributes, collection, Optional.empty());
			case DSWARM_FUNCTION_COLLECT:

				final String flushWithEntityForCollect = determineFlushWithEntity(optionalCommonAttributePathOfMappingInputs);

				convertCollectFunction(multipleInputComponent, flushWithEntityForCollect);

				collection = doc.createElement(METAMORPH_FUNCTION_CONCAT);

				return convertCollectionFunction(multipleInputComponent, collectionNameAttribute, collectionSourceAttributes, collection, Optional.empty());
			case DSWARM_FUNCTION_MULTI_COLLECT:

				final String flushWithEntityForCollectMultiCollect = determineFlushWithEntity(optionalCommonAttributePathOfMappingInputs);

				convertCollectFunction(multipleInputComponent, flushWithEntityForCollectMultiCollect);

				collection = doc.createElement(METAMORPH_FUNCTION_CONCAT);

				return convertCollectionFunction(multipleInputComponent, collectionNameAttribute, collectionSourceAttributes, collection, Optional.of(flushWithEntityForCollectMultiCollect));
			case DSWARM_FUNCTION_IFELSE:

				collection = doc.createElement(METAMORPH_FUNCTION_CHOOSE);

				return convertIfElseFunction(multipleInputComponent, collectionNameAttribute, collection);
			case METAMORPH_FUNCTION_ALL:

				collection = doc.createElement(functionName);

				final Element collectionElement = convertCollectionFunction(multipleInputComponent, collectionNameAttribute, collectionSourceAttributes, collection, Optional.empty());

				collectionElement.setAttribute(MF_COLLECTOR_RESET_ATTRIBUTE_IDENTIFIER, BOOLEAN_VALUE_TRUE);

				if (!optionalSelectValueFromSameSubEntity.isPresent() || optionalSelectValueFromSameSubEntity.get()) {

					// 3.2 if the key attribute paths of the filter expressions are all equal, then the select value will be selected from the same sub entity

					if (!optionalDeepestMappingInput.isPresent()) {

						throw new DMPConverterException("deepest mapping input is not available for parametrizing 'all' collector properly");
					}

					final String deepestMappingInputAttributePath = optionalDeepestMappingInput.get();

					collectionElement.setAttribute(MF_FLUSH_WITH_ATTRIBUTE_IDENTIFIER, deepestMappingInputAttributePath);
				} else {
					// 3.3 if the key attribute paths of the filter expressions differ, then the select value will taken from another sub entity

					collectionElement.setAttribute(MF_FLUSH_WITH_ATTRIBUTE_IDENTIFIER, METAFACTURE_RECORD_IDENTIFIER);
					collectionElement.setAttribute(MF_COLLECTOR_INCLUDE_SUB_ENTITIES_ATTRIBUTE_IDENTIFIER, BOOLEAN_VALUE_TRUE);
				}

				return collectionElement;
			default:

				collection = doc.createElement(functionName);

				return convertCollectionFunction(multipleInputComponent, collectionNameAttribute, collectionSourceAttributes, collection, Optional.empty());
		}
	}


	private Element convertCollectionFunction(final Component multipleInputComponent,
	                                          final String collectionNameAttribute,
	                                          final Set<String> collectionSourceAttributes,
	                                          final Element collection,
	                                          final Optional<String> optionalFlushWithEntity) {

		createParameters(multipleInputComponent, collection);

		collection.setAttribute(METAMORPH_DATA_TARGET, MF_VARIABLE_PREFIX + collectionNameAttribute);

		for (final String sourceAttribute : collectionSourceAttributes) {

			createDataElement(collection, sourceAttribute, sourceAttribute, optionalFlushWithEntity);
		}

		return collection;
	}

	private static String getComponentName(final Component component) throws DMPConverterException {

		final String componentName = component.getName();

		if (componentName != null && !componentName.isEmpty()) {

			return componentName;
		} else {

			MorphScriptBuilder.LOG.error("component name (an id assigned by frontend) doesn't exist");

			throw new DMPConverterException("component name doesn't exist");
		}
	}

	private static String getComponentFunctionName(final Component component) {

		final String componentFunctionName = component.getFunction().getName();

		if (METAMORPH_FUNCTION_SQLMAP.equals(componentFunctionName)) {

			return METAMORPH_FUNCTION_LOOKUP;
		}

		return componentFunctionName;
	}

	private static String determineTransformationOutputVariable(final Component transformationComponent) {

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

	private String addOrdinalFilter(final Integer ordinal,
	                                final String variable,
	                                final Element rules) {

		final Element occurrenceData = doc.createElement(METAMORPH_ELEMENT_DATA);

		occurrenceData.setAttribute(METAMORPH_DATA_TARGET, MF_VARIABLE_PREFIX + variable);

		final String manipulatedVariable = variable + MorphScriptBuilder.OCCURRENCE_VARIABLE_POSTFIX;

		occurrenceData.setAttribute(METAMORPH_DATA_SOURCE, MF_VARIABLE_PREFIX + manipulatedVariable);

		final Element occurrenceFunction = doc.createElement(METAMORPH_FUNCTION_OCCURRENCE);

		occurrenceFunction.setAttribute(MF_OCCURRENCE_FUNCTION_ONLY_ATTRIBUTE_IDENTIFIER, String.valueOf(ordinal));

		occurrenceData.appendChild(occurrenceFunction);

		rules.appendChild(occurrenceData);

		return manipulatedVariable;
	}

	private int getMAPICount(final String mapiIdentifier) {

		if (!mapiVarCounters.containsKey(mapiIdentifier)) {

			final AtomicInteger mapiVarCounter = new AtomicInteger(0);

			mapiVarCounters.put(mapiIdentifier, mapiVarCounter);
		}

		return mapiVarCounters.get(mapiIdentifier).getAndIncrement();
	}

	private String getMAPIIdentifier(final MappingAttributePathInstance mapi) throws DMPConverterException {

		final String mapiUuid = mapi.getUuid();

		if (mapiUuid != null && !mapiUuid.trim().isEmpty()) {

			return mapiUuid;
		}

		final String mapiName = mapi.getName();

		if (mapiName != null && !mapiName.trim().isEmpty()) {

			return XMLUtils.getXMLName(mapiName);
		}

		throw new DMPConverterException("couldn't determine MAPI identifier");
	}

	/**
	 * convert concat function to combine function because concat concatenates the
	 * values in the order they appear in the input and not in the order of the <data> sources.
	 *
	 * @param multipleInputComponent
	 * @param collectionSourceAttributes
	 */
	private static void convertConcatFunction(final Component multipleInputComponent,
	                                          final Set<String> collectionSourceAttributes) {

		final Map<String, String> parameters = multipleInputComponent.getParameterMappings();

		final StringBuilder valueStringBuilder = new StringBuilder();

		final String delimiterString;

		if (parameters.get(MF_CONCAT_FUNCTION_DELIMITER_ATTRIBUTE_IDENTIFIER) != null) {

			delimiterString = parameters.get(MF_CONCAT_FUNCTION_DELIMITER_ATTRIBUTE_IDENTIFIER);
		} else {

			// fallback default
			delimiterString = "";
		}

		if (parameters.get(MF_CONCAT_FUNCTION_PREFIX_ATTRIBUTE_IDENTIFIER) != null) {

			valueStringBuilder.append(parameters.get(MF_CONCAT_FUNCTION_PREFIX_ATTRIBUTE_IDENTIFIER));
		} else {

			// fallback default
			valueStringBuilder.append("");
		}

		final Iterator<String> iter = collectionSourceAttributes.iterator();

		int i = 0;

		while (iter.hasNext()) {

			final String sourceAttribute = iter.next();

			valueStringBuilder.append(MF_VALUE_VARIABLE_PREFIX)
					.append(sourceAttribute)
					.append(MF_VALUE_VARIABLE_POSTFIX);

			if ((i++ + 1) < collectionSourceAttributes.size()) {

				valueStringBuilder.append(delimiterString);
			}

		}

		if (parameters.get(MF_CONCAT_FUNCTION_POSTFIX_ATTRIBUTE_IDENTIFIER) != null) {

			valueStringBuilder.append(parameters.get(MF_CONCAT_FUNCTION_POSTFIX_ATTRIBUTE_IDENTIFIER));
		}

		final Map<String, String> extendedParameterMappings = new HashMap<>();
		final String valueString = valueStringBuilder.toString();

		extendedParameterMappings.put(MF_ELEMENT_VALUE_ATTRIBUTE_IDENTIFIER, valueString);

		extendedParameterMappings.put(MF_COLLECTOR_RESET_ATTRIBUTE_IDENTIFIER, BOOLEAN_VALUE_TRUE);

		multipleInputComponent.setParameterMappings(extendedParameterMappings);
	}

	/**
	 * convert collect function to concat function to allow concatenating of field that occur multiple times in a record
	 *
	 * @param multipleInputComponent
	 */
	private static void convertCollectFunction(final Component multipleInputComponent,
	                                           final String flushWithEntity) {


		multipleInputComponent.addParameterMapping(MF_FLUSH_WITH_ATTRIBUTE_IDENTIFIER, flushWithEntity);
		multipleInputComponent.addParameterMapping(MF_COLLECTOR_RESET_ATTRIBUTE_IDENTIFIER, BOOLEAN_VALUE_TRUE);
	}

	private static String determineFlushWithEntity(final Optional<String> optionalCommonAttributePathOfMappingInputs) {

		return optionalCommonAttributePathOfMappingInputs.orElse(METAFACTURE_RECORD_IDENTIFIER);
	}

	private Element convertIfElseFunction(final Component multipleInputComponent,
	                                      final String collectionNameAttribute,
	                                      final Element collection) throws DMPConverterException {

		final Map<String, String> parameters = multipleInputComponent.getParameterMappings();

		if (parameters == null) {

			throw new DMPConverterException("cannot convert if/else component; there are not parameter mappings");
		}

		if (!parameters.containsKey(IF_VARIABLE_IDENTIFIER)) {

			throw new DMPConverterException("cannot convert if/else component; cannot find parameter mapping for if-branch in parameter mappings");
		}

		final String ifBranchComponentVariableName = parameters.get(IF_VARIABLE_IDENTIFIER);

		if (!parameters.containsKey(ELSE_VARIABLE_IDENTIFIER)) {

			throw new DMPConverterException("cannot convert if/else component; cannot find parameter mapping for else-branch in parameter mappings");
		}

		final String elseBranchComponentVariableName = parameters.get(ELSE_VARIABLE_IDENTIFIER);

		final String ifElseComponentID = multipleInputComponent.getUuid();
		final String ifElseComponentName = IF_ELSE_COMPONENT_NAME_PREFIX + ifElseComponentID;

		collection.setAttribute(METAMORPH_DATA_TARGET, MF_VARIABLE_PREFIX + collectionNameAttribute);

		createDataElement(collection, ifBranchComponentVariableName, ifElseComponentName + IF_BRANCH_POSTFIX, Optional.empty());
		createDataElement(collection, elseBranchComponentVariableName, ifElseComponentName + ELSE_BRANCH_POSTFIX, Optional.empty());

		return collection;
	}

	private void createDataElement(final Element collection,
	                               final String sourceAttribute,
	                               final String targetName,
	                               final Optional<String> optionalFlushWithEntity) {

		final Element collectionData = doc.createElement(METAMORPH_ELEMENT_DATA);

		collectionData.setAttribute(METAMORPH_DATA_SOURCE, MF_VARIABLE_PREFIX + sourceAttribute);
		collectionData.setAttribute(METAMORPH_DATA_TARGET, targetName);

		if (optionalFlushWithEntity.isPresent()) {

			final String flushWithEntity = optionalFlushWithEntity.get();

			final Element buffer = doc.createElement(METAMORPH_ELEMENT_BUFFER);

			buffer.setAttribute(MF_FLUSH_WITH_ATTRIBUTE_IDENTIFIER, flushWithEntity);

			collectionData.appendChild(buffer);
		}

		collection.appendChild(collectionData);
	}

	private static Optional<Map<String, Integer>> generateSchemaMap(final Schema schema) {

		final AtomicInteger counter = new AtomicInteger(0);

		return Optional.ofNullable(schema.getAttributePaths())
				.filter(attributePaths -> !attributePaths.isEmpty())
				.map(attributePaths -> attributePaths.stream()
						.filter(schemaAttributePathInstance -> Optional.ofNullable(schemaAttributePathInstance.getAttributePath()).isPresent())
						.filter(schemaAttributePathInstance1 -> Optional.ofNullable(schemaAttributePathInstance1.getAttributePath().toAttributePath()).isPresent())
						.map(schemaAttributePathInstance2 -> Tuple.of(StringEscapeUtils.escapeXml(schemaAttributePathInstance2.getAttributePath().toAttributePath()), counter.getAndIncrement()))
						.collect(Collectors.toMap(Tuple2::_1, Tuple2::_2))
				);
	}

	private static Optional<Map<String, Integer>> generateInputSchemaMap(final Task task) {

		return Optional.ofNullable(task)
				.flatMap(task1 -> Optional.ofNullable(task1.getInputDataModel()))
				.flatMap(inputDataModel -> Optional.ofNullable(inputDataModel.getSchema()))
				.flatMap(MorphScriptBuilder::generateSchemaMap);
	}

	private static Optional<String> determineDeepestMappingInputAttributePath(final Mapping mapping,
	                                                                          final Optional<Map<String, Integer>> optionalInputSchemaMap) {

		return Optional.ofNullable(mapping.getInputAttributePaths())
				.filter(mappingInputAttributePathInstances -> !mappingInputAttributePathInstances.isEmpty())
				.flatMap(mappingInputAttributePathInstances -> {

					final Set<String> uniqueInputAttributePaths = mappingInputAttributePathInstances.stream()
							.map(mappingInputAttributePathInstance -> StringEscapeUtils.escapeXml(mappingInputAttributePathInstance.getAttributePath().toAttributePath()))
							.collect(Collectors.toSet());

					final Optional<String> optionalDeepestMappingInputAttributePath;

					if (!uniqueInputAttributePaths.isEmpty()) {

						if (optionalInputSchemaMap.isPresent()) {

							// note: this algorithm might not be working as expected, i.e., maybe we need to compare the length (number of attributes) of the attribute path as well

							final Map<String, Integer> inputSchemaMap = optionalInputSchemaMap.get();

							int highestOrder = 0;
							String tempDeepestMappingInputAttributePath = uniqueInputAttributePaths.iterator().next();

							for (final String inputAttributePath : uniqueInputAttributePaths) {

								final Integer attributePathOrder = inputSchemaMap.getOrDefault(inputAttributePath, 0);

								if (attributePathOrder > highestOrder) {

									highestOrder = attributePathOrder;
									tempDeepestMappingInputAttributePath = inputAttributePath;
								}
							}

							optionalDeepestMappingInputAttributePath = Optional.of(tempDeepestMappingInputAttributePath);
						} else {

							// try to determine deepest mapping input attribute path without given input schema

							if (uniqueInputAttributePaths.size() == 1) {

								// simply take this one

								optionalDeepestMappingInputAttributePath = Optional.of(uniqueInputAttributePaths.iterator().next());
							} else {

								// take the longest attribute path

								int biggestLength = 0;
								String longestAttributePath = uniqueInputAttributePaths.iterator().next();

								for (final String inputAttributePath : uniqueInputAttributePaths) {

									int attributePathSize = determineAttributePathLength(inputAttributePath);

									if (attributePathSize >= biggestLength) {

										biggestLength = attributePathSize;
										longestAttributePath = inputAttributePath;
									}
								}

								optionalDeepestMappingInputAttributePath = Optional.of(longestAttributePath);
							}
						}
					} else {

						// deepest mapping input attribute path cannot be calculated

						optionalDeepestMappingInputAttributePath = Optional.empty();
					}

					return optionalDeepestMappingInputAttributePath;
				});
	}

	private Optional<String> determineCommonAttributePathOfMappingInputs(final Mapping mapping,
	                                                                     final Optional<Schema> optionalInputSchema,
	                                                                     final boolean isXmlSchema) {

		return Optional.ofNullable(mapping.getInputAttributePaths())
				.filter(mappingInputAttributePathInstances -> !mappingInputAttributePathInstances.isEmpty())
				.flatMap(mappingInputAttributePathInstances -> {

					final Set<String> uniqueInputAttributePaths = mappingInputAttributePathInstances.stream()
							.map(mappingInputAttributePathInstance -> StringEscapeUtils.escapeXml(mappingInputAttributePathInstance.getAttributePath().toAttributePath()))
							.collect(Collectors.toSet());

					if (optionalInputSchema.isPresent()
							&& optionalInputSchema.get().getContentSchema() != null
							&& optionalInputSchema.get().getContentSchema().getValueAttributePath() != null
							&& uniqueInputAttributePaths.size() == 1
							&& optionalInputSchema.get().getContentSchema().getValueAttributePath().toAttributePath().equals(uniqueInputAttributePaths.iterator().next())) {

						// mapping inputs are on value attribute path of content schema

						return Optional.empty();
					}

					// add attribute paths from filters as well
					mappingInputAttributePathInstances.stream()
							.forEach(mappingAttributePathInstance -> Optional.ofNullable(getFilterExpression(mappingAttributePathInstance))
									.ifPresent(filterExpressionString -> {

										try {

											final Map<String, FilterExpression> filterExpressionMap = extractFilterExpressions(filterExpressionString);

											if (filterExpressionMap != null) {

												uniqueInputAttributePaths.addAll(filterExpressionMap.keySet());
											}
										} catch (final DMPConverterException e) {

											throw DMPConverterError.wrap(e);
										}
									}));

					final Set<String> finalUniqueInputAttributePaths = uniqueInputAttributePaths.stream().map(attributePath -> {

						if (isXmlSchema && attributePath.endsWith(GDMUtil.RDF_value)) {

							return attributePath.substring(0, attributePath.length() - GDMUtil.RDF_value.length() - 1);
						}

						return attributePath;
					}).collect(Collectors.toSet());

					final String[] attributePaths = new String[finalUniqueInputAttributePaths.size()];

					return Optional.of(determineCommonAttributePath(finalUniqueInputAttributePaths, attributePaths, 0));
				});
	}

	/**
	 * _1 = mapping output
	 * _2 = mapping output attributes - last attribute
	 * _3 = last attribute of mapping output
	 * _4 = mapping output 'data' element
	 * _5 = optional common attribute path of mapping inputs
	 *
	 * @param mappingOutputs
	 */
	private static void createMappingOutputs(final List<Tuple5<Optional<String>, javaslang.collection.List<String>, String, Element, Optional<String>>> mappingOutputs,
	                                         final Element rules,
	                                         final Document doc) {

		// _1 = mapping output attributes
		// _2 = optional common attribute path of mapping inputs
		final javaslang.collection.List<Tuple2<javaslang.collection.List<String>, Optional<String>>> entityMappingOutputTuples = javaslang.collection.List.ofAll(mappingOutputs)
				.filter(mappingOutputTuple -> mappingOutputTuple._1.isPresent())
				// _1 just for sorting
				.map(mappingOutputTuple -> Tuple.of(mappingOutputTuple._1.get(), mappingOutputTuple._2, mappingOutputTuple._5))
				.sortBy(mappingOutputTuple -> mappingOutputTuple._1)
				.map(mappingOutputTuple -> Tuple.of(mappingOutputTuple._2, mappingOutputTuple._3));

		final Map<String, Element> entityElements = createEntityElements(entityMappingOutputTuples, rules, doc);

		mappingOutputs.forEach(mappingOutputTuple -> {

			final String mappingOutputAttribute = mappingOutputTuple._3;
			final Element mappingOutputElement = mappingOutputTuple._4;

			mappingOutputElement.setAttribute(MF_ELEMENT_NAME_ATTRIBUTE_IDENTIFIER, mappingOutputAttribute);

			final Optional<String> optionalMappintOutputAttributePath = mappingOutputTuple._1;

			if (!optionalMappintOutputAttributePath.isPresent()) {

				rules.appendChild(mappingOutputElement);

				return;
			}

			final String mappingOutputAttributePath = optionalMappintOutputAttributePath.get();

			entityElements.computeIfPresent(mappingOutputAttributePath, (mappingOutputAttributePath1, entityElement) -> {

				entityElement.appendChild(mappingOutputElement);

				return entityElement;
			});
		});
	}

	/**
	 * _1 = mapping output attributes - last attribute
	 * _2 = optional common attribute path of mapping inputs
	 *
	 * @param entityMappingOutputTuples
	 * @param rules
	 * @return
	 */
	private static Map<String, Element> createEntityElements(final javaslang.collection.List<Tuple2<javaslang.collection.List<String>, Optional<String>>> entityMappingOutputTuples,
	                                                         final Element rules,
	                                                         final Document doc) {


		final Map<String, Element> entityElements = new ConcurrentHashMap<>();

		// could probably be rewritten to foldLeft
		entityMappingOutputTuples.forEach(entityMappingOutputTuple -> {

			final javaslang.collection.List<String> mappingOutputAttributes = entityMappingOutputTuple._1;
			final Optional<String> optionalCommonAttributePathOfMappingInputs = entityMappingOutputTuple._2;

			mappingOutputAttributes.foldLeft(javaslang.collection.List.empty(), (currentAttributePath, currentAttribute) -> {

				javaslang.collection.List currentAttributes = currentAttributePath.append(currentAttribute);

				final Optional<String> optionalParentAttributePath = Optional.of(currentAttributePath.mkString(ATTRIBUTE_DELIMITER))
						.filter(attributePath -> !attributePath.isEmpty());
				final String attributePath = currentAttributes.mkString(ATTRIBUTE_DELIMITER);

				entityElements.computeIfAbsent(attributePath, attributePath1 -> {

					final Element entityElement = doc.createElement(METAMORPH_ELEMENT_ENTITY);
					entityElement.setAttribute(MF_ELEMENT_NAME_ATTRIBUTE_IDENTIFIER, currentAttribute);

					if (!optionalParentAttributePath.isPresent()) {

						rules.appendChild(entityElement);

						// set flushWith only at parent entity

						optionalCommonAttributePathOfMappingInputs.ifPresent(commonAttributePathOfMappingInputs -> {

							entityElement.setAttribute(MF_FLUSH_WITH_ATTRIBUTE_IDENTIFIER, commonAttributePathOfMappingInputs);
							entityElement.setAttribute(MF_COLLECTOR_RESET_ATTRIBUTE_IDENTIFIER, BOOLEAN_VALUE_TRUE);
						});
					}

					optionalParentAttributePath.ifPresent(parentAttributePath -> entityElements
							.computeIfPresent(parentAttributePath, (parentAttributePath1, parentAttributePathEntityElement) -> {

								parentAttributePathEntityElement.appendChild(entityElement);

								return parentAttributePathEntityElement;
							}));

					return entityElement;
				});

				return currentAttributes;
			});
		});

		return entityElements;
	}

	/**
	 * _1 = mapping output - last attribute
	 * _2 = mapping output attributes - last attribute
	 * _3 = last attribute of mapping output
	 *
	 * @param mappingOutput
	 * @return
	 */
	private static Optional<Tuple3<Optional<String>, javaslang.collection.List<String>, String>> determineMappingOutputAttribute(final MappingAttributePathInstance mappingOutput) {

		return Optional.ofNullable(mappingOutput)
				.flatMap(mappingOutput1 -> Optional.ofNullable(mappingOutput1.getAttributePath()))
				.flatMap(attributePath -> Optional.ofNullable(attributePath.toAttributePath()))
				.flatMap(attributePathString -> {

					// .ESCAPE_XML11.with(NumericEntityEscaper.between(0x7f, Integer.MAX_VALUE)).translate( <- also doesn't work
					final String escapedMappingOutputAP = StringEscapeUtils.escapeXml(attributePathString);

					final javaslang.collection.List<String> mappingOutputAttributes = javaslang.collection.List.of(escapedMappingOutputAP.split(ATTRIBUTE_DELIMITER));

					if (mappingOutputAttributes.isEmpty()) {

						return Optional.of(Tuple.of(Optional.empty(), mappingOutputAttributes.init(), escapedMappingOutputAP));
					}

					final String lastAttribute = mappingOutputAttributes.last();

					final String mappingOutputRoot = mappingOutputAttributes.init().mkString(ATTRIBUTE_DELIMITER);
					final Optional<String> optionalMappingOutputRoot = Optional.of(mappingOutputRoot).filter(mappingOutputRoot1 -> !mappingOutputRoot1.isEmpty());

					return Optional.of(Tuple.of(optionalMappingOutputRoot, mappingOutputAttributes.init(), lastAttribute));
				});
	}

	/**
	 * sort components re. their natural order (begin from the end, i.e., component without output components)
	 *
	 * @param components original components set
	 * @return
	 */
	private static javaslang.collection.List<Component> sortComponents(final Set<Component> components) {

		final Optional<Component> optionalLastComponent = components.stream()
				.filter(component -> {

					final Set<Component> outputComponents = component.getOutputComponents();

					return outputComponents == null || outputComponents.isEmpty();
				})
				.findFirst();

		if (!optionalLastComponent.isPresent()) {

			return javaslang.collection.List.ofAll(components);
		}

		final Component lastComponent = optionalLastComponent.get();
		final javaslang.collection.List<Component> lastComponentList = javaslang.collection.List.of(lastComponent);
		final javaslang.collection.Map<String, Component> componentMap = javaslang.collection.HashSet.ofAll(components)
				.toMap(component -> Tuple.of(component.getUuid(), component));
		final javaslang.collection.List<Component> emptyComponentList = javaslang.collection.List.empty();

		return addComponents(lastComponentList, componentMap, emptyComponentList);
	}

	private static javaslang.collection.List<Component> addComponents(final javaslang.collection.List<Component> components,
	                                                                  final javaslang.collection.Map<String, Component> componentMap,
	                                                                  final javaslang.collection.List<Component> componentList) {

		final javaslang.collection.Map<String, Component> newComponentMap = componentMap.removeAll(components.map(DMPObject::getUuid));
		final javaslang.collection.List<Component> newComponentList = componentList.prependAll(components);

		if (newComponentMap.isEmpty()) {

			return newComponentList;
		}

		final javaslang.collection.List<Component> emptyPreviousComponentList = javaslang.collection.List.empty();

		javaslang.collection.List<Component> previousComponents = components.foldLeft(emptyPreviousComponentList, (currentComponentList, currentComponent) -> {

			final Set<Component> inputComponents = currentComponent.getInputComponents();

			if (inputComponents == null) {

				return currentComponentList;
			}

			final Set<Component> properInputComponents = inputComponents.stream()
					.map(inputComponent -> newComponentMap.get(inputComponent.getUuid()))
					.filter(optionInputComponent -> !optionInputComponent.isEmpty())
					.map(Option::get)
					.collect(Collectors.toSet());

			return currentComponentList.prependAll(properInputComponents);
		});

		return addComponents(previousComponents, newComponentMap, newComponentList);
	}
}
