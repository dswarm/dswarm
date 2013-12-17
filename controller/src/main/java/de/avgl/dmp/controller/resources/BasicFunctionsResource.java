package de.avgl.dmp.controller.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Provider;

import de.avgl.dmp.controller.status.DMPStatus;
import de.avgl.dmp.persistence.model.job.Function;
import de.avgl.dmp.persistence.service.job.BasicFunctionService;

/**
 * 
 * @author tgaengler
 *
 * @param <POJOCLASSPERSISTENCESERVICE>
 * @param <POJOCLASS>
 */
public abstract class BasicFunctionsResource<POJOCLASSPERSISTENCESERVICE extends BasicFunctionService<POJOCLASS>, POJOCLASS extends Function> extends
		ExtendedBasicDMPResource<POJOCLASSPERSISTENCESERVICE, POJOCLASS> {

	public BasicFunctionsResource(final Class<POJOCLASS> clasz, final Provider<POJOCLASSPERSISTENCESERVICE> persistenceServiceProviderArg,
			final ObjectMapper objectMapper, final DMPStatus dmpStatus) {

		super(clasz, persistenceServiceProviderArg, objectMapper, dmpStatus);
	}

	@Override
	protected POJOCLASS prepareObjectForUpdate(final POJOCLASS objectFromJSON, final POJOCLASS object) {

		super.prepareObjectForUpdate(objectFromJSON, object);

		object.setFunctionDescription(objectFromJSON.getFunctionDescription());
		object.setParameters(objectFromJSON.getParameters());

		return object;
	}
}
