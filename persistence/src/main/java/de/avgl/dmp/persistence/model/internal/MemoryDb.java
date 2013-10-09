package de.avgl.dmp.persistence.model.internal;

import java.util.Map;

import com.google.common.base.Optional;
import com.google.common.collect.Table;


public interface MemoryDb<A, B, C> {

	public void put(A resourceId, B configurationId, C value);

	public Map<B, C> get(A resourceId);

	public Optional<C> get(A resourceId, B configurationId);

	public void delete(A resourceId, B configurationId);

	public Table<A, B, C> underlying();
}
