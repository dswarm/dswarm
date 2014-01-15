package de.avgl.dmp.controller.resources.resource.utils;

import javax.inject.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import de.avgl.dmp.controller.resources.utils.ExtendedBasicDMPResourceUtils;
import de.avgl.dmp.persistence.model.resource.DataModel;
import de.avgl.dmp.persistence.service.resource.DataModelService;

/**
 * @author tgaengler
 * @param <POJOCLASSPERSISTENCESERVICE>
 * @param <POJOCLASS>
 * @param <POJOCLASSIDTYPE>
 */
public class DataModelsResourceUtils extends ExtendedBasicDMPResourceUtils<DataModelService, DataModel> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(DataModelsResourceUtils.class);

	@Inject
	public DataModelsResourceUtils(final Provider<DataModelService> persistenceServiceProviderArg, final Provider<ObjectMapper> objectMapperProviderArg) {

		super(DataModel.class, persistenceServiceProviderArg, objectMapperProviderArg);
	}
}
