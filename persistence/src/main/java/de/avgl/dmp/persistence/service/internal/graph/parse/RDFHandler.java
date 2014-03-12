package de.avgl.dmp.persistence.service.internal.graph.parse;

import com.hp.hpl.jena.rdf.model.Statement;

/**
 * @author tgaengler
 */
public interface RDFHandler {

	public void handleStatement(Statement st);
}
