package de.avgl.dmp.persistence.services;

import javax.persistence.EntityManager;

import com.google.inject.Inject;
import com.google.inject.Provider;

import de.avgl.dmp.persistence.model.job.Function;

/**
 * 
 * @author tgaengler
 *
 */
public class FunctionService extends BasicFunctionService<Function> {

	@Inject
	public FunctionService(final Provider<EntityManager> entityManagerProvider) {

		super(Function.class, entityManagerProvider);
	}
}
