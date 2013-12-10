package de.avgl.dmp.controller.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Provider;

import de.avgl.dmp.controller.status.DMPStatus;
import de.avgl.dmp.persistence.model.BasicDMPJPAObject;
import de.avgl.dmp.persistence.service.BasicDMPJPAService;

/**
 * @author tgaengler
 */
public abstract class BasicDMPResource<POJOCLASSPERSISTENCESERVICE extends BasicDMPJPAService<POJOCLASS>, POJOCLASS extends BasicDMPJPAObject>
		extends BasicResource<POJOCLASSPERSISTENCESERVICE, POJOCLASS, Long> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(BasicDMPResource.class);

	public BasicDMPResource(final Class<POJOCLASS> clasz, final Provider<POJOCLASSPERSISTENCESERVICE> dataModelServiceProviderArg,
			final ObjectMapper objectMapper, final DMPStatus dmpStatus) {

		super(clasz, dataModelServiceProviderArg, objectMapper, dmpStatus);
	}

	@Override
	protected POJOCLASS prepareObjectForUpdate(final POJOCLASS objectFromJSON, final POJOCLASS object) {

		object.setName(objectFromJSON.getName());

		return object;
	}
}
