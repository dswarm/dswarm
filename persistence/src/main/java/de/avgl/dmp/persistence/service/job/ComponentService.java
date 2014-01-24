package de.avgl.dmp.persistence.service.job;

import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;

import com.google.inject.Inject;
import com.google.inject.Provider;

import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.job.Component;
import de.avgl.dmp.persistence.model.job.Function;
import de.avgl.dmp.persistence.model.job.proxy.ProxyComponent;
import de.avgl.dmp.persistence.service.ExtendedBasicDMPJPAService;

/**
 * A persistence service for {@link Component}s.
 * 
 * @author tgaengler
 */
public class ComponentService extends ExtendedBasicDMPJPAService<ProxyComponent, Component> {

	/**
	 * Creates a new component persistence service with the given entity manager provider.
	 * 
	 * @param entityManagerProvider an entity manager provider
	 */
	@Inject
	public ComponentService(final Provider<EntityManager> entityManagerProvider) {

		super(Component.class, entityManagerProvider);
	}

	/**
	 * {@inheritDoc}<br/>
	 * Clears the relationship to the function, input components and output components.
	 */
	@Override
	protected void prepareObjectForRemoval(final Component object) {

		// release connections to other objects
		object.setFunction(null);
		object.setInputComponents(null);
		object.setOutputComponents(null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void updateObjectInternal(final Component object, final Component updateObject, final EntityManager entityManager)
			throws DMPPersistenceException {

		super.updateObjectInternal(object, updateObject, entityManager);

		final Function function = object.getFunction();
		final Set<Component> inputComponents = object.getInputComponents();
		final Set<Component> outputComponents = object.getOutputComponents();
		final Map<String, String> parameterMappings = object.getParameterMappings();
		// final Transformation transformation = object.getTransformation();

		updateObject.setFunction(function);
		updateObject.setInputComponents(inputComponents);
		updateObject.setOutputComponents(outputComponents);
		updateObject.setParameterMappings(parameterMappings);
		// updateObject.setTransformation(transformation);
	}

}
