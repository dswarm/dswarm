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
package org.dswarm.persistence.model.job.utils;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Key;

import org.dswarm.persistence.model.ExtendedBasicDMPJPAObject;
import org.dswarm.persistence.model.job.Component;
import org.dswarm.persistence.model.job.Function;
import org.dswarm.persistence.model.job.Transformation;
import org.dswarm.persistence.model.types.Tuple;
import org.dswarm.persistence.service.job.ComponentService;

/**
 * Custom logic to deserialize a {@link org.dswarm.persistence.model.job.Transformation}. The problem with the default
 * databind/annotation based approach was, that nested/recursive components, wouldn't work in the way, that they are looked up as
 * needed. Consider the following: <code>
 * components: [{
 *     id: 1
 *     name: c1,
 *     output_components: [{
 *         id: 2
 *     }]
 * },{
 *     id: 2
 *     name: c1,
 *     input_components: [{
 *         id: 1
 *     }]
 * }
 * </code> The default deserializer could not differentiate between full components (main array) and reference-components
 * ((in/out)put_components) and would eventually randomly choose one of these to be the actual component. This deserialzer first
 * parses all main components and collects only the ids from the (in|out)put_components and later resolves them to the full
 * components.
 */
public class TransformationDeserializer extends JsonDeserializer<Transformation> {

	private static final String		ID_KEY						= "id";
	private static final String		NAME_KEY					= "name";
	private static final String		DESCRIPTION_KEY				= "description";
	private static final String		PARAMETERS_KEY				= "parameters";
	private static final String		COMPONENTS_KEY				= "components";
	private static final String		FUNCTION_KEY				= "function";
	private static final String		FUNCTION_DESCRIPTION_KEY	= "function_description";
	private static final String		PARAMETER_MAPPINGS_KEY		= "parameter_mappings";
	private static final String		INPUT_COMPONENTS_KEY		= "input_components";
	private static final String		OUTPUT_COMPONENTS_KEY		= "output_components";

	private DeserializationContext	deserializationContext;

	@Override
	public Transformation deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException {

		deserializationContext = ctxt;
		Transformation transformation = new Transformation();

		String currentFieldName = null;
		JsonToken currentToken = jp.getCurrentToken();

		while (currentToken != JsonToken.END_OBJECT) {
			switch (currentToken) {
				case FIELD_NAME:
					currentFieldName = jp.getText();
					break;

				case VALUE_STRING:
					if (TransformationDeserializer.FUNCTION_DESCRIPTION_KEY.equals(currentFieldName)) {
						TransformationDeserializer.setFunctionDescription(jp, transformation);
					} else {
						TransformationDeserializer.setStringValue(jp, transformation, currentFieldName);
					}
					break;

				case VALUE_NUMBER_INT:
					transformation = Transformation.withId(transformation, jp.getLongValue());
					break;

				case START_ARRAY:
					if (TransformationDeserializer.PARAMETERS_KEY.equals(currentFieldName)) {
						TransformationDeserializer.setParameters(jp, transformation);
					}
					if (TransformationDeserializer.COMPONENTS_KEY.equals(currentFieldName)) {
						setComponents(jp, transformation);
					}
					break;

				default: // no-op
			}

			currentToken = jp.nextToken();
		}

		return transformation;
	}

	/**
	 * get a {@link org.dswarm.persistence.service.job.ComponentService} from Guice
	 * 
	 * @param ctxt the deserialization context
	 * @return the component service or throw an NPE if no service could be found
	 */
	private static ComponentService getComponentService(final DeserializationContext ctxt) {
		Preconditions.checkNotNull(ctxt);
		final ComponentService componentService = (ComponentService) ctxt.findInjectableValue(Key.get(ComponentService.class), null, null);
		return Preconditions.checkNotNull(componentService);
	}

	/**
	 * Set common string values (name, and description) for either a transformation or a component
	 * 
	 * @param jp the current json parser
	 * @param object either the transformation or the component
	 * @param currentFieldName the json field name
	 * @throws IOException
	 */
	private static void setStringValue(final JsonParser jp, final ExtendedBasicDMPJPAObject object, final String currentFieldName) throws IOException {
		if (TransformationDeserializer.NAME_KEY.equals(currentFieldName)) {

			object.setName(jp.getText());

		} else if (TransformationDeserializer.DESCRIPTION_KEY.equals(currentFieldName)) {

			object.setDescription(jp.getText());
		}
	}

	/**
	 * Set the function description of a transformation. Requires an object behind function_description.
	 * 
	 * @param jp the current json parser
	 * @param transformation the target {@code Transformation}
	 * @throws IOException
	 */
	private static void setFunctionDescription(final JsonParser jp, final Transformation transformation) throws IOException {
		final TreeNode treeNode = jp.readValueAsTree();
		if (treeNode.isObject()) {
			transformation.setFunctionDescription((com.fasterxml.jackson.databind.node.ObjectNode) treeNode);
		} else {
			throw JsonMappingException.from(jp, String.format(
					"Cannot parse the function description for the Transformation [%s], because it is not a Json Object", transformation));
		}
	}

	/**
	 * Set the parameter list of a transformation.
	 * 
	 * @param jp the current json parser
	 * @param transformation the target {@code Transformation}
	 * @throws IOException
	 */
	private static void setParameters(final JsonParser jp, final Transformation transformation) throws IOException {
		JsonToken currentToken = jp.nextToken();

		final LinkedList<String> parameters = Lists.newLinkedList();

		while (currentToken != JsonToken.END_ARRAY) {
			parameters.add(jp.getText());
			currentToken = jp.nextToken();
		}

		transformation.setParameters(parameters);
	}

	/**
	 * Set the components of a transformation. This will parse all components and cache them into a map. Simultaneously, it will
	 * parse the (in|out)put_components and build a map of {@code id -> (inIds, outIds)}. After having parsed all components, the
	 * actual input components and output components are resolved, linked, and finally inserted into each component.
	 * 
	 * @param jp the current json parser
	 * @param transformation the target {@code Transformation}
	 * @throws IOException
	 */
	private void setComponents(final JsonParser jp, final Transformation transformation) throws IOException {
		final Map<Long, Component> components = Maps.newLinkedHashMap();
		final Map<Long, Tuple<List<Long>, List<Long>>> inOutComponents = Maps.newHashMap();

		JsonToken currentToken = jp.getCurrentToken();
		String currentFieldName = null;

		while (currentToken != JsonToken.END_ARRAY) {

			while (currentToken != JsonToken.START_OBJECT && currentToken != JsonToken.END_ARRAY) {
				currentToken = jp.nextToken();
			}
			if (currentToken == JsonToken.END_ARRAY) {
				break;
			}
			currentToken = jp.nextToken();

			Long currentComponentId = null;
			final Component currentComponent = new Component();

			final ImmutableList.Builder<Long> inputComponentsBuilder = new ImmutableList.Builder<>();
			final ImmutableList.Builder<Long> outputComponentsBuilder = new ImmutableList.Builder<>();

			while (currentToken != JsonToken.END_OBJECT) {
				switch (currentToken) {
					case FIELD_NAME:
						currentFieldName = jp.getText();
						break;

					case VALUE_STRING:
						TransformationDeserializer.setStringValue(jp, currentComponent, currentFieldName);
						break;

					case START_OBJECT:
						if (TransformationDeserializer.FUNCTION_KEY.equals(currentFieldName)) {
							TransformationDeserializer.setFunction(jp, currentComponent);
						} else if (TransformationDeserializer.PARAMETER_MAPPINGS_KEY.equals(currentFieldName)) {
							TransformationDeserializer.setParameterMappings(jp, currentComponent);
						}
						break;

					case VALUE_NUMBER_INT:
						if (TransformationDeserializer.ID_KEY.equals(currentFieldName)) {
							currentComponentId = jp.getLongValue();
							components.put(currentComponentId, currentComponent);
						}
						break;

					case START_ARRAY:
						if (TransformationDeserializer.INPUT_COMPONENTS_KEY.equals(currentFieldName)) {
							inputComponentsBuilder.addAll(TransformationDeserializer.parseNestedComponent(jp));
						}
						if (TransformationDeserializer.OUTPUT_COMPONENTS_KEY.equals(currentFieldName)) {
							outputComponentsBuilder.addAll(TransformationDeserializer.parseNestedComponent(jp));
						}
						break;

					default: // no-op
				}

				currentToken = jp.nextToken();
			}

			if (currentComponentId == null) {
				throw JsonMappingException.from(jp, String.format("This component [%s] is missing an ID", currentComponent));
			}

			final List<Long> inputComponentIds = inputComponentsBuilder.build();
			final List<Long> outputComponentIds = outputComponentsBuilder.build();

			inOutComponents.put(currentComponentId, Tuple.tuple(inputComponentIds, outputComponentIds));

			currentToken = jp.nextToken();
		}

		TransformationDeserializer.assignComponentIds(components);

		linkInOutComponents(components, inOutComponents);

		TransformationDeserializer.addComponents(transformation, components);
	}

	/**
	 * Set the function of a component. Delegates to annotation/databind based parsing of a
	 * {@link org.dswarm.persistence.model.job.Function}
	 * 
	 * @param jp the current json parser
	 * @param component the target {@code Component}
	 * @throws IOException
	 */
	private static void setFunction(final JsonParser jp, final Component component) throws IOException {

		final Function function = jp.readValueAs(Function.class);
		component.setFunction(function);
	}

	/**
	 * Set the parameter mappings of a component.
	 * 
	 * @param jp the current json parser
	 * @param component the target {@code Component}
	 * @throws IOException
	 */
	private static void setParameterMappings(final JsonParser jp, final Component component) throws IOException {
		final Map<String, String> mappings = Maps.newHashMap();

		JsonToken currentToken = jp.nextToken();
		String currentFieldName = null;

		while (currentToken != JsonToken.END_OBJECT) {
			switch (currentToken) {
				case FIELD_NAME:
					currentFieldName = jp.getText();
					break;

				case VALUE_STRING:
					mappings.put(currentFieldName, jp.getText());
					break;

				default: // no-op
			}

			currentToken = jp.nextToken();
		}

		component.setParameterMappings(mappings);
	}

	/**
	 * Parse a component as a nested component, that has only the id field set. Does not attempt to actually parse any of the
	 * component but the id.
	 * 
	 * @param jp the current json parser
	 * @return a list found ids
	 * @throws IOException
	 */
	private static List<Long> parseNestedComponent(final JsonParser jp) throws IOException {
		final ImmutableList.Builder<Long> ids = new ImmutableList.Builder<>();

		JsonToken currentToken = jp.getCurrentToken();
		String currentFieldName = null;

		while (currentToken != JsonToken.END_ARRAY) {

			while (currentToken != JsonToken.START_OBJECT && currentToken != JsonToken.END_ARRAY) {
				currentToken = jp.nextToken();
			}
			if (currentToken == JsonToken.END_ARRAY) {
				break;
			}
			currentToken = jp.nextToken();

			while (currentToken != JsonToken.END_OBJECT) {
				switch (currentToken) {
					case FIELD_NAME:
						currentFieldName = jp.getText();
						break;

					case VALUE_NUMBER_INT:
						if (TransformationDeserializer.ID_KEY.equals(currentFieldName)) {
							ids.add(jp.getLongValue());
						}
						break;

					default: // no-op
				}

				currentToken = jp.nextToken();
			}

			currentToken = jp.nextToken();
		}

		return ids.build();
	}

	/**
	 * Update the cache by making sure, that every component as a valid id set. The reason behind this is, that {@code Component}s
	 * are compared only by their ID, if they are compared in a shallow manner (e.g. when used in a {@code Set}).
	 * 
	 * @param components a map of (id -> {@code Component})
	 */
	private static void assignComponentIds(final Map<Long, Component> components) {
		for (final Long componentId : components.keySet()) {

			final Component component = components.get(componentId);
			if (component.getId() == null) {
				final Component newComponent = Component.withId(component, componentId);
				components.put(componentId, newComponent);
			}
		}
	}

	/**
	 * Link all input and output components. the input and output components are given as a list of ids and need to be resolved to
	 * actual {@code Component}s. This happens primarily against the components of the current Json or against the database, if
	 * the component wasn't defined within the current Json.
	 * 
	 * @param components a map of (id -> {@code Component})
	 * @param inOutComponents a map of (id -> list ( inIds, outIds ) )
	 */
	private void linkInOutComponents(final Map<Long, Component> components, final Map<Long, Tuple<List<Long>, List<Long>>> inOutComponents) {
		final ComponentService componentService = TransformationDeserializer.getComponentService(deserializationContext);

		for (final Long componentId : components.keySet()) {

			final Tuple<List<Long>, List<Long>> inOut = inOutComponents.get(componentId);
			if (inOut != null) {
				final List<Long> inputComponentIds = inOut.v1();
				final List<Long> outputComponentIds = inOut.v2();

				final List<Component> inputComponents = TransformationDeserializer.lookupComponents(inputComponentIds, components, componentService);
				final List<Component> outputComponents = TransformationDeserializer
						.lookupComponents(outputComponentIds, components, componentService);

				final Component component = components.get(componentId);

				for (final Component inputComponent : inputComponents) {
					component.addInputComponent(inputComponent);
				}

				for (final Component outputComponent : outputComponents) {
					component.addOutputComponent(outputComponent);
				}
			}
		}
	}

	/**
	 * Set the components for a transformation.
	 * 
	 * @param transformation the target {@code Transformation}
	 * @param components the target components, a map of (id -> {@code Component})
	 */
	private static void addComponents(final Transformation transformation, final Map<Long, Component> components) {
		for (final Component component : components.values()) {
			transformation.addComponent(component);
		}
	}

	/**
	 * Lookup {@code Component}s by its id. First, look in the provided cache. Second, go to the database Third, ensure, that
	 * there is an ID set (Note: It might be, that this isn't necessary anymore)
	 * 
	 * @param ids a list of components ids to lookup
	 * @param components the current component cache, a map of (id -> {@code Component})
	 * @param componentService the component service for database lookups
	 */
	private static LinkedList<Component> lookupComponents(final List<Long> ids, final Map<Long, Component> components,
			final ComponentService componentService) {
		final LinkedList<Component> result = Lists.newLinkedList();

		for (final Long id : ids) {
			Component component = components.get(id);

			if (component == null) {
				component = componentService.getObject(id);
			}
			if (component != null) {

				if (component.getId() == null) {
					component = Component.withId(component, id);
					components.put(id, component);
				}

				result.add(component);
			}
		}

		return result;
	}
}
