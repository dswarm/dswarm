package de.avgl.dmp.persistence.service.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.avgl.dmp.persistence.service.InternalModelService;
import de.avgl.dmp.persistence.service.InternalModelServiceFactory;


@Singleton
public class InternalServiceFactoryImpl implements InternalModelServiceFactory {
	
	private final InternalMemoryDbService internalMemoryDbService;
	private final InternalTripleService internalTripleService;
	
	@Inject
	public InternalServiceFactoryImpl(final InternalMemoryDbService internalMemoryDbService, final InternalTripleService internalTripleService) {
		
		this.internalMemoryDbService = internalMemoryDbService;
		this.internalTripleService = internalTripleService;
	}

	@Override
	public InternalModelService getMemoryDbInternalService() {
		
		return internalMemoryDbService;
	}

	@Override
	public InternalModelService getInternalTripleService() {
		
		return internalTripleService;
	}
}
