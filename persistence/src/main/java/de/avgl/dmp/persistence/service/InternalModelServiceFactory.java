package de.avgl.dmp.persistence.service;

/**
 * A factory for internal model service.
 * 
 * @author tgaengler
 */
public interface InternalModelServiceFactory {

	// <TYPE extends Model> InternalService<TYPE> create(TypeLiteral<TYPE> typeLiteral);

	/**
	 * Gets the memory DB internal model service implementation.
	 * 
	 * @return the memory DB internal model service implementation
	 */
	/* @Named("MemoryDb") */InternalModelService getMemoryDbInternalService();

	/**
	 * Gets the triple internal model service implementation.
	 * 
	 * @return the triple internal model service implementation
	 */
	/* @Named("Triple") */InternalModelService getInternalTripleService();

}
