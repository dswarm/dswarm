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
package org.dswarm.persistence.model.internal.gdm;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.common.web.URI;
import org.dswarm.graph.json.Model;
import org.dswarm.graph.json.Node;
import org.dswarm.graph.json.Resource;
import org.dswarm.graph.json.ResourceNode;
import org.dswarm.graph.json.Statement;
import org.dswarm.graph.json.util.Util;
import org.dswarm.init.util.DMPStatics;
import org.dswarm.persistence.model.internal.gdm.helper.ConverterHelper3;
import org.dswarm.persistence.model.internal.gdm.helper.ConverterHelperGDMHelper3;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.util.GDMUtil;

/**
 * Created by tgaengler on 07.10.16.
 */
public final class GDMModelUtil {

	private static final Logger LOG = LoggerFactory.getLogger(GDMModelUtil.class);

	public static Optional<JsonNode> toJSCJSON(final Model model, final Set<String> recordURIs, final Schema schema) {

		if (model == null) {

			LOG.debug("model is null, can't convert model to JSON");

			return Optional.empty();
		}

		if (recordURIs == null) {

			LOG.debug("resource URI is null, can't convert model to JSON");

			return Optional.empty();
		}

		final Iterator<String> iter = recordURIs.iterator();

		if (!iter.hasNext()) {

			// no entries

			return Optional.empty();
		}

		final ArrayNode jsonArray = Util.getJSONObjectMapper().createArrayNode();
		final Map<String, URI> uriMap = new ConcurrentHashMap<>();
		final Set<String> arraySapis = schema.getAttributePaths()
				.stream()
				.filter(sapi -> sapi.isMultivalue() != null && Boolean.TRUE.equals(sapi.isMultivalue()))
				.map(sapi1 -> sapi1.getAttributePath().toAttributePath())
				.collect(Collectors.toSet());
		final Map<String, Map<String, String>> attributePathMap = new ConcurrentHashMap<>();

		iter.forEachRemaining(resourceURI -> {

			final Resource recordResource = model.getResource(resourceURI);

			if (recordResource == null) {

				LOG.debug("couldn't find record resource for record  uri '{}' in model", resourceURI);

				return;
			}

			// determine record resource node from statements of the record resource
			final ResourceNode recordResourceNode = Util.getResourceNode(resourceURI, recordResource);

			if (recordResourceNode == null) {

				LOG.debug("couldn't find record resource node for record  uri '{}' in model", resourceURI);

				return;
			}

			final ObjectNode json = Util.getJSONObjectMapper().createObjectNode();
			final String rootAttributePath = "";

			final Optional<JsonNode> optionalJsonNode = convertToJSCJSON(recordResource, recordResourceNode, json, json, rootAttributePath, uriMap, arraySapis, attributePathMap);

			optionalJsonNode.ifPresent(jsonNode -> jsonArray.add(json));
		});

		if (jsonArray.size() <= 0) {

			return Optional.empty();
		}

		return Optional.of(jsonArray);
	}

	private static Optional<JsonNode> convertToJSCJSON(final Resource recordResource,
	                                                   final Node resourceNode,
	                                                   final ObjectNode rootJson,
	                                                   final ObjectNode json,
	                                                   final String rootAttributePath,
	                                                   final Map<String, URI> uriMap,
	                                                   final Collection<String> arraySapis,
	                                                   final Map<String, Map<String, String>> attributePathMap) {

		final Map<String, ConverterHelper3> converterHelpers = new LinkedHashMap<>();

		// filter record resource statements to statements for subject uri/id (resource node))
		final Set<Statement> statements = Util.getResourceStatement(resourceNode, recordResource);

		statements.forEach(statement -> {

			final String propertyURI = statement.getPredicate().getUri();

			if(GDMUtil.RDF_type.equals(propertyURI)) {

				// skip rdf:type statements for now

				return;
			}

			final String localName = getOrAddLocalName(propertyURI, uriMap);
			final String attributePath = getOrAddAttributePath(rootAttributePath, propertyURI, attributePathMap);
			final boolean isArrayAttributePath = isArrayAttributePath(attributePath, arraySapis);
			final Node gdmNode = statement.getObject();

			switch (gdmNode.getType()) {

				case Literal:

					handleLiteralNode(converterHelpers, localName, gdmNode, isArrayAttributePath);

					break;
				case Resource:

					handleResourceNode(recordResource, rootJson, uriMap, converterHelpers, localName, gdmNode, isArrayAttributePath, attributePath, arraySapis, attributePathMap);

					break;
				case BNode:

					handleBNode(recordResource, rootJson, uriMap, converterHelpers, localName, gdmNode, isArrayAttributePath, attributePath, arraySapis, attributePathMap);

					break;
			}
		});

		converterHelpers.entrySet().forEach(converterHelperEntry -> converterHelperEntry.getValue().build(json));

		return Optional.of(json);
	}

	private static void handleLiteralNode(final Map<String, ConverterHelper3> converterHelpers,
	                                      final String localName,
	                                      final Node gdmNode,
	                                      final boolean isArrayAttributePath) {

		ConverterHelperGDMHelper3.addLiteralToConverterHelper(converterHelpers, localName, gdmNode, isArrayAttributePath);
	}

	private static void handleResourceNode(final Resource recordResource,
	                                       final ObjectNode rootJson,
	                                       final Map<String, URI> uriMap,
	                                       final Map<String, ConverterHelper3> converterHelpers,
	                                       final String localName,
	                                       final Node gdmNode,
	                                       final boolean isArrayAttributePath,
	                                       final String rootAttributePath,
	                                       final Collection<String> arraySapis,
	                                       final Map<String, Map<String, String>> attributePathMap) {

		final ResourceNode object = (ResourceNode) gdmNode;

		// filter record resource statements to statements for object uri (object node))
		final Set<Statement> objectStatements = Util.getResourceStatement(object, recordResource);

		if (objectStatements == null || objectStatements.isEmpty()) {

			ConverterHelperGDMHelper3.addURIResourceToConverterHelper(converterHelpers, localName, gdmNode, isArrayAttributePath);

			return;
		}

		// resource has an uri, but is deeper in the hierarchy -> it will be attached to the root json node as separate
		// entry

		final ObjectNode objectNode = Util.getJSONObjectMapper().createObjectNode();

		final Optional<JsonNode> optionalJsonNode = convertToJSCJSON(recordResource, object, rootJson, objectNode, rootAttributePath, uriMap, arraySapis, attributePathMap);

		optionalJsonNode.ifPresent(jsonNode -> rootJson.set(object.getUri(), jsonNode));
	}

	private static void handleBNode(final Resource recordResource,
	                                final ObjectNode rootJson,
	                                final Map<String, URI> uriMap,
	                                final Map<String, ConverterHelper3> converterHelpers,
	                                final String localName,
	                                final Node gdmNode,
	                                final boolean isArrayAttributePath,
	                                final String rootAttributePath,
	                                final Collection<String> arraySapis,
	                                final Map<String, Map<String, String>> attributePathMap) {

		// node is (/must be) a blank node

		final ObjectNode objectNode = Util.getJSONObjectMapper().createObjectNode();

		final Optional<JsonNode> optionalJsonNode = convertToJSCJSON(recordResource, gdmNode, rootJson, objectNode, rootAttributePath, uriMap, arraySapis, attributePathMap);

		optionalJsonNode.ifPresent(jsonNode -> ConverterHelperGDMHelper3.addJSONNodeToConverterHelper(converterHelpers, localName, jsonNode, isArrayAttributePath));
	}

	private static String getOrAddLocalName(final String uri,
	                                        final Map<String, URI> uriMap) {

		return uriMap.computeIfAbsent(uri, URI::new).getLocalName();
	}

	private static String getOrAddAttributePath(final String rootAttributePath,
	                                            final String attribute,
	                                            final Map<String, Map<String, String>> attributePathMap) {

		return attributePathMap.computeIfAbsent(rootAttributePath, rootAttributePath1 -> new ConcurrentHashMap<>())
				.computeIfAbsent(attribute, attribute1 -> createAttributePath(rootAttributePath, attribute));
	}

	private static String createAttributePath(final String rootAttributePath, final String attribute) {

		if("".equals(rootAttributePath)) {

			return attribute;
		}

		return rootAttributePath + DMPStatics.ATTRIBUTE_DELIMITER + attribute;
	}

	private static boolean isArrayAttributePath(final String attributePath, final Collection<String> arraySapis) {

		return arraySapis.contains(attributePath);
	}
}
