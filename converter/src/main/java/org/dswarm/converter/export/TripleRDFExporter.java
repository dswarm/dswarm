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
package org.dswarm.converter.export;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.system.StreamRDF;
import org.dswarm.converter.DMPConverterException;
import org.dswarm.graph.json.Predicate;
import org.dswarm.graph.json.Statement;

import javax.ws.rs.core.MediaType;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by tgaengler on 07.03.16.
 */
public class TripleRDFExporter extends RDFExporter<Triple> {

	public TripleRDFExporter(final MediaType mediaTypeArg) {

		super(mediaTypeArg);
	}

	@Override
	protected Triple generateTuple(final Statement statement,
	                               final ConcurrentHashMap<String, Node> resourceNodeCache,
	                               final ConcurrentHashMap<Long, Node> bnodeCache,
	                               final ConcurrentHashMap<String, Node> predicateCache) throws DMPConverterException {

		return generateTriple(statement, resourceNodeCache, bnodeCache, predicateCache);
	}

	@Override
	protected void writeTuple(final Triple tuple,
	                          final StreamRDF writer) {

		writer.triple(tuple);
	}

	private static Triple generateTriple(final Statement statement,
	                                     final ConcurrentHashMap<String, Node> resourceNodeCache,
	                                     final ConcurrentHashMap<Long, org.apache.jena.graph.Node> bnodeCache,
	                                     final ConcurrentHashMap<String, org.apache.jena.graph.Node> predicateCache) throws DMPConverterException {

		final org.dswarm.graph.json.Node gdmSubject = statement.getSubject();
		final Predicate gdmPredicate = statement.getPredicate();
		final org.dswarm.graph.json.Node gdmObject = statement.getObject();

		final org.apache.jena.graph.Node subject = generateSubjectNode(gdmSubject, resourceNodeCache, bnodeCache);
		final org.apache.jena.graph.Node predicate = generatePredicate(gdmPredicate, predicateCache);
		final org.apache.jena.graph.Node object = generateObjectNode(gdmObject, resourceNodeCache, bnodeCache);

		return Triple.create(subject, predicate, object);
	}
}
