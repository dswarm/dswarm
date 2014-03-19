package de.avgl.dmp.persistence.service;

import de.avgl.dmp.persistence.service.internal.graph.InternalGraphService;

/**
 * A factory for internal model service.
 * 
 * @author tgaengler
 */
public interface InternalModelServiceFactory {

	// <TYPE extends Model> InternalService<TYPE> create(TypeLiteral<TYPE> typeLiteral);

	/**
	 * Gets the memory DB internal model service implementation. Note: this service is deprecated, please utilise
	 * {@link InternalGraphService} preferable.
	 * 
	 * @return the memory DB internal model service implementation
	 */
	@Deprecated
	/* @Named("MemoryDb") */InternalModelService getMemoryDbInternalService();

	/**
	 * Gets the triple internal model service implementation.
	 * 
	 * @return the triple internal model service implementation
	 */
	/* @Named("Triple") */InternalModelService getInternalTripleService();

	/**
	 * Gets the graph internal model service implementation.
	 * 
	 * @return the graph internal model service implementation
	 */
	/* @Named("Triple") */InternalModelService getInternalGraphService();
}
