package de.avgl.dmp.persistence.services;

import com.google.inject.name.Named;





public interface InternalServiceFactory {
	
	//<TYPE extends Model> InternalService<TYPE> create(TypeLiteral<TYPE> typeLiteral);
	
	/*@Named("MemoryDb")*/ InternalService getMemoryDbInternalService();
	/*@Named("Triple")*/ InternalService getInternalTripleService();

}
