package de.avgl.dmp.persistence.service.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.avgl.dmp.persistence.service.InternalService;
import de.avgl.dmp.persistence.service.InternalServiceFactory;


@Singleton
public class InternalServiceFactoryImpl implements InternalServiceFactory {
	
	private final InternalMemoryDbService internalMemoryDbService;
	private final InternalTripleService internalTripleService;
	
	@Inject
	public InternalServiceFactoryImpl(final InternalMemoryDbService internalMemoryDbService, final InternalTripleService internalTripleService) {
		
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
