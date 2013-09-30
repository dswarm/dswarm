package de.avgl.dmp.controller.factories;

import javax.persistence.EntityManager;

import org.glassfish.hk2.api.Factory;

import de.avgl.dmp.persistence.services.utils.JPAUtil;

public class DMPEntityManagerFactory implements Factory<EntityManager> {
	@Override
	public EntityManager provide() {

		return JPAUtil.getEntityManagerFactory().createEntityManager();
	}

	@Override
	public void dispose(EntityManager instance) {

		JPAUtil.closeEntityManager(instance);
	}
}

