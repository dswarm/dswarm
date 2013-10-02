package de.avgl.dmp.controller.factories;

import org.glassfish.hk2.api.Factory;

import de.avgl.dmp.persistence.model.internal.InternalMemoryDb;
import de.avgl.dmp.persistence.services.InternalService;

public class InternalServiceFactory implements Factory<InternalService> {

	private final	InternalMemoryDb		memoryDb;

	private InternalService inst = null;

	public InternalServiceFactory(InternalMemoryDb memoryDb) {
		this.memoryDb = memoryDb;
	}

	@Override
	public InternalService provide() {
		if (null == inst) {
			System.out.println("create internal service inst");
			inst = new InternalService();
			inst.setMemoryDb(memoryDb);
		}

		return inst;
	}

	@Override
	public void dispose(InternalService instance) {}
}
