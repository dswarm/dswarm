package de.avgl.dmp.persistence.services.impl;

import java.util.Map;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.google.inject.Singleton;

import de.avgl.dmp.persistence.services.InternalService;


@Singleton
public class InternalServiceImpl extends BaseMemoryServiceImpl<Long, Long, Table<String, String, String>> implements InternalService {

	@Override
	public void createObject(Long id, Long id1, String subject, String predicate, String object) {
		synchronized (this) {
			final Optional<Table<String, String, String>> tableOptional = getObjects(id, id1);
			final Table<String, String, String> tab = tableOptional.or(HashBasedTable.<String, String, String>create());

			tab.put(subject, predicate, object);
			createObject(id, id1, tab);
		}
	}

	@Override
	public Optional<Map<String, Map<String, String>>> getObjects(Long id, Long configurationId, Optional<Integer> atMost) {
		final Optional<Table<String, String, String>> maybeTable = getObjects(id, configurationId);

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

	@Override
	public Optional<Set<String>> getSchema(Long id, Long configurationId) {
		synchronized (this) {
			final Optional<Table<String, String, String>> tab = getObjects(id, configurationId);
			return tab.transform(new Function<Table<String, String, String>, Set<String>>() {
				@Override
				public Set<String> apply(Table<String, String, String> input) {
					return input.columnKeySet();
				}
			});
		}
	}
}
