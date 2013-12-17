package de.avgl.dmp.persistence.service.resource;

import javax.persistence.EntityManager;

import com.google.inject.Inject;
import com.google.inject.Provider;

import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.resource.Configuration;
import de.avgl.dmp.persistence.model.resource.DataModel;
import de.avgl.dmp.persistence.model.resource.Resource;
import de.avgl.dmp.persistence.model.schema.Schema;
import de.avgl.dmp.persistence.service.ExtendedBasicDMPJPAService;

/**
 * @author tgaengler
 */
public class DataModelService extends ExtendedBasicDMPJPAService<DataModel> {

	@Inject
	public DataModelService(final Provider<EntityManager> entityManagerProvider) {

		super(DataModel.class, entityManagerProvider);
	}

	@Override
	protected void prepareObjectForRemoval(final DataModel object) {

		// should clear the relationship to the schema, data resource, configuration
		object.setSchema(null);
		object.setDataResource(null);
		object.setConfiguration(null);
	}

	@Override
	protected void updateObjectInternal(final DataModel object, final DataModel updateObject, final EntityManager entityManager)
			throws DMPPersistenceException {

		super.updateObjectInternal(object, updateObject, entityManager);

		final Schema schema = object.getSchema();
		final Resource resource = object.getDataResource();
		final Configuration configuration = object.getConfiguration();

		updateObject.setSchema(schema);
		updateObject.setDataResource(resource);
		updateObject.setConfiguration(configuration);
	}

}
