package de.avgl.dmp.persistence.service.internal.graph.read;

import org.neo4j.graphdb.Relationship;


/**
 * 
 * @author tgaengler
 *
 */
public interface RelationshipHandler {

	public void handleRelationship(Relationship rel);
}
