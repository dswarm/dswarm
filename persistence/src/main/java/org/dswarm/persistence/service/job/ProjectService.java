package org.dswarm.persistence.service.job;

import java.util.Set;

import javax.persistence.EntityManager;

import com.google.inject.Inject;
import com.google.inject.Provider;

import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.job.Function;
import org.dswarm.persistence.model.job.Mapping;
import org.dswarm.persistence.model.job.Project;
import org.dswarm.persistence.model.job.proxy.ProxyProject;
import org.dswarm.persistence.model.resource.DataModel;
import org.dswarm.persistence.service.ExtendedBasicDMPJPAService;

/**
 * A persistence service for {@link Project}s.
 * 
 * @author tgaengler
 */
public class ProjectService extends ExtendedBasicDMPJPAService<ProxyProject, Project> {

	/**
	 * Creates a new project persistence service with the given entity manager provider.
	 * 
	 * @param entityManagerProvider an entity manager provider
	 */
	@Inject
	public ProjectService(final Provider<EntityManager> entityManagerProvider) {

		super(Project.class, ProxyProject.class, entityManagerProvider);
	}

	/**
	 * {@inheritDoc}<br/>
	 * Clears the relationship to the input data model, output data model, mappings and functions.
	 */
	@Override
	protected void prepareObjectForRemoval(final Project object) {

		// should clear the relationship to the input data model, output data model, mappings and functions
		object.setInputDataModel(null);
		object.setOutputDataModel(null);
		object.setMappings(null);
		object.setFunctions(null);
	}

	/**
	 * {@inheritDoc}
	 */
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
