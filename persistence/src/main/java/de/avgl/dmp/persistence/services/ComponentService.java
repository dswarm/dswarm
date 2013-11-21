package de.avgl.dmp.persistence.services;

import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;

import com.google.inject.Inject;
import com.google.inject.Provider;

import de.avgl.dmp.persistence.DMPPersistenceException;
import de.avgl.dmp.persistence.model.job.Component;
import de.avgl.dmp.persistence.model.job.Function;

public class ComponentService extends BasicDMPJPAService<Component> {

	@Inject
	public ComponentService(final Provider<EntityManager> entityManagerProvider) {

		super(Component.class, entityManagerProvider);
	}

	@Override
	protected void prepareObjectForRemoval(final Component object) {

		// release connections to other objects
		object.setFunction(null);
		object.setInputComponents(null);
		object.setOutputComponents(null);
	}

	@Override
	protected void updateObjectInternal(final Component object, final Component updateObject, final EntityManager entityManager)
			throws DMPPersistenceException {

		final Function function = object.getFunction();
		final Set<Component> inputComponents = object.getInputComponents();
		final Set<Component> outputComponents = object.getOutputComponents();
		final Map<String, String> parameterMappings = object.getParameterMappings();

		updateObject.setFunction(function);
		updateObject.setInputComponents(inputComponents);
		updateObject.setOutputComponents(outputComponents);
		updateObject.setParameterMapping(parameterMappings);

		super.updateObjectInternal(object, updateObject, entityManager);
	}

}
