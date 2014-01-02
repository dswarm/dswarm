package de.avgl.dmp.persistence.service;

/**
 * @author tgaengler
 */
public interface InternalModelServiceFactory {

	// <TYPE extends Model> InternalService<TYPE> create(TypeLiteral<TYPE> typeLiteral);

	/* @Named("MemoryDb") */InternalModelService getMemoryDbInternalService();

	/* @Named("Triple") */InternalModelService getInternalTripleService();

}
