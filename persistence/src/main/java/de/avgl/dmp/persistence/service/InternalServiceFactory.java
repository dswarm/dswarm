package de.avgl.dmp.persistence.service;

import com.google.inject.name.Named;

/**
 * @author tgaengler
 */
public interface InternalServiceFactory {

	// <TYPE extends Model> InternalService<TYPE> create(TypeLiteral<TYPE> typeLiteral);

	/* @Named("MemoryDb") */InternalService getMemoryDbInternalService();

	/* @Named("Triple") */InternalService getInternalTripleService();

}
