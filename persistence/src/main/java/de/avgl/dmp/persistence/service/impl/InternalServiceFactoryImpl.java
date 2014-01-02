package de.avgl.dmp.persistence.service.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.avgl.dmp.persistence.service.InternalModelService;
import de.avgl.dmp.persistence.service.InternalModelServiceFactory;

/**
 * An internal model service factory implementation.
 * 
 * @author tgaengler
 */
@Singleton
public class InternalServiceFactoryImpl implements InternalModelServiceFactory {

	/**
	 * The memory DB internal model service implementation.
	 */
	private final InternalMemoryDbService	internalMemoryDbService;

	/**
	 * The triple internal model service implementation.
	 */
	private final InternalTripleService		internalTripleService;

	/**
	 * Creates a new internal model service factory with the given memory DB and triple internal model service implementations.
	 * 
	 * @param internalMemoryDbService the memory DB internal model service implementation
	 * @param internalTripleService the triple internal model service implementation
	 */
	@Inject
	public InternalServiceFactoryImpl(final InternalMemoryDbService internalMemoryDbService, final InternalTripleService internalTripleService) {

		this.internalMemoryDbService = internalMemoryDbService;
		this.internalTripleService = internalTripleService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InternalModelService getMemoryDbInternalService() {

		return internalMemoryDbService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InternalModelService getInternalTripleService() {

		return internalTripleService;
	}
}
