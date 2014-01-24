package de.avgl.dmp.persistence.service.job;

import java.util.Set;

import javax.persistence.EntityManager;

import com.google.inject.Inject;
import com.google.inject.Provider;

import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.job.Component;
import de.avgl.dmp.persistence.model.job.Filter;
import de.avgl.dmp.persistence.model.job.Mapping;
import de.avgl.dmp.persistence.model.job.proxy.ProxyMapping;
import de.avgl.dmp.persistence.model.schema.AttributePath;
import de.avgl.dmp.persistence.service.BasicDMPJPAService;

/**
 * A persistence service for {@link Mapping}s.
 * 
 * @author tgaengler
 */
public class MappingService extends BasicDMPJPAService<ProxyMapping, Mapping> {

	/**
	 * Creates a new mapping persistence service with the given entity manager provider.
	 * 
	 * @param entityManagerProvider an entity manager provider
	 */
	@Inject
	public MappingService(final Provider<EntityManager> entityManagerProvider) {

		super(Mapping.class, entityManagerProvider);
	}

	/**
	 * {@inheritDoc}<br/>
	 * Clears the relationship to the input attribute paths, output attribute path, input filter and output filter.
	 */
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

	/**
	 * {@inheritDoc}
	 */
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
