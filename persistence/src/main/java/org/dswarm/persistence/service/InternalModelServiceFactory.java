package org.dswarm.persistence.service;

/**
 * A factory for internal model service.
 *
 * @author tgaengler
 */
public interface InternalModelServiceFactory {

	// <TYPE extends Model> InternalService<TYPE> create(TypeLiteral<TYPE> typeLiteral);

	/**
	 * Gets the GDM graph internal model service implementation.
	 *
	 * @return the GDM graph internal model service implementation
	 */
	/* @Named("Triple") */InternalModelService getInternalGDMGraphService();
}
