package de.avgl.dmp.persistence.services;

import javax.persistence.EntityManager;

import com.google.inject.Provider;

import de.avgl.dmp.persistence.model.DMPJPAObject;

/**
 * 
 * @author tgaengler
 *
 * @param <POJOCLASS>
 */
public abstract class BasicIDJPAService<POJOCLASS extends DMPJPAObject> extends BasicJPAService<POJOCLASS, Long> {
	
	public BasicIDJPAService(final Class<POJOCLASS> clasz, final Provider<EntityManager> entityManagerProvider) {

		super(clasz, entityManagerProvider);
	}
}
