/**
 * Copyright (C) 2013, 2014 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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
package org.dswarm.persistence.jpa;

import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import org.hibernate.AssertionFailure;
import org.hibernate.event.spi.EventSource;
import org.hibernate.pretty.MessageHelper;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * FixedEventCache is a Map implementation that can be used by an event
 * listener to keep track of entities involved in the operation
 * being performed.
 *
 * This is a rough copy of {@link org.hibernate.event.internal.EventCache},
 * except for {@link #put(Object, Object, boolean)}, which does not check the
 * entity's state in this implementation.
 */
final class FixedEventCache implements Map<Object, Object> {
	private final EventSource session;

	// key is an entity involved with the operation performed by the listener;
	// value can be either a copy of the entity or the entity itself
	private final Map<Object, Object> entityToCopyMap = new IdentityHashMap<>(10);

	// maintains the inverse of the entityToCopyMap for performance reasons.
	private final Map<Object, Object> copyToEntityMap = new IdentityHashMap<>(10);

	// key is an entity involved with the operation performed by the listener;
	// value is a flag indicating if the listener explicitly operates on the entity
	private final Map<Object, Boolean> entityToOperatedOnFlagMap = new IdentityHashMap<>(10);

	FixedEventCache(final EventSource session) {
		this.session = session;
	}

	@Override
	public void clear() {
		entityToCopyMap.clear();
		copyToEntityMap.clear();
		entityToOperatedOnFlagMap.clear();
	}

	@Override
	public boolean containsKey(final Object entity) {
		checkNotNull(entity, "null entities are not supported by %s", getClass().getName());
		return entityToCopyMap.containsKey(entity);
	}

	@Override
	public boolean containsValue(final Object copy) {
		checkNotNull(copy, "null copies are not supported by %s", getClass().getName());
		return copyToEntityMap.containsKey(copy);
	}

	@Override
	@Nonnull
	public Set<Entry<Object, Object>> entrySet() {
		return Collections.unmodifiableSet(entityToCopyMap.entrySet());
	}

	@Override
	public Object get(final Object entity) {
		checkNotNull(entity, "null entities are not supported by %s", getClass().getName());
		return entityToCopyMap.get(entity);
	}

	@Override
	public boolean isEmpty() {
		return entityToCopyMap.isEmpty();
	}

	@Override
	@Nonnull
	public Set<Object> keySet() {
		return Collections.unmodifiableSet(entityToCopyMap.keySet());
	}

	@Override
	public Object put(final Object entity, final Object copy) {
		return put(entity, copy, false);
	}

	Object put(final Object entity, final Object copy, final boolean isOperatedOn) {
		checkNotNull(entity, "null entities are not supported by %s", getClass().getName());
		checkNotNull(copy, "null copies are not supported by %s", getClass().getName());
		final Object oldCopy = entityToCopyMap.put(entity, copy);
		entityToOperatedOnFlagMap.put(entity, isOperatedOn);
		copyToEntityMap.put(copy, entity);
		return oldCopy;
	}

	@Override
	public void putAll(@Nonnull final Map<?, ?> map) {
		for (final Entry<?, ?> entry : map.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}

	@SuppressWarnings("ObjectEquality")
	@Override
	public Object remove(final Object entity) {
		checkNotNull(entity, "null entities are not supported by %s", getClass().getName());

		final Boolean oldOperatedOn = entityToOperatedOnFlagMap.remove(entity);
		final Object oldCopy = entityToCopyMap.remove(entity);
		final Object oldEntity = oldCopy != null ? copyToEntityMap.remove(oldCopy) : null;
		if(oldCopy == null) {
			checkState(oldOperatedOn == null, "Removed entity %s from FixedEventCache#entityToOperatedOnFlagMap, but FixedEventCache#entityToCopyMap did not contain the entity.", printEntity(entity));
		} else {
			checkState(oldEntity != null, "Removed entity %s from FixedEventCache#entityToCopyMap, but FixedEventCache#copyToEntityMap did not contain the entity.", printEntity(entity));
			checkState(oldOperatedOn != null, "FixedEventCache#entityToCopyMap contained an entity %s, but FixedEventCache#entityToOperatedOnFlagMap did not.", printEntity(entity));
			checkState(oldEntity == entity, "An entity copy %s was associated with a different entity %s than provided %s.", printEntity(oldCopy), printEntity(oldEntity), printEntity(entity));
		}

		return oldCopy;
	}

	@Override
	public int size() {
		return entityToCopyMap.size();
	}

	@Override
	@Nonnull
	public Collection<Object> values() {
		return Collections.unmodifiableCollection(entityToCopyMap.values());
	}

	boolean isOperatedOn(final Object entity) {
		checkNotNull(entity, "null entities are not supported by %s", getClass().getName());
		return entityToOperatedOnFlagMap.get(entity);
	}

	void setOperatedOn(final Object entity, final boolean isOperatedOn) {
		checkNotNull(entity, "null entities are not supported by %s", getClass().getName());
		hAssert(entityToOperatedOnFlagMap.containsKey(entity) && entityToCopyMap.containsKey(entity),
				"called IdentityEventCache#setOperatedOn() for entity not found in IdentityEventCache");
		entityToOperatedOnFlagMap.put(entity, isOperatedOn);
	}

	Map<Object, Object> invertMap() {
		return Collections.unmodifiableMap(copyToEntityMap);
	}

	private String printEntity(final Object entity) {
		return session.getPersistenceContext().getEntry(entity) != null ?
				MessageHelper.infoString(session.getEntityName(entity), session.getIdentifier(entity)) :
				// Entity was not found in current persistence context. Use Object#toString() method.
				"[" + entity + "]";
	}

	static void hAssert(final boolean condition, final String message) {
		if (!condition) {
			throw new AssertionFailure(message);
		}
	}
}
