package de.avgl.dmp.persistence.mapping;


import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.transformation.Component;
import de.avgl.dmp.persistence.model.transformation.ComponentType;
import de.avgl.dmp.persistence.model.transformation.Parameter;
import de.avgl.dmp.persistence.model.transformation.Payload;
import de.avgl.dmp.persistence.model.transformation.Transformation;

public class JsonToPojoMapper {

	private static final ObjectMapper mapper;

	static {
		// create once, reuse
		mapper = new ObjectMapper();

		final JaxbAnnotationModule module = new JaxbAnnotationModule();
		// configure as necessary
		mapper.registerModule(module);
	}

	private Map<String, Parameter> extractParameters(final JsonNode root) {
		if (root == null) {
			return null;
		}

		Iterator<String> jsParameterFields = root.fieldNames();
		Map<String, Parameter> parameters = Maps.newHashMap();


		while (jsParameterFields.hasNext()) {
			String parameterName = jsParameterFields.next();
			JsonNode jsParameter = root.get(parameterName);

			Parameter parameter = new Parameter();

			parameter.setName(parameterName);
			parameter.setType(jsParameter.has("type") ? jsParameter.get("type").asText() : null);
			parameter.setRepeat(jsParameter.hasNonNull("repeat") && jsParameter.get("repeat").asBoolean());

			parameter.setData(jsParameter.hasNonNull("data") ? jsParameter.get("data").asText() : null);

			if (jsParameter.hasNonNull("parameters")) {
				parameter.setParameters(extractParameters(jsParameter.get("parameters")));
			}

			parameters.put(parameterName, parameter);
		}

		return parameters;
	}

	private Component extractComponent(JsonNode jsComponent) {
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

		String name;
		switch (componentType) {
			case SOURCE:
			case TARGET:    // fall through
				name = jsPayload.get("path").asText();
				break;
			case FUNCTION:
				name = jsPayload.get("reference").asText();
				break;
			case EXTENDED:
			default:        // fall through
				name = jsPayload.hasNonNull("name") ? jsPayload.get("name").asText() : null;
				break;
		}

		payload.setName(name);
		component.setName(name);
		payload.setParameters(extractParameters(jsPayload.get("parameters")));

		component.setPayload(payload);
		return component;
	}

	public List<Transformation> apply(final String json) throws IOException {
		final JsonNode root = mapper.readTree(json);

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
}


