package de.avgl.dmp.persistence.model.internal;

import java.util.Map;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;


public abstract class MemoryDb<A, B, S, P, O> {

	private Table<A, B, Table<S, P, O>> table = HashBasedTable.create();

	public void put(A resourceId, B configurationId, S subject, P predicate, O object) {
		synchronized (this) {
			Table<S, P, O> tab = table.get(resourceId, configurationId);
			if (tab == null) {
				tab = HashBasedTable.create();
			}
			tab.put(subject, predicate, object);
			table.put(resourceId, configurationId, tab);
		}
	}

	public Map<B, Table<S, P, O>> get(A resourceId) {
		synchronized (this) {
			return table.row(resourceId);
		}
	}

	public Optional<Table<S, P, O>> get(A resourceId, B configurationId) {
		synchronized (this) {
			return Optional.fromNullable(table.get(resourceId, configurationId));
		}
	}

	public Optional<Set<P>> schema(A resourceId, B configurationId) {
		synchronized (this) {
			final Table<S, P, O> tab = table.get(resourceId, configurationId);
			if (tab != null) {
				return Optional.of(tab.columnKeySet());
			}
			return Optional.absent();
		}
	}

	public void delete(A resourceId, B configurationId) {
		synchronized (this) {
			table.remove(resourceId, configurationId);
		}
	}
}
