package de.avgl.dmp.persistence.services;

import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.google.inject.Inject;
import com.google.inject.Provider;

import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.job.Component;
import de.avgl.dmp.persistence.model.job.Transformation;

/**
 * @author tgaengler
 */
public class TransformationService extends BasicFunctionService<Transformation> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(TransformationService.class);

	@Inject
	public TransformationService(final Provider<EntityManager> entityManagerProvider) {

		super(Transformation.class, entityManagerProvider);
	}

	@Override
	protected void prepareObjectForRemoval(final Transformation object) {

		final Set<Component> components = object.getComponents();

		if (components != null) {

			for (final Component component : components) {

				// release functions from components of a transformation
				// and disconnect components from each other

				component.setFunction(null);
				component.setInputComponents(null);
				component.setOutputComponents(null);
			}
		}

		LOG.debug("transformation after prepare for removal: " + ToStringBuilder.reflectionToString(object));
	}

	@Override
	protected void updateObjectInternal(final Transformation object, final Transformation updateObject, final EntityManager entityManager)
			throws DMPPersistenceException {

		super.updateObjectInternal(object, updateObject, entityManager);

		final Set<Component> components = object.getComponents();

		updateObject.setComponents(components);
	}

}
