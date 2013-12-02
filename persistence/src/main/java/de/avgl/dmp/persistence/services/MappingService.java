package de.avgl.dmp.persistence.services;

import java.util.Set;

import javax.persistence.EntityManager;

import com.google.inject.Inject;
import com.google.inject.Provider;

import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.job.AttributePath;
import de.avgl.dmp.persistence.model.job.Component;
import de.avgl.dmp.persistence.model.job.Filter;
import de.avgl.dmp.persistence.model.job.Mapping;

/**
 * @author tgaengler
 */
public class MappingService extends BasicDMPJPAService<Mapping> {

	@Inject
	public MappingService(final Provider<EntityManager> entityManagerProvider) {

		super(Mapping.class, entityManagerProvider);
	}

	@Override
	protected void prepareObjectForRemoval(final Mapping object) {

		// should clear the relationship to the input attribute paths, output attribute path, input filter, output filter +
		// transformation function
		object.setInputAttributePaths(null);
		object.setOutputAttributePath(null);
		object.setInputFilter(null);
		object.setOutputFilter(null);

		final Component transformation = object.getTransformation();

		if (transformation != null) {

			transformation.setFunction(null);
		}
	}

	@Override
	protected void updateObjectInternal(final Mapping object, final Mapping updateObject, final EntityManager entityManager)
			throws DMPPersistenceException {

		super.updateObjectInternal(object, updateObject, entityManager);
		
		final Set<AttributePath> inputAttributePaths = object.getInputAttributePaths();
		final AttributePath outputAttributePath = object.getOutputAttributePath();
		final Filter inputFilter = object.getInputFilter();
		final Filter outputFilter = object.getOutputFilter();
		final Component transformation = object.getTransformation();

		updateObject.setInputAttributePaths(inputAttributePaths);
		updateObject.setOutputAttributePath(outputAttributePath);
		updateObject.setInputFilter(inputFilter);
		updateObject.setOutputFilter(outputFilter);
		updateObject.setTransformation(transformation);
	}

}
