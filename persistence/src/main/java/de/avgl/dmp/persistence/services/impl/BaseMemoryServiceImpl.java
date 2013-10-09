package de.avgl.dmp.persistence.services.impl;

import com.google.common.base.Optional;

import de.avgl.dmp.persistence.model.internal.MemoryDb;
import de.avgl.dmp.persistence.model.internal.impl.MemoryDbImpl;

abstract public class BaseMemoryServiceImpl<A, B, C> {

	protected final MemoryDb<A, B, C> db;

	protected BaseMemoryServiceImpl() {
		db = new MemoryDbImpl<A, B, C>();
	}

	public void createObject(A id1, B id2, C schema) {
		db.put(id1, id2, schema);
	}

	public Optional<C> getObjects(A id1, B id2) {
		return db.get(id1, id2);
	}

	public void deleteObject(A id1, B id2) {
		db.delete(id1, id2);
	}
}
