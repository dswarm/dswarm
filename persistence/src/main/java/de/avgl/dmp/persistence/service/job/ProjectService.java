package de.avgl.dmp.persistence.service.job;

import java.util.Set;

import javax.persistence.EntityManager;

import com.google.inject.Inject;
import com.google.inject.Provider;

import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.job.Function;
import de.avgl.dmp.persistence.model.job.Mapping;
import de.avgl.dmp.persistence.model.job.Project;
import de.avgl.dmp.persistence.model.resource.DataModel;
import de.avgl.dmp.persistence.service.ExtendedBasicDMPJPAService;

/**
 * @author tgaengler
 */
public class ProjectService extends ExtendedBasicDMPJPAService<Project> {

	@Inject
	public ProjectService(final Provider<EntityManager> entityManagerProvider) {

		super(Project.class, entityManagerProvider);
	}

	@Override
	protected void prepareObjectForRemoval(final Project object) {

		// should clear the relationship to the input data model, output data model, mappings and functions
		object.setInputDataModel(null);
		object.setOutputDataModel(null);
		object.setMappings(null);
		object.setFunctions(null);
	}

	@Override
	protected void updateObjectInternal(final Project object, final Project updateObject, final EntityManager entityManager)
			throws DMPPersistenceException {

		super.updateObjectInternal(object, updateObject, entityManager);

		final DataModel inputDataModel = object.getInputDataModel();
		final DataModel outputDataModel = object.getOutputDataModel();
		final Set<Mapping> mappings = object.getMappings();
		final Set<Function> functions = object.getFunctions();

		updateObject.setInputDataModel(inputDataModel);
		updateObject.setOutputDataModel(outputDataModel);
		updateObject.setMappings(mappings);
		updateObject.setFunctions(functions);
	}

}
