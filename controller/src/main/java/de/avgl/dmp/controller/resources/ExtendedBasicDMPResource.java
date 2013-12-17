package de.avgl.dmp.controller.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Provider;

import de.avgl.dmp.controller.status.DMPStatus;
import de.avgl.dmp.persistence.model.ExtendedBasicDMPJPAObject;
import de.avgl.dmp.persistence.service.ExtendedBasicDMPJPAService;

/**
 * @author tgaengler
 */
public abstract class ExtendedBasicDMPResource<POJOCLASSPERSISTENCESERVICE extends ExtendedBasicDMPJPAService<POJOCLASS>, POJOCLASS extends ExtendedBasicDMPJPAObject>
		extends BasicDMPResource<POJOCLASSPERSISTENCESERVICE, POJOCLASS> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(ExtendedBasicDMPResource.class);

	public ExtendedBasicDMPResource(final Class<POJOCLASS> clasz, final Provider<POJOCLASSPERSISTENCESERVICE> persistenceServiceProviderArg,
			final ObjectMapper objectMapper, final DMPStatus dmpStatus) {

		super(clasz, persistenceServiceProviderArg, objectMapper, dmpStatus);
	}

	@Override
	protected POJOCLASS prepareObjectForUpdate(final POJOCLASS objectFromJSON, final POJOCLASS object) {

		super.prepareObjectForUpdate(objectFromJSON, object);
		
		object.setDescription(objectFromJSON.getDescription());

		return object;
	}
}
