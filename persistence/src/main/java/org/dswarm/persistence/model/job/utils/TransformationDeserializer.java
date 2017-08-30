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
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Key;

import org.dswarm.common.types.Tuple;
import org.dswarm.persistence.model.ExtendedBasicDMPJPAObject;
import org.dswarm.persistence.model.job.Component;
import org.dswarm.persistence.model.job.Function;
import org.dswarm.persistence.model.job.Transformation;
import org.dswarm.persistence.service.UUIDService;
import org.dswarm.persistence.service.job.ComponentService;

/**
 * Custom logic to deserialize a {@link org.dswarm.persistence.model.job.Transformation}. The problem with the default
 * databind/annotation based approach was, that nested/recursive components, wouldn't work in the way, that they are looked up as
 * needed. Consider the following: <code>
 * components: [{
 * id: 1
 * name: c1,
 * output_components: [{
 * id: 2
 * }]
 * },{
 * id: 2
 * name: c1,
 * input_components: [{
 * id: 1
 * }]
 * }
 * </code> The default deserializer could not differentiate between full components (main array) and reference-components
 * ((in/out)put_components) and would eventually randomly choose one of these to be the actual component. This deserialzer first
 * parses all main components and collects only the ids from the (in|out)put_components and later resolves them to the full
 * components.
 */
public class TransformationDeserializer extends JsonDeserializer<Transformation> {

	private static final String UUID_KEY                 = "uuid";
	private static final String NAME_KEY                 = "name";
	private static final String DESCRIPTION_KEY          = "description";
	private static final String PARAMETERS_KEY           = "parameters";
	private static final String COMPONENTS_KEY           = "components";
	private static final String FUNCTION_KEY             = "function";
	private static final String FUNCTION_DESCRIPTION_KEY = "function_description";
	private static final String PARAMETER_MAPPINGS_KEY   = "parameter_mappings";
	private static final String INPUT_COMPONENTS_KEY     = "input_components";
	private static final String OUTPUT_COMPONENTS_KEY    = "output_components";

	private DeserializationContext deserializationContext;

	@Override
	public Transformation deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException {

		deserializationContext = ctxt;

		// ok, transformation will be generated from scratch, when uuid is available in payload
		final String uuid = UUIDService.getUUID(Transformation.class.getSimpleName());

		Transformation transformation = new Transformation(uuid);

		String currentFieldName = null;
		JsonToken currentToken = jp.getCurrentToken();

		while (currentToken != JsonToken.END_OBJECT) {
			switch (currentToken) {
				case FIELD_NAME:
					currentFieldName = jp.getText();
					break;

				case VALUE_STRING:
					if (currentFieldName != null) {
						switch (currentFieldName) {
							case TransformationDeserializer.FUNCTION_DESCRIPTION_KEY:
								TransformationDeserializer.setFunctionDescription(jp, transformation);
								break;
							case TransformationDeserializer.UUID_KEY:
								transformation = Transformation.withId(transformation, jp.getText());
								break;
							default:
								TransformationDeserializer.setStringValue(jp, transformation, currentFieldName);
								break;
						}
					}
					break;

				case VALUE_NUMBER_INT:

					// TODO: are there any numbers in there? id is now a uuid; hence a string
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
	 * @param jp               the current json parser
	 * @param object           either the transformation or the component
	 * @param currentFieldName the json field name
	 * @throws IOException
	 */
	private static void setStringValue(final JsonParser jp, final ExtendedBasicDMPJPAObject object, final String currentFieldName)
			throws IOException {
		if (TransformationDeserializer.NAME_KEY.equals(currentFieldName)) {

			object.setName(jp.getText());

		} else if (TransformationDeserializer.DESCRIPTION_KEY.equals(currentFieldName)) {

			object.setDescription(jp.getText());
		}
	}

	/**
	 * Set the function description of a transformation. Requires an object behind function_description.
	 *
	 * @param jp             the current json parser
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
	 * @param jp             the current json parser
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
	 * @param jp             the current json parser
	 * @param transformation the target {@code Transformation}
	 * @throws IOException
	 */
	private void setComponents(final JsonParser jp, final Transformation transformation) throws IOException {
		final Map<String, Component> components = Maps.newLinkedHashMap();
		final Map<String, Tuple<List<String>, List<String>>> inOutComponents = Maps.newHashMap();

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

			Optional<String> currentComponentId = Optional.absent();

			// TODO we first need to extract the existing uuid, before we generate one!
			// => right now the processing order is not correct, i.e., we need to this in another, i.e., the component should always (?) be created with a given uuid, or? - (as long as there is one available in the given payload)
			// => currently, we often run in a branch where createComponent(String, Component) is utilised (which should be avoided)
			// => i.e. we need to get the process into right processing order

			Component currentComponent = new Component(UUIDService.getUUID(Component.class.getSimpleName()));

			final ImmutableList.Builder<String> inputComponentsBuilder = new ImmutableList.Builder<>();
			final ImmutableList.Builder<String> outputComponentsBuilder = new ImmutableList.Builder<>();

			while (currentToken != JsonToken.END_OBJECT) {
				switch (currentToken) {
					case FIELD_NAME:
						currentFieldName = jp.getText();
						break;

					case VALUE_STRING:

						if (TransformationDeserializer.UUID_KEY.equals(currentFieldName)) {
							currentComponentId = Optional.fromNullable(Strings.emptyToNull(jp.getText()));
							if (!currentComponentId.isPresent()) {
								throw JsonMappingException.from(jp, "could not create component, i.e., uuid is not provided");
							}

							currentComponent = createNewComponentWithId(currentComponent, currentComponentId.get());
							components.put(currentComponentId.get(), currentComponent);
						} else {
							TransformationDeserializer.setStringValue(jp, currentComponent, currentFieldName);
						}
						break;

					case START_OBJECT:
						if (TransformationDeserializer.FUNCTION_KEY.equals(currentFieldName)) {

							TransformationDeserializer.setFunction(jp, currentComponent);
						} else if (TransformationDeserializer.PARAMETER_MAPPINGS_KEY.equals(currentFieldName)) {

							TransformationDeserializer.setParameterMappings(jp, currentComponent);
						}
						break;

					case VALUE_NUMBER_INT:

						// TODO: are there any numbers in there? id is now a uuid; hence a string

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

			if (!currentComponentId.isPresent()) {
				throw JsonMappingException.from(jp, String.format("This component [%s] is missing an ID", currentComponent));
			}

			final List<String> inputComponentIds = inputComponentsBuilder.build();
			final List<String> outputComponentIds = outputComponentsBuilder.build();

			inOutComponents.put(currentComponentId.get(), Tuple.tuple(inputComponentIds, outputComponentIds));

			currentToken = jp.nextToken();
		}

		TransformationDeserializer.assignComponentIds(components);

		linkInOutComponents(components, inOutComponents);

		TransformationDeserializer.addComponents(transformation, components);
	}

	private Component createNewComponentWithId(final Component currentComponent, final String componentId) throws JsonMappingException {
		return Component.withId(currentComponent, componentId);
	}

	/**
	 * Set the function of a component. Delegates to annotation/databind based parsing of a
	 * {@link org.dswarm.persistence.model.job.Function}
	 *
	 * @param jp        the current json parser
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
	 * @param jp        the current json parser
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
	private static List<String> parseNestedComponent(final JsonParser jp) throws IOException {
		final ImmutableList.Builder<String> ids = new ImmutableList.Builder<>();

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

					case VALUE_STRING:

						if (TransformationDeserializer.UUID_KEY.equals(currentFieldName)) {
							ids.add(jp.getText());
						}

						break;
					case VALUE_NUMBER_INT:

						// TODO: are there any numbers in there? id is now a uuid; hence a string

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
	private static void assignComponentIds(final Map<String, Component> components) {
		for (final String componentId : components.keySet()) {

			final Component component = components.get(componentId);
			if (component.getUuid() == null) {
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
	 * @param components      a map of (id -> {@code Component})
	 * @param inOutComponents a map of (id -> list ( inIds, outIds ) )
	 */
	private void linkInOutComponents(final Map<String, Component> components, final Map<String, Tuple<List<String>, List<String>>> inOutComponents) {
		final ComponentService componentService = TransformationDeserializer.getComponentService(deserializationContext);

		for (final String componentId : components.keySet()) {

			final Tuple<List<String>, List<String>> inOut = inOutComponents.get(componentId);
			if (inOut != null) {
				final List<String> inputComponentIds = inOut.v1();
				final List<String> outputComponentIds = inOut.v2();

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
	 * @param components     the target components, a map of (id -> {@code Component})
	 */
	private static void addComponents(final Transformation transformation, final Map<String, Component> components) {
		for (final Component component : components.values()) {
			transformation.addComponent(component);
		}
	}

	/**
	 * Lookup {@code Component}s by its id. First, look in the provided cache. Second, go to the database Third, ensure, that
	 * there is an ID set (Note: It might be, that this isn't necessary anymore)
	 *
	 * @param ids              a list of components ids to lookup
	 * @param components       the current component cache, a map of (id -> {@code Component})
	 * @param componentService the component service for database lookups
	 */
	private static LinkedList<Component> lookupComponents(final List<String> ids, final Map<String, Component> components,
			final ComponentService componentService) {
		final LinkedList<Component> result = Lists.newLinkedList();

		for (final String id : ids) {
			Component component = components.get(id);

			if (component == null) {
				component = componentService.getObject(id);
			}
			if (component != null) {

				if (component.getUuid() == null) {
					component = Component.withId(component, id);
					components.put(id, component);
				}

				result.add(component);
			}
		}

		return result;
	}
}
