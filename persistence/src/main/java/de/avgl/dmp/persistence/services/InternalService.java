package de.avgl.dmp.persistence.services;

import java.util.Map;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;

import de.avgl.dmp.persistence.model.internal.InternalMemoryDb;

public class InternalService {

	private InternalMemoryDb memoryDb;

	public void setMemoryDb(final InternalMemoryDb memoryDb) {
		this.memoryDb = memoryDb;
	}

	public void createObject(Long id, Long id1, String subject, String predicate, String object) {
		memoryDb.put(id, id1, subject, predicate, object);
	}

	public Optional<Map<String, Map<String, String>>> getObjects(Long id, Long configurationId, Optional<Integer> atMost) {
		final Optional<Table<String, String, String>> maybeTable = memoryDb.get(id, configurationId);

		if (maybeTable.isPresent()) {

			final Table<String, String, String> table = maybeTable.get();
			final Iterable<String> rows = atMost.isPresent() ? Iterables.limit(table.rowKeySet(), atMost.get()) : table.rowKeySet();

			final Map<String, Map<String, String>> finalMap = Maps.newHashMap();

			for (String row : rows) {
				final Map<String, String> recordMap = table.row(row);
				finalMap.put(row, recordMap);
			}

			return Optional.of(finalMap);
		}

		return Optional.absent();
	}

	public void deleteObject(Long id, Long configurationId) {
		memoryDb.delete(id, configurationId);
	}

	public Optional<Set<String>> getSchema(Long id, Long configurationId) {
		return memoryDb.schema(id, configurationId);
	}
}
