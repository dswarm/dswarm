package de.avgl.dmp.persistence.service.internal.graph.read;

import java.util.Map;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterable;

import com.google.common.collect.Maps;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

import de.avgl.dmp.persistence.service.internal.graph.GraphStatics;

/**
 * @author tgaengler
 */
public class PropertyGraphReader implements RDFReader {

	private NodeHandler					nodeHandler;
	private RelationshipHandler			relationshipHandler;

	private final String				recordClassUri;
	private final String				resourceGraphUri;

	private final GraphDatabaseService	database;

	private Model						model;

	public PropertyGraphReader(final String recordClassUriArg, final String resourceGraphUriArg, final GraphDatabaseService databaseArg) {

		recordClassUri = recordClassUriArg;
		resourceGraphUri = resourceGraphUriArg;
		database = databaseArg;
		nodeHandler = new CBDNodeHandler();
		relationshipHandler = new CBDRelationshipHandler();
	}

	@Override
	public Model read() {

		final Label recordClassLabel = DynamicLabel.label(recordClassUri);

		final ResourceIterable<Node> recordNodes = database.findNodesByLabelAndProperty(recordClassLabel, GraphStatics.PROVENANCE_PROPERTY,
				resourceGraphUri);

		if (recordNodes == null) {

			return null;
		}

		model = ModelFactory.createDefaultModel();

		for (final Node recordNode : recordNodes) {

			nodeHandler.handleNode(recordNode);
		}

		return model;
	}

	private class CBDNodeHandler implements NodeHandler {

		@Override
		public void handleNode(Node node) {

			// TODO: find a better way to determine the end of a resource description, e.g., add a property "resource" to each
			// node that holds the uri of the resource (record)

			if (node.hasProperty(GraphStatics.URI_PROPERTY)) {

				Iterable<Relationship> relationships = database.traversalDescription().traverse(node).relationships();

				for (final Relationship relationship : relationships) {

					relationshipHandler.handleRelationship(relationship);
				}
			}
		}
	}

	private class CBDRelationshipHandler implements RelationshipHandler {

		final Map<String, Resource>	bnodes		= Maps.newHashMap();
		final Map<String, Resource>	resources	= Maps.newHashMap();

		@Override
		public void handleRelationship(Relationship rel) {

			if (rel.getProperty(GraphStatics.PROVENANCE_PROPERTY).equals(resourceGraphUri)) {

				final String subject = (String) rel.getStartNode().getProperty(GraphStatics.URI_PROPERTY, null);

				final Resource subjectResource;

				if (subject == null) {

					// subject is a bnode

					final String subjectBNode = (String) rel.getStartNode().getProperty(GraphStatics.BNODE_PROPERTY, null);
					subjectResource = createResourceFromBNode(subjectBNode);
				} else {

					subjectResource = createResourceFromURI(subject);
				}

				final String predicate = (String) rel.getProperty(GraphStatics.URI_PROPERTY, null);
				final Property predicateProperty = model.createProperty(predicate);

				final String object;

				final String objectURI = (String) rel.getEndNode().getProperty(GraphStatics.URI_PROPERTY, null);

				final Resource objectResource;

				if (objectURI != null) {
					
					// object is a resource

					object = objectURI;
					objectResource = createResourceFromURI(object);
				} else {

					// check, whether object is a bnode

					final String objectBNode = (String) rel.getEndNode().getProperty(GraphStatics.BNODE_PROPERTY, null);

					if (objectBNode != null) {
						
						// object is a bnode

						objectResource = createResourceFromBNode(objectBNode);

					} else {

						// object is a literal node

						object = (String) rel.getEndNode().getProperty(GraphStatics.VALUE_PROPERTY, null);

						model.add(subjectResource, predicateProperty, object);

						return;
					}
				}

				model.add(subjectResource, predicateProperty, objectResource);

				// continue traversal with object node
				nodeHandler.handleNode(rel.getEndNode());
			}
		}

		private Resource createResourceFromBNode(final String bnodeId) {

			if (!bnodes.containsKey(bnodeId)) {

				bnodes.put(bnodeId, model.createResource());
			}

			return bnodes.get(bnodeId);
		}

		private Resource createResourceFromURI(final String uri) {

			if (!resources.containsKey(uri)) {

				resources.put(uri, model.createResource(uri));
			}

			return resources.get(uri);
		}
	}
}
