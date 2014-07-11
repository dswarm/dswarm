package org.dswarm.controller.resources.schema;

import org.dswarm.controller.resources.BasicDMPResource;
import org.dswarm.controller.resources.schema.utils.AttributePathInstancesResourceUtils;
import org.dswarm.controller.status.DMPStatus;
import org.dswarm.persistence.model.schema.AttributePathInstance;
import org.dswarm.persistence.model.schema.proxy.ProxyAttributePathInstance;
import org.dswarm.persistence.service.schema.AttributePathInstanceService;

/**
 * A generic resource (controller service) for {@link AttributePathInstance}s.
 * 
 * @author tgaengler
 * @param <POJOCLASSRESOURCEUTILS>
 * @param <POJOCLASSPERSISTENCESERVICE> the concrete {@link AttributePathInstance} persistence service of the resource that is
 *            related to the concrete {@link AttributePathInstance} class
 * @param <PROXYPOJOCLASS>
 * @param <POJOCLASS> the concrete {@link AttributePathInstance} class
 */
public abstract class AttributePathInstancesResource<POJOCLASSRESOURCEUTILS extends AttributePathInstancesResourceUtils<POJOCLASSPERSISTENCESERVICE, PROXYPOJOCLASS, POJOCLASS>, POJOCLASSPERSISTENCESERVICE extends AttributePathInstanceService<PROXYPOJOCLASS, POJOCLASS>, PROXYPOJOCLASS extends ProxyAttributePathInstance<POJOCLASS>, POJOCLASS extends AttributePathInstance>
		extends BasicDMPResource<POJOCLASSRESOURCEUTILS, POJOCLASSPERSISTENCESERVICE, PROXYPOJOCLASS, POJOCLASS> {

	/**
	 * Creates a new resource (controller service) for the given concrete {@link AttributePathInstance} class with the provider of
	 * the concrete {@link AttributePathInstance} persistence service, the object mapper and metrics registry.
	 * 
	 * @param pojoClassResourceUtilsArg
	 * @param dmpStatusArg
	 */
	public AttributePathInstancesResource(final POJOCLASSRESOURCEUTILS pojoClassResourceUtilsArg, final DMPStatus dmpStatusArg) {

		super(pojoClassResourceUtilsArg, dmpStatusArg);
	}

	/**
	 * {@inheritDoc}<br/>
	 */
	@Override
	protected POJOCLASS prepareObjectForUpdate(final POJOCLASS objectFromJSON, final POJOCLASS object) {

		super.prepareObjectForUpdate(objectFromJSON, object);

		object.setAttributePath(objectFromJSON.getAttributePath());

		return object;
	}
}
