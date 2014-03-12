package de.avgl.dmp.persistence.service.internal.graph.read;

import org.neo4j.graphdb.Node;


/**
 * 
 * @author tgaengler
 *
 */
public interface NodeHandler {

	public void handleNode(Node node);
}
