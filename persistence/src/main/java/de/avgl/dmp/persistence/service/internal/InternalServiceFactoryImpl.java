package de.avgl.dmp.persistence.service.internal;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.avgl.dmp.persistence.service.InternalModelService;
import de.avgl.dmp.persistence.service.InternalModelServiceFactory;
import de.avgl.dmp.persistence.service.internal.graph.InternalGDMGraphService;

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
