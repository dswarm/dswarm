package de.avgl.dmp.persistence.model.internal;

import java.util.Map;

import com.google.common.base.Optional;
import com.google.common.collect.Table;


public interface MemoryDb<A, B, C> {

	void put(A resourceId, B configurationId, C value);

	Map<B, C> get(A resourceId);

	Optional<C> get(A resourceId, B configurationId);

	void delete(A resourceId, B configurationId);

	Table<A, B, C> underlying();
}
