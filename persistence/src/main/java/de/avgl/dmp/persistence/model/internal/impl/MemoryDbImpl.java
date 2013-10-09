package de.avgl.dmp.persistence.model.internal.impl;

import java.util.Map;

import com.google.common.base.Optional;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;

import de.avgl.dmp.persistence.model.internal.MemoryDb;


public class MemoryDbImpl<A, B, C> implements MemoryDb<A, B, C> {

	private Table<A, B, C> table = HashBasedTable.create();

	@Override
	public void put(final A resourceId, final B configurationId, final C value) {
		synchronized (this) {
			table.put(resourceId, configurationId, value);
		}
	}

	@Override
	public Map<B, C> get(final A resourceId) {
		synchronized (this) {
			return table.row(resourceId);
		}
	}

	@Override
	public Optional<C> get(final A resourceId, final B configurationId) {
		synchronized (this) {
			return Optional.fromNullable(table.get(resourceId, configurationId));
		}
	}

	@Override
	public void delete(final A resourceId, final B configurationId) {
		synchronized (this) {
			table.remove(resourceId, configurationId);
		}
	}

	@Override
	public Table<A, B, C> underlying() {
		final ImmutableTable.Builder<A, B, C> builder = ImmutableTable.builder();

		for (final Table.Cell<A, B, C> cell : table.cellSet()) {
			final C underlyingC = underlyingC(cell.getValue());
			builder.put(cell.getRowKey(), cell.getColumnKey(), underlyingC);
		}

		return builder.build();
	}

	protected C underlyingC(final C rowValue) {
		return rowValue;
	}

}
