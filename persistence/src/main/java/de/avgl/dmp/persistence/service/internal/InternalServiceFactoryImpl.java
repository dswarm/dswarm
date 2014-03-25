package de.avgl.dmp.persistence.service.internal;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.avgl.dmp.persistence.service.InternalModelService;
import de.avgl.dmp.persistence.service.InternalModelServiceFactory;
import de.avgl.dmp.persistence.service.internal.graph.InternalGDMGraphService;
import de.avgl.dmp.persistence.service.internal.graph.InternalRDFGraphService;
import de.avgl.dmp.persistence.service.internal.memorydb.InternalMemoryDbService;
import de.avgl.dmp.persistence.service.internal.triple.InternalTripleService;

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
	 * The RDF graph internal model service implementation.
	 */
	private final InternalRDFGraphService	internalRDFGraphService;

	/**
	 * The GDM graph internal model service implementation.
	 */
	private final InternalGDMGraphService	internalGDMGraphService;

	/**
	 * Creates a new internal model service factory with the given memory DB and triple internal model service implementations.
	 * 
	 * @param internalMemoryDbService the memory DB internal model service implementation
	 * @param internalTripleService the triple internal model service implementation
	 */
	@Inject
	public InternalServiceFactoryImpl(final InternalMemoryDbService internalMemoryDbService, final InternalTripleService internalTripleService,
			final InternalRDFGraphService internalRDFGraphService, final InternalGDMGraphService internalGDMGraphService) {

		this.internalMemoryDbService = internalMemoryDbService;
		this.internalTripleService = internalTripleService;
		this.internalRDFGraphService = internalRDFGraphService;
		this.internalGDMGraphService = internalGDMGraphService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Deprecated
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InternalModelService getInternalRDFGraphService() {

		return internalRDFGraphService;
	}

	@Override
	public InternalModelService getInternalGDMGraphService() {

		return internalGDMGraphService;
	}
}
