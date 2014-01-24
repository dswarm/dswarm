package de.avgl.dmp.controller.resources.job;

import de.avgl.dmp.controller.resources.ExtendedBasicDMPResource;
import de.avgl.dmp.controller.resources.job.utils.BasicFunctionsResourceUtils;
import de.avgl.dmp.controller.status.DMPStatus;
import de.avgl.dmp.persistence.model.job.Function;
import de.avgl.dmp.persistence.model.job.proxy.ProxyBasicFunction;
import de.avgl.dmp.persistence.service.job.BasicFunctionService;

/**
 * A generic resource (controller service) for {@link Function}s.
 * 
 * @author tgaengler
 * @param <POJOCLASSPERSISTENCESERVICE> the concrete {@link Function} persistence service of the resource that is related to the
 *            concrete {@link Function} class
 * @param <POJOCLASS> the concrete {@link Function} class
 */
public abstract class BasicFunctionsResource<POJOCLASSRESOURCEUTILS extends BasicFunctionsResourceUtils<POJOCLASSPERSISTENCESERVICE, PROXYPOJOCLASS, POJOCLASS>, POJOCLASSPERSISTENCESERVICE extends BasicFunctionService<PROXYPOJOCLASS, POJOCLASS>, PROXYPOJOCLASS extends ProxyBasicFunction<POJOCLASS>, POJOCLASS extends Function>
		extends ExtendedBasicDMPResource<POJOCLASSRESOURCEUTILS, POJOCLASSPERSISTENCESERVICE, PROXYPOJOCLASS, POJOCLASS> {

	/**
	 * Creates a new resource (controller service) for the given concrete {@link Function} class with the provider of the concrete
	 * {@link Function} persistence service, the object mapper and metrics registry.
	 * 
	 * @param clasz a concrete {@link Function} class
	 * @param persistenceServiceProviderArg the concrete persistence service that is related to the concrete {@link Function}
	 *            class
	 * @param objectMapperArg an object mapper
	 * @param dmpStatusArg a metrics registry
	 */
	public BasicFunctionsResource(final POJOCLASSRESOURCEUTILS pojoClassResourceUtilsArg, final DMPStatus dmpStatusArg) {

		super(pojoClassResourceUtilsArg, dmpStatusArg);
	}

	/**
	 * {@inheritDoc}<br/>
	 * Updates the name, description, parameters and machine processable function description of the function.
	 */
	@Override
	protected POJOCLASS prepareObjectForUpdate(final POJOCLASS objectFromJSON, final POJOCLASS object) {

		super.prepareObjectForUpdate(objectFromJSON, object);

		object.setFunctionDescription(objectFromJSON.getFunctionDescription());
		object.setParameters(objectFromJSON.getParameters());

		return object;
	}
}
