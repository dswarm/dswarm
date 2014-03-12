package de.avgl.dmp.persistence.service.internal.graph.read;

import com.hp.hpl.jena.rdf.model.Model;

public interface RDFReader {
	
	public Model read();
}
