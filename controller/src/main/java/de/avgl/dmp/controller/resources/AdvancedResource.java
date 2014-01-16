package de.avgl.dmp.controller.resources;

import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.avgl.dmp.controller.DMPControllerException;
import de.avgl.dmp.controller.resources.utils.AdvancedResourceUtils;
import de.avgl.dmp.controller.status.DMPStatus;
import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.BasicDMPJPAObject;
import de.avgl.dmp.persistence.model.BasicDMPObject;
import de.avgl.dmp.persistence.service.AdvancedJPAService;

/**
 * A generic resource (controller service) implementation for {@link BasicDMPJPAObject}s, i.e., objects where the identifier will
 * be generated by the database and that can have a name.
 * 
 * @author tgaengler
 * @param <POJOCLASSPERSISTENCESERVICE> the concrete persistence service of the resource that is related to the concrete POJO
 *            class
 * @param <POJOCLASS> the concrete POJO class of the resource
 */
public abstract class AdvancedResource<POJOCLASSRESOURCEUTILS extends AdvancedResourceUtils<POJOCLASSPERSISTENCESERVICE, POJOCLASS>, POJOCLASSPERSISTENCESERVICE extends AdvancedJPAService<POJOCLASS>, POJOCLASS extends BasicDMPObject>
		extends BasicResource<POJOCLASSRESOURCEUTILS, POJOCLASSPERSISTENCESERVICE, POJOCLASS, String> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(AdvancedResource.class);

	/**
	 * Creates a new resource (controller service) for the given concrete POJO class with the provider of the concrete persistence
	 * service, the object mapper and metrics registry.
	 * 
	 * @param clasz a concrete POJO class
	 * @param persistenceServiceProviderArg the concrete persistence service that is related to the concrete POJO class
	 * @param objectMapperArg an object mapper
	 * @param dmpStatusArg a metrics registry
	 */
	public AdvancedResource(final POJOCLASSRESOURCEUTILS pojoClassResourceUtilsArg, final DMPStatus dmpStatusArg) {

		super(pojoClassResourceUtilsArg, dmpStatusArg);
	}

	@Override
	public Response getObject(final String id) throws DMPControllerException {

		return Response.status(505).build();
	}

	@Override
	protected POJOCLASS prepareObjectForUpdate(final POJOCLASS objectFromJSON, final POJOCLASS object) {

		object.setName(objectFromJSON.getName());

		return object;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected POJOCLASS createObject(final POJOCLASS objectFromJSON, final POJOCLASSPERSISTENCESERVICE persistenceService)
			throws DMPPersistenceException {

		return persistenceService.createObjectTransactional(objectFromJSON.getId());
	}

	@Override
	protected String prepareObjectJSONString(String objectJSONString) throws DMPControllerException {

		// an attribute or clasz is not a complex object

		return objectJSONString;
	}

	@Override
	protected void checkObjectId(final JsonNode idNode, final ObjectNode objectJSON) {

		// TODO: handle string ids, if necessary
	}

	@Override
	protected ObjectNode addDummyId(final ObjectNode objectJSON) {

		// TODO: handle string ids, if necessary

		return objectJSON;
	}
}
