package de.avgl.dmp.persistence.services;

import com.google.inject.name.Named;

/**
 * @author tgaengler
 */
public interface InternalServiceFactory {

	// <TYPE extends Model> InternalService<TYPE> create(TypeLiteral<TYPE> typeLiteral);

	/* @Named("MemoryDb") */InternalService getMemoryDbInternalService();

	/* @Named("Triple") */InternalService getInternalTripleService();

}
