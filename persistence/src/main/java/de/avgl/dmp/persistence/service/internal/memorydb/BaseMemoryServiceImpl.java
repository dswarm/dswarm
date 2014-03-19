package de.avgl.dmp.persistence.service.internal.memorydb;

import com.google.common.base.Optional;

import de.avgl.dmp.persistence.model.internal.MemoryDb;
import de.avgl.dmp.persistence.model.internal.impl.MemoryDbImpl;

public class BaseMemoryServiceImpl<A, B, C> {

	private final MemoryDb<A, B, C> db;

	protected BaseMemoryServiceImpl() {
		db = new MemoryDbImpl<>();
	}

	public void createObject(final A id1, final B id2, final C schema) {
		db.put(id1, id2, schema);
	}

	public Optional<C> getObjects(final A id1, final B id2) {
		return db.get(id1, id2);
	}

	public void deleteObject(final A id1, final B id2) {
		db.delete(id1, id2);
	}
}
