package de.avgl.dmp.persistence.mapping;

import java.io.IOException;

import javax.validation.Payload;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.job.Component;
import de.avgl.dmp.persistence.model.job.ComponentType;
import de.avgl.dmp.persistence.model.job.Job;
import de.avgl.dmp.persistence.model.job.Transformation;

public class JsonToPojoMapper {

	private final ObjectMapper objectMapper;

	@Inject
	public JsonToPojoMapper(final ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

//	private Map<String, Parameter> extractParameters(final JsonNode root) {
//		if (root == null) {
//			return null;
//		}
//
//		final Iterator<String> jsParameterFields = root.fieldNames();
//		final Map<String, Parameter> parameters = Maps.newHashMap();
//
//		while (jsParameterFields.hasNext()) {
//			final String parameterName = jsParameterFields.next();
//			final JsonNode jsParameter = root.get(parameterName);
//
//			final Parameter parameter = new Parameter();
//
//			parameter.setName(parameterName);
//			parameter.setType(jsParameter.has("type") ? jsParameter.get("type").asText() : null);
//			parameter.setRepeat(jsParameter.hasNonNull("repeat") && jsParameter.get("repeat").asBoolean());
//
//			parameter.setData(jsParameter.hasNonNull("data") ? jsParameter.get("data").asText() : null);
//
//			if (jsParameter.hasNonNull("parameters")) {
//				parameter.setParameters(extractParameters(jsParameter.get("parameters")));
//			}
//
//			parameters.put(parameterName, parameter);
//		}
//
//		return parameters;
//	}

//	private Component extractComponent(final JsonNode jsComponent) {
//		ComponentType componentType;
//		try {
//			final JsonNode jsComponentTyeJs = jsComponent.get("componentType");
//			final String jsComponentType = jsComponentTyeJs.asText();
//			componentType = ComponentType.getComponentTypeByName(jsComponentType);
//		} catch (DMPPersistenceException e) {
//			e.printStackTrace();
//			return null;
//		}
//
//		final Component component = new Component();
//
//		component.setId(jsComponent.get("id").asText());
//		component.setType(componentType);
//
//		final JsonNode jsPayload = jsComponent.get("payload");
//
//		final Payload payload = new Payload();
//
//		String name;
//		switch (componentType) {
//			case SOURCE:
//			case TARGET: // fall through
//				name = jsPayload.get("path").asText();
//				break;
//			case FUNCTION:
//				name = jsPayload.get("reference").asText();
//				break;
//			case EXTENDED:
//			default: // fall through
//				name = jsPayload.hasNonNull("name") ? jsPayload.get("name").asText() : null;
//				break;
//		}
//
//		payload.setName(name);
//		component.setName(name);
//		payload.setParameters(extractParameters(jsPayload.get("parameters")));
//
//		component.setPayload(payload);
//		return component;
//	}

//	private EndpointComponent extractEndpointComponent(final JsonNode jsComponent) throws DMPPersistenceException {
//		final Component regularComponent = extractComponent(jsComponent);
//		final EndpointComponent component = new EndpointComponent(regularComponent);
//
//		final JsonNode jsPayload = jsComponent.get("payload");
//
//		if (!jsPayload.hasNonNull("resourceId") || !jsPayload.hasNonNull("configurationId")) {
//			throw new DMPPersistenceException(String.format("The component [%s] down not seem to be a proper endpoint component (source|target)", jsComponent));
//		}
//
//		component.setResourceId(jsPayload.get("resourceId").asLong());
//		component.setConfigurationId(jsPayload.get("configurationId").asLong());
//
//		return component;
//	}

//	public Job toJob(final String json) throws IOException, DMPPersistenceException {
//
//		final JsonNode root = objectMapper.readTree(json);
//
//		final ImmutableList.Builder<Transformation> transformationsBuilder = ImmutableList.builder();
//
//		final JsonNode jsTransformations = root.get("transformations");
//
//		for (final JsonNode jsTransformation : jsTransformations) {
//
//			final Transformation transformation = toTransformation(jsTransformation);
//
//			transformationsBuilder.add(transformation);
//		}
//
//		final Job job = new Job();
//
//		job.setTransformations(transformationsBuilder.build());
//
//		return job;
//	}

//	public Transformation toTransformation(final String json) throws IOException, DMPPersistenceException {
//
//		final JsonNode root = objectMapper.readTree(json);
//
//		return toTransformation(root);
//	}

//	private Transformation toTransformation(final JsonNode transformationJsonNode) throws DMPPersistenceException {
//
//		final Transformation transformation = new Transformation();
//
//		transformation.setId(transformationJsonNode.get("id").asText());
//		transformation.setName(transformationJsonNode.get("name").asText());
//
//		final JsonNode jsSource = transformationJsonNode.get("source");
//		final JsonNode jsTarget = transformationJsonNode.get("target");
//		final EndpointComponent source = extractEndpointComponent(jsSource);
//		final EndpointComponent target = extractEndpointComponent(jsTarget);
//
//		transformation.setSource(source);
//		transformation.setTarget(target);
//
//		final ImmutableList.Builder<Component> componentsBuilder = ImmutableList.builder();
//
//		for (final JsonNode jsComponent : transformationJsonNode.get("components")) {
//
//			final Component component = extractComponent(jsComponent);
//			if (component == null) {
//				continue;
//			}
//
//			componentsBuilder.add(component);
//		}
//
//		transformation.setComponents(componentsBuilder.build());
//
//		return transformation;
//	}
}
