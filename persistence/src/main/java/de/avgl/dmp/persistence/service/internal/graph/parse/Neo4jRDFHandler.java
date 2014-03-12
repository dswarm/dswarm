package de.avgl.dmp.persistence.service.internal.graph.parse;

import java.util.LinkedList;
import java.util.List;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

import de.avgl.dmp.persistence.service.internal.graph.GraphStatics;

/**
 * TODO: maybe we should add a general type for (bibliographic) resources (to easily identify the boundaries of the resources)
 * 
 * @author tgaengler
 */
public class Neo4jRDFHandler implements RDFHandler {

	private int							totalTriples		= 0;
	private int							addedNodes			= 0;
	private int							addedLabels			= 0;
	private int							addedRelationships	= 0;
	private int							sinceLastCommit		= 0;
	private int							i					= 0;
	private int							literals			= 0;

	private long						tick				= System.currentTimeMillis();
	private final GraphDatabaseService	database;
	private final Index<Node>			nodeIndex;
	private final Index<Relationship> relationshipIndex;

	private Transaction					tx;

	private final String				resourceGraphURI;

	public Neo4jRDFHandler(final GraphDatabaseService database, final String resourceGraphURIArg) {

		this.database = database;
		tx = database.beginTx();
		nodeIndex = database.index().forNodes("nodeIndex");
		relationshipIndex = database.index().forRelationships("relationshipIndex");

		resourceGraphURI = resourceGraphURIArg;
	}

	@Override
	public void handleStatement(final Statement st) {

		i++;

		System.out.println("handle statement " + i + ": " + st.toString());

		try {
			
			final Resource subject = st.getSubject();

			final Property predicate = st.getPredicate();
			final String predicateName = predicate.toString();

			final RDFNode object = st.getObject();

			// Check index for subject
			Node subjectNode;
			IndexHits<Node> hits = null;

			if (subject.isAnon()) {

				hits = nodeIndex.get(GraphStatics.BNODE, subject.toString());
			} else {

				hits = nodeIndex.get(GraphStatics.URI, subject.toString());
			}

			if (hits != null && hits.hasNext()) { // node exists
				
				subjectNode = hits.next();
			} else {

				subjectNode = database.createNode();

				if (subject.isAnon()) {

					// TODO: how unique is a generated bnode as identifier
					subjectNode.setProperty(GraphStatics.BNODE_PROPERTY, subject.toString());
					nodeIndex.add(subjectNode, GraphStatics.BNODE, subject.toString());
				} else {

					subjectNode.setProperty(GraphStatics.URI_PROPERTY, subject.toString());
					subjectNode.setProperty(GraphStatics.PROVENANCE_PROPERTY, resourceGraphURI);
					nodeIndex.add(subjectNode, GraphStatics.URI, subject.toString());
				}

				addedNodes++;
			}

			if (object.isLiteral()) {

				literals++;

				final Literal literal = (Literal) object;
				final RDFDatatype type = literal.getDatatype();
				Object value = literal.getValue();
				final Node objectNode = database.createNode();
				objectNode.setProperty(GraphStatics.VALUE_PROPERTY, value);

				if (type != null) {

					objectNode.setProperty(GraphStatics.DATATYPE_PROPERTY, type.getURI());
				}

				addedNodes++;

				final RelationshipType relType = DynamicRelationshipType.withName(predicateName);

				final Relationship rel = subjectNode.createRelationshipTo(objectNode, relType);
				rel.setProperty(GraphStatics.URI_PROPERTY, predicate.toString());
				rel.setProperty(GraphStatics.PROVENANCE_PROPERTY, resourceGraphURI);
				relationshipIndex.add(rel, GraphStatics.ID, Long.valueOf(rel.getId()));

				addedRelationships++;

			} else { // must be Resource
						// Make sure object exists

				// add Label if this is a type entry
				if (predicate.toString().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
					
					final Label label = DynamicLabel.label(object.asResource().toString());
					boolean hit = false;
					final Iterable<Label> labels = subjectNode.getLabels();
					final List<Label> labelList = new LinkedList<Label>();
					for (final Label lbl : labels) {
						if (label.equals(lbl)) {
							hit = true;
							break;
						}
						labelList.add(lbl);
					}
					if (!hit) {
						labelList.add(label);
						subjectNode.addLabel(label);
						addedLabels++;
					}
				}

				Node objectNode;

				IndexHits<Node> objectHits = null;

				if (object.isAnon()) {

					objectHits = nodeIndex.get(GraphStatics.BNODE, object.toString());
				} else {

					objectHits = nodeIndex.get(GraphStatics.URI, object.toString());
				}

				if (objectHits != null && objectHits.hasNext()) { // node exists
					
					objectNode = objectHits.next();
				} else {

					objectNode = database.createNode();

					if (object.isAnon()) {

						// TODO: how unique is a generated bnode as identifier
						objectNode.setProperty(GraphStatics.BNODE_PROPERTY, object.toString());
						nodeIndex.add(objectNode, GraphStatics.BNODE, object.toString());
					} else {

						objectNode.setProperty(GraphStatics.URI_PROPERTY, object.toString());
						nodeIndex.add(objectNode, GraphStatics.URI, object.toString());
					}

					addedNodes++;
				}

				final RelationshipType relType = DynamicRelationshipType.withName(predicateName);
				final Relationship rel = subjectNode.createRelationshipTo(objectNode, relType);
				rel.setProperty(GraphStatics.URI_PROPERTY, predicate.toString());
				rel.setProperty(GraphStatics.PROVENANCE_PROPERTY, resourceGraphURI);
				relationshipIndex.add(rel, GraphStatics.ID, Long.valueOf(rel.getId()));

				addedRelationships++;
				// }
			}

			totalTriples++;

			final long nodeDelta = totalTriples - sinceLastCommit;
			final long timeDelta = (System.currentTimeMillis() - tick) / 1000;

			if (nodeDelta >= 150000 || timeDelta >= 30) { // Commit every 150k operations or every 30 seconds
				
				tx.success();
				tx.close();
				tx = database.beginTx();

				sinceLastCommit = totalTriples;
				System.out.println(totalTriples + " triples @ ~" + (double) nodeDelta / timeDelta + " triples/second.");
				tick = System.currentTimeMillis();
			}
		} catch (final Exception e) {
			
			e.printStackTrace();
			tx.close();
			tx = database.beginTx();
		}
	}

	public int getCountedStatements() {

		return totalTriples;
	}

	public int getNodesAdded() {

		return addedNodes;
	}

	public int getRelationShipsAdded() {

		return addedRelationships;
	}

	public int getCountedLiterals() {

		return literals;
	}
}
