package org.dswarm.persistence.service.job;

import java.util.Set;

import javax.persistence.EntityManager;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.job.Component;
import org.dswarm.persistence.model.job.Transformation;
import org.dswarm.persistence.model.job.proxy.ProxyTransformation;

/**
 * A persistence service for {@link Transformation}s.
 * 
 * @author tgaengler
 */
public class TransformationService extends BasicFunctionService<ProxyTransformation, Transformation> {

	private static final Logger	LOG	= LoggerFactory.getLogger(TransformationService.class);

	/**
	 * Creates a new transformation persistence service with the given entity manager provider.
	 * 
	 * @param entityManagerProvider an entity manager provider
	 */
	@Inject
	public TransformationService(final Provider<EntityManager> entityManagerProvider) {

		super(Transformation.class, ProxyTransformation.class, entityManagerProvider);
	}

	/**
	 * {@inheritDoc}<br/>
	 * Clears the relationship to the functions of the components and decouples the components from each other.
	 */
	@Override
	protected void prepareObjectForRemoval(final Transformation object) {

		final Set<Component> components = object.getComponents();

		if (components != null) {

			final Set<Component> componentsToBeDeleted = Sets.newCopyOnWriteArraySet(components);

			for (final Component component : componentsToBeDeleted) {

				// release functions from components of a transformation
				// and disconnect components from each other

				component.setFunction(null);
				component.setInputComponents(null);
				component.setOutputComponents(null);
			}
		}

		TransformationService.LOG.trace("transformation after prepare for removal: " + ToStringBuilder.reflectionToString(object));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void updateObjectInternal(final Transformation object, final Transformation updateObject, final EntityManager entityManager)
			throws DMPPersistenceException {

		super.updateObjectInternal(object, updateObject, entityManager);

		final Set<Component> components = object.getComponents();

		updateObject.setComponents(components);
	}

}
