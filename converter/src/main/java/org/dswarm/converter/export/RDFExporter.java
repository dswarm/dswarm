/**
 * Copyright (C) 2013 – 2016 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dswarm.converter.export;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Iterators;
import org.apache.jena.graph.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.dswarm.converter.DMPConverterError;
import org.dswarm.converter.DMPConverterException;
import org.dswarm.graph.json.*;
import org.dswarm.graph.json.Node;
import org.dswarm.persistence.model.internal.gdm.GDMModel;
import rx.Observable;

import javax.ws.rs.core.MediaType;
import javax.xml.stream.XMLStreamException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by tgaengler on 03.03.16.
 * <p>
 * TODO: maybe, go with a cache impl that is more performing than concurrent hash map, e.g., that one from hppc
 */
public class RDFExporter implements Exporter<GDMModel> {

	private final MediaType mediaType;
	private final Lang rdfSerializationFormat;

	private enum TriplePosition {
		SUBJECT,
		OBJECT
	}

	public RDFExporter(final MediaType mediaTypeArg) {

		mediaType = mediaTypeArg;
		rdfSerializationFormat = RDFLanguages.contentTypeToLang(mediaType.toString());
	}

	@Override
	public Observable<JsonNode> generate(final Observable<GDMModel> recordGDM,
	                                     final OutputStream outputStream) throws XMLStreamException {

		final StreamRDF writer = StreamRDFWriter.getWriterStream(outputStream, rdfSerializationFormat);

		writer.start();

		final ConcurrentHashMap<String, org.apache.jena.graph.Node> resourceNodeCache = new ConcurrentHashMap<>();
		final ConcurrentHashMap<String, org.apache.jena.graph.Node> predicateCache = new ConcurrentHashMap<>();

		return recordGDM.map(recordGDMModel -> processRecordGDMModel(writer, resourceNodeCache, predicateCache, recordGDMModel))
				.map(org.dswarm.persistence.model.internal.Model::toJSON)
				.flatMapIterable(nodes -> {

					final ArrayList<JsonNode> nodeList = new ArrayList<>();

					Iterators.addAll(nodeList, nodes.elements());

					return nodeList;
				})
				.doOnCompleted(() -> writer.finish());
	}

	private static GDMModel processRecordGDMModel(final StreamRDF writer,
	                                              final ConcurrentHashMap<String, org.apache.jena.graph.Node> resourceNodeCache,
	                                              final ConcurrentHashMap<String, org.apache.jena.graph.Node> predicateCache, GDMModel recordGDMModel) {

		final Optional<Model> optionalRecordModel = Optional.ofNullable(recordGDMModel.getModel());

		optionalRecordModel.flatMap(recordModel -> Optional.ofNullable(recordModel.getResources())
				.filter(resources -> {

					if (resources.isEmpty()) {

						return false;
					}

					return true;
				}))
				.ifPresent(resources -> resources.stream()
						.forEach(resource -> processResource(writer, resourceNodeCache, predicateCache, resource)));


		return recordGDMModel;
	}

	private static void processResource(final StreamRDF writer,
	                                    final ConcurrentHashMap<String, org.apache.jena.graph.Node> resourceNodeCache,
	                                    final ConcurrentHashMap<String, org.apache.jena.graph.Node> predicateCache, Resource resource) {

		final ConcurrentHashMap<Long, org.apache.jena.graph.Node> bnodeCache = new ConcurrentHashMap<>();

		final Optional<Collection<Statement>> optionalStatements = Optional.ofNullable(resource.getStatements());

		optionalStatements.filter(statements -> {

			if (statements.isEmpty()) {

				return false;
			}

			return true;
		})
				.ifPresent(statements -> statements.stream()
						.map(statement -> {

							try {

								return generateTriple(statement, resourceNodeCache, bnodeCache, predicateCache);
							} catch (final DMPConverterException e) {

								throw DMPConverterError.wrap(e);
							}
						})
						.forEach(triple -> writer.triple(triple)));
	}

	private static Triple generateTriple(final Statement statement,
	                                     final ConcurrentHashMap<String, org.apache.jena.graph.Node> resourceNodeCache,
	                                     final ConcurrentHashMap<Long, org.apache.jena.graph.Node> bnodeCache,
	                                     final ConcurrentHashMap<String, org.apache.jena.graph.Node> predicateCache) throws DMPConverterException {

		final Node gdmSubject = statement.getSubject();
		final Predicate gdmPredicate = statement.getPredicate();
		final Node gdmObject = statement.getObject();

		final org.apache.jena.graph.Node subject = generateSubjectNode(gdmSubject, resourceNodeCache, bnodeCache);
		final org.apache.jena.graph.Node predicate = generatePredicate(gdmPredicate, predicateCache);
		final org.apache.jena.graph.Node object = generateObjectNode(gdmObject, resourceNodeCache, bnodeCache);

		return Triple.create(subject, predicate, object);
	}

	private static org.apache.jena.graph.Node generateSubjectNode(final Node gdmNode,
	                                                              final ConcurrentHashMap<String, org.apache.jena.graph.Node> resourceNodeCache,
	                                                              final ConcurrentHashMap<Long, org.apache.jena.graph.Node> bnodeCache) throws DMPConverterException {

		return generateNode(gdmNode, TriplePosition.SUBJECT, resourceNodeCache, bnodeCache);
	}

	private static org.apache.jena.graph.Node generateObjectNode(final Node gdmNode,
	                                                             final ConcurrentHashMap<String, org.apache.jena.graph.Node> resourceNodeCache,
	                                                             final ConcurrentHashMap<Long, org.apache.jena.graph.Node> bnodeCache) throws DMPConverterException {

		return generateNode(gdmNode, TriplePosition.OBJECT, resourceNodeCache, bnodeCache);
	}

	private static org.apache.jena.graph.Node generateNode(final Node gdmNode,
	                                                       final TriplePosition triplePosition,
	                                                       final ConcurrentHashMap<String, org.apache.jena.graph.Node> resourceNodeCache,
	                                                       final ConcurrentHashMap<Long, org.apache.jena.graph.Node> bnodeCache) throws DMPConverterException {

		final NodeType gdmNodeType = gdmNode.getType();

		final org.apache.jena.graph.Node node;

		switch (gdmNodeType) {

			case Resource:

				final ResourceNode resourceNode = (ResourceNode) gdmNode;
				final String resourceURI = resourceNode.getUri();

				node = resourceNodeCache.computeIfAbsent(resourceURI, resourceURI1 -> NodeFactory.createURI(resourceURI1));

				break;
			case BNode:

				final Long nodeId = gdmNode.getId();

				node = bnodeCache.computeIfAbsent(nodeId, nodeId1 -> NodeFactory.createBlankNode());

				break;
			case Literal:

				if (TriplePosition.SUBJECT.equals(triplePosition)) {

					throw new DMPConverterException("a subject cannot be a literal");
				}

				final LiteralNode literalNode = (LiteralNode) gdmNode;

				node = NodeFactory.createBlankNode(literalNode.getValue());

				break;
			default:

				throw new DMPConverterException(String.format("cannot process node type '%s'", gdmNodeType.toString()));
		}

		return node;
	}

	private static org.apache.jena.graph.Node generatePredicate(final Predicate gdmPredicate,
	                                                            final ConcurrentHashMap<String, org.apache.jena.graph.Node> predicateCache) {

		final String predicateURI = gdmPredicate.getUri();

		return predicateCache.computeIfAbsent(predicateURI, predicateURI1 -> NodeFactory.createURI(predicateURI1));
	}
}
