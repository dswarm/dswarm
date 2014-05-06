package de.avgl.dmp.persistence.service;

/**
 * A factory for internal model service.
 * 
 * @author tgaengler
 */
public interface InternalModelServiceFactory {

	// <TYPE extends Model> InternalService<TYPE> create(TypeLiteral<TYPE> typeLiteral);

	/**
	 * Gets the triple internal model service implementation.
	 * 
	 * @return the triple internal model service implementation
	 */
	/* @Named("Triple") */InternalModelService getInternalTripleService();

	/**
	 * Gets the RDFgraph internal model service implementation.
	 * 
	 * @return the RDF graph internal model service implementation
	 */
	/* @Named("Triple") */InternalModelService getInternalRDFGraphService();

	/**
	 * Gets the GDM graph internal model service implementation.
	 * 
	 * @return the GDM graph internal model service implementation
	 */
	/* @Named("Triple") */InternalModelService getInternalGDMGraphService();
}
