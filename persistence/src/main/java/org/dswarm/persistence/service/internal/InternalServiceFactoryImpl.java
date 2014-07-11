package org.dswarm.persistence.service.internal;

import org.dswarm.persistence.service.InternalModelService;
import org.dswarm.persistence.service.InternalModelServiceFactory;
import org.dswarm.persistence.service.internal.graph.InternalGDMGraphService;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * An internal model service factory implementation.
 * 
 * @author tgaengler
 */
@Singleton
public class InternalServiceFactoryImpl implements InternalModelServiceFactory {

	/**
	 * The GDM graph internal model service implementation.
	 */
	private final InternalGDMGraphService	internalGDMGraphService;

	/**
	 * Creates a new internal model service factory with the given memory DB and triple internal model service implementations.
	 * 
	 * @param internalGDMGraphService the GDM graph internal model service implementation
	 */
	@Inject
	public InternalServiceFactoryImpl(final InternalGDMGraphService internalGDMGraphService) {

		this.internalGDMGraphService = internalGDMGraphService;
	}

	@Override
	public InternalModelService getInternalGDMGraphService() {

		return internalGDMGraphService;
	}
}
