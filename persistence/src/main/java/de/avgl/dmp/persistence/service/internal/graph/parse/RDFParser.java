package de.avgl.dmp.persistence.service.internal.graph.parse;



/**
 * 
 * @author tgaengler
 *
 */
public interface RDFParser {

	/**
	 * Sets the RDFHandler that will handle the parsed RDF data.
	 */
	public void setRDFHandler(RDFHandler handler);
	
	public void parse();
}
