package de.avgl.dmp.persistence.services.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.avgl.dmp.persistence.services.InternalService;
import de.avgl.dmp.persistence.services.InternalServiceFactory;


@Singleton
public class InternalServiceFactoryImpl implements InternalServiceFactory {
	
	private final InternalServiceImpl internalMemoryDbService;
	private final InternalTripleService internalTripleService;
	
	@Inject
	public InternalServiceFactoryImpl(final InternalServiceImpl internalMemoryDbService, final InternalTripleService internalTripleService) {
		
		this.internalMemoryDbService = internalMemoryDbService;
		this.internalTripleService = internalTripleService;
	}

	@Override
	public InternalService getMemoryDbInternalService() {
		
		return internalMemoryDbService;
	}

	@Override
	public InternalService getInternalTripleService() {
		
		return internalTripleService;
	}
}
