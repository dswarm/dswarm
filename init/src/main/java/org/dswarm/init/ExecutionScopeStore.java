/**
 * Copyright (C) 2013 – 2017 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dswarm.init;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

import com.google.common.collect.Maps;
import com.google.inject.Key;
import com.google.inject.Scopes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ExecutionScopeStore {

	private static final Logger LOG = LoggerFactory.getLogger(ExecutionScopeStore.class);

	private final Map<Key<?>,Object> store = Maps.newConcurrentMap();

	public <T> boolean has(final Key<T> key) {
		return store.containsKey(key);
	}

	@SuppressWarnings("unchecked")
	public <T> T get(final Key<T> key) {
		return (T) store.get(key);
	}

	public <T> T getOrElseUpdate(final Key<T> key, final Supplier<T> fallback) {
		final T value = get(key);
		if (value != null) {
			return value;
		}
		final T newValue = fallback.get();
		if (Scopes.isCircularProxy(newValue)) {
			return newValue;
		}
		set(key, newValue);
		return newValue;
	}

	public <T> void set(final Key<T> key, final T value) {
		final Object previous = store.putIfAbsent(key, value);
		if (previous != null) {
			LOG.warn("The key [{}] was already created in this scope. Current value: {}, Discarded value: {}", key, previous, value);
		}
	}
}
