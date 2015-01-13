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

import java.io.Serializable;
import java.sql.Blob;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import org.hibernate.HibernateException;
import org.hibernate.ObjectDeletedException;
import org.hibernate.Session;
import org.hibernate.StaleObjectStateException;
import org.hibernate.WrongClassException;
import org.hibernate.bytecode.instrumentation.spi.FieldInterceptor;
import org.hibernate.engine.internal.Cascade;
import org.hibernate.engine.internal.CascadePoint;
import org.hibernate.engine.spi.CascadingAction;
import org.hibernate.engine.spi.CascadingActions;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.EntityKey;
import org.hibernate.engine.spi.PersistenceContext;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.event.internal.AbstractSaveEventListener;
import org.hibernate.event.spi.EventSource;
import org.hibernate.event.spi.MergeEvent;
import org.hibernate.event.spi.MergeEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.hibernate.type.ForeignKeyDirection;
import org.hibernate.type.TypeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines the a copy event listener, that fixes hibernates default listener.
 *
 * This is a rough copy of {@link org.hibernate.event.internal.DefaultMergeEventListener},
 * only that this implementation call {@link #fixValues(Object[], Object...)}
 * before executing the cascading merge.
 */
final class FixedMergeEventListener extends AbstractSaveEventListener implements MergeEventListener {
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(FixedMergeEventListener.class);

	@Override
	public void onMerge(final MergeEvent event) throws HibernateException {
		final Map<?, ?> cache = new FixedEventCache(event.getSession());
		onMerge(event, cache);
		cache.clear();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void onMerge(final MergeEvent event, final Map copiedAlready) throws HibernateException {
		final FixedEventCache cache = (FixedEventCache) copiedAlready;
		final EventSource source = event.getSession();

		final Optional<Object> original = Optional.fromNullable(event.getOriginal());
		final Optional<Object> eventEntity = bind(original, getEventEntity(event, source));
		final Optional<Object> entity = bind(eventEntity, getMergeEntity(event, cache));

		if (entity.isPresent()) {
			final EntityState entityState = getMergeEntityState(entity.get(), event, source);

			switch (entityState) {
				case DETACHED:
					entityIsDetached(event, cache);
					break;
				case TRANSIENT:
					entityIsTransient(event, cache);
					break;
				case PERSISTENT:
					entityIsPersistent(event, cache);
					break;
				case DELETED:
					throw new ObjectDeletedException("deleted instance passed to merge", null, getLoggableName(event.getEntityName(), entity.get()));
			}
		}
	}

	@Override
	protected void cascadeAfterSave(final EventSource source, final EntityPersister persister, final Object entity, final Object anything) throws HibernateException {
	}

	@Override
	protected void cascadeBeforeSave(final EventSource source, final EntityPersister persister, final Object entity, final Object anything) throws HibernateException {
	}

	@Override
	protected Boolean getAssumedUnsaved() {
		return Boolean.FALSE;
	}

	@Override
	protected CascadingAction getCascadeAction() {
		return CascadingActions.MERGE;
	}

	@Override
	protected Map<?, ?> getMergeMap(final Object anything) {
		return ((FixedEventCache) anything).invertMap();
	}

	private EntityState getMergeEntityState(final Object entity, final MergeEvent event, final SessionImplementor source) {
		final PersistenceContext context = source.getPersistenceContext();
		final Optional<EntityEntry> entry = Optional.fromNullable(context.getEntry(entity));
		if (entry.isPresent()) {
			return getEntityState(entity, event.getEntityName(), entry.get(), source);
		}

		// Check the persistence context for an entry relating to this
		// entity to be merged...
		final EntityPersister persister = source.getEntityPersister(event.getEntityName(), entity);
		final Serializable id = persister.getIdentifier(entity, source);
		if (id != null) {
			final EntityKey key = source.generateEntityKey(id, persister);
			final Object managedEntity = context.getEntity(key);
			final Optional<EntityEntry> entry1 = Optional.fromNullable(context.getEntry(managedEntity));
			if (entry1.isPresent()) {
				// we have specialized case of a detached entity from the
				// perspective of the merge operation.  Specifically, we
				// have an incoming entity instance which has a corresponding
				// entry in the current persistence context, but registered
				// under a different entity instance
				return EntityState.DETACHED;
			}
		}
		return getEntityState(entity, event.getEntityName(), null, source);
	}

	private void entityIsPersistent(final MergeEvent event, final FixedEventCache cache) {
		LOG.trace("Ignoring persistent instance");

		final Object entity = event.getEntity();
		final EventSource source = event.getSession();
		final EntityPersister persister = source.getEntityPersister(event.getEntityName(), entity);

		cache.put(entity, entity, true);  //before cascade!

		cascadeOnMerge(source, persister, entity, cache);
		copyValues(persister, entity, entity, source, cache);

		event.setResult(entity);
	}

	private void cascadeOnMerge(
			final EventSource source,
			final EntityPersister persister,
			final Object entity,
			final Object cache) {
		source.getPersistenceContext().incrementCascadeLevel();

		try {
			new Cascade(getCascadeAction(), CascadePoint.BEFORE_MERGE, source)
					.cascade(persister, entity, cache);
		} finally {
			source.getPersistenceContext().decrementCascadeLevel();
		}
	}

	private void entityIsTransient(final MergeEvent event, final FixedEventCache cache) {
		LOG.trace("Merging transient instance");

		final Object entity = event.getEntity();
		final EventSource source = event.getSession();

		final String entityName = event.getEntityName();
		final EntityPersister persister = source.getEntityPersister(entityName, entity);

		final Serializable id = persister.hasIdentifierProperty() ?
				persister.getIdentifier(entity, source) : null;

		if (cache.containsKey(entity)) {
			persister.setIdentifier(cache.get(entity), id, source);
		} else {
			cache.put(entity, source.instantiate(persister, id), true);  //before cascade!
		}
		final Object copy = cache.get(entity);

		// cascade first, so that all unsaved objects get their
		// copy created before we actually copy
		//cascadeOnMerge(event, persister, entity, copyCache, Cascades.CASCADE_BEFORE_MERGE);
		super.cascadeBeforeSave(source, persister, entity, cache);
		copyValues(persister, entity, copy, source, cache, ForeignKeyDirection.FOREIGN_KEY_FROM_PARENT);

		saveTransientEntity(copy, entityName, event.getRequestedId(), source, cache);

		// cascade first, so that all unsaved objects get their
		// copy created before we actually copy
		super.cascadeAfterSave(source, persister, entity, cache);
		copyValues(persister, entity, copy, source, cache, ForeignKeyDirection.FOREIGN_KEY_TO_PARENT);

		event.setResult(copy);
	}

	private void saveTransientEntity(
			final Object entity,
			final String entityName,
			final Serializable requestedId,
			final EventSource source,
			final FixedEventCache cache) {
		//this bit is only *really* absolutely necessary for handling
		//requestedId, but is also good if we merge multiple object
		//graphs, since it helps ensure uniqueness
		if(requestedId == null) {
			saveWithGeneratedId(entity, entityName, cache, source, false);
		} else {
			saveWithRequestedId(entity, requestedId, entityName, cache, source);
		}

	}

	@SuppressWarnings("ObjectEquality")
	private void entityIsDetached(final MergeEvent event, final FixedEventCache cache) {
		LOG.trace("Merging detached instance");

		final Object entity = event.getEntity();
		final EventSource source = event.getSession();

		final EntityPersister persister = source.getEntityPersister(event.getEntityName(), entity);
		final String entityName = persister.getEntityName();

		Serializable id = event.getRequestedId();
		if (id == null) {
			id = persister.getIdentifier(entity, source);
		} else {
			// check that entity id = requestedId
			final Serializable previousId = persister.getIdentifier(entity, source);
			if (!persister.getIdentifierType().isEqual(id, previousId, source.getFactory())) {
				throw new HibernateException("merge requested with id not matching id of passed entity");
			}
		}

		final String previousFetchProfile = source.getLoadQueryInfluencers().getInternalFetchProfile(); // source.getFetchProfile();
		source.getLoadQueryInfluencers().setInternalFetchProfile("merge"); // source.setFetchProfile("merge");

		//we must clone embedded composite identifiers, or
		//we will get back the same instance that we pass in
		final Serializable clonedIdentifier = (Serializable) persister.getIdentifierType()
				.deepCopy(id, source.getFactory());
		final Object result = source.get(entityName, clonedIdentifier);
		source.getLoadQueryInfluencers().setInternalFetchProfile(previousFetchProfile); // source.setFetchProfile(previousFetchProfile);

		if (result == null) {
			//TODO: we should throw an exception if we really *know* for sure
			//      that this is a detached instance, rather than just assuming
			//throw new StaleObjectStateException(entityName, id);

			// we got here because we assumed that an instance
			// with an assigned id was detached, when it was
			// really persistent
			entityIsTransient(event, cache);
			return;
		}

		cache.put(entity, result, true);

		final Object target = source.getPersistenceContext().unproxy(result);
		FixedEventCache.hAssert(target != entity, "entity was not detached");

		if (!source.getEntityName(target).equals(entityName)) {
			throw new WrongClassException(
					"class of the given object did not match class of persistent copy",
					event.getRequestedId(),
					entityName
			);
		}

		if (isVersionChanged(entity, source, persister, target)) {
			if (source.getFactory().getStatistics().isStatisticsEnabled()) {
				source.getFactory().getStatisticsImplementor()
						.optimisticFailure(entityName);
			}

			throw new StaleObjectStateException(entityName, id);
		}

		// cascade first, so that all unsaved objects get their
		// copy created before we actually copy
		cascadeOnMerge(source, persister, entity, cache);
		copyValues(persister, entity, target, source, cache);

		//copyValues works by reflection, so explicitly mark the entity instance dirty
		markInterceptorDirty(target, persister);

		event.setResult(result);
	}

	private static Function<Object, Optional<Object>> getEventEntity(final MergeEvent event, final Session source) {
		return new Function<Object, Optional<Object>>() {
			@Nullable
			@Override
			public Optional<Object> apply(@Nullable final Object input) {
				return getEventEntity(event, input, source);
			}

			private Optional<Object> getEventEntity(final MergeEvent event, final Object original, final Session source) {
				if (!(original instanceof HibernateProxy)) {
					return Optional.of(original);
				}

				final LazyInitializer entityState = ((HibernateProxy) original).getHibernateLazyInitializer();
				if (entityState.isUninitialized()) {
					LOG.trace("Ignoring uninitialized Proxy");
					event.setResult(source.load(entityState.getEntityName(), entityState.getIdentifier()));
					return Optional.absent();
				}

				return Optional.fromNullable(entityState.getImplementation());
			}
		};
	}

	private static Function<Object, Optional<Object>> getMergeEntity(final MergeEvent event, final FixedEventCache cache) {
		return new Function<Object, Optional<Object>>() {
			@Nullable
			@Override
			public Optional<Object> apply(@Nullable final Object input) {
				return getMergeEntity(input, cache, event);
			}

			private Optional<Object> getMergeEntity(final Object entity, final FixedEventCache cache, final MergeEvent event) {
				if (cache.containsKey(entity) && cache.isOperatedOn(entity)) {
					LOG.trace("Merge is already in progress");
					event.setResult(entity);
					return Optional.absent();
				}

				if (cache.containsKey(entity)) {
					LOG.trace("Already in cache, setting in merge process");
					cache.setOperatedOn(entity, true);
				}

				event.setEntity(entity);
				return Optional.of(entity);
			}
		};
	}

	private static void copyValues(
			final EntityPersister persister,
			final Object entity,
			final Object target,
			final SessionImplementor source,
			final Map<?, ?> cache) {
		final Object[] values = persister.getPropertyValues(entity);
		final Object[] copiedValues = TypeHelper.replace(
				values,
				persister.getPropertyValues(target),
				persister.getPropertyTypes(),
				source,
				target,
				cache
		);

		fixValues(values, copiedValues);

		persister.setPropertyValues(target, copiedValues);
	}

	private static void fixValues(final Object[] values, final Object... copiedValues) {
		for (int i = 0; i < copiedValues.length; i++) {
			final Object copiedValue = copiedValues[i];

			if (copiedValue == null) {
				continue;
			}

			if (copiedValue instanceof Blob) {
				copiedValues[i] = values[i];
				continue;
			}

			if (copiedValue instanceof Set && ((Set<?>) copiedValue).isEmpty()) {
				copiedValues[i] = null;
			}
		}
	}

	@SuppressWarnings("ObjectEquality")
	private static void copyValues(
			final EntityPersister persister,
			final Object entity,
			final Object target,
			final SessionImplementor source,
			final Map<?, ?> cache,
			final ForeignKeyDirection foreignKeyDirection) {
		final Object[] values = persister.getPropertyValues(entity);
		final Object[] copiedValues;
		if (foreignKeyDirection == ForeignKeyDirection.FOREIGN_KEY_TO_PARENT) {
			// this is the second pass through on a merge op, so here we limit the
			// replacement to associations types (value types were already replaced
			// during the first pass)
			copiedValues = TypeHelper.replaceAssociations(
					values,
					persister.getPropertyValues(target),
					persister.getPropertyTypes(),
					source,
					target,
					cache,
					foreignKeyDirection);
		} else {
			copiedValues = TypeHelper.replace(
					values,
					persister.getPropertyValues(target),
					persister.getPropertyTypes(),
					source,
					target,
					cache,
					foreignKeyDirection);
		}

		fixValues(values, copiedValues);
		persister.setPropertyValues(target, copiedValues);
	}

	private static boolean isVersionChanged(
			final Object entity,
			final SessionImplementor source,
			final EntityPersister persister,
			final Object target) {
		if (!persister.isVersioned()) {
			return false;
		}

		// for merging of versioned entities, we consider the version having
		// been changed only when:
		// 1) the two version values are different;
		//      *AND*
		// 2) The target actually represents database state!
		//
		// This second condition is a special case which allows
		// an entity to be merged during the same transaction
		// (though during a seperate operation) in which it was
		// originally persisted/saved
		final boolean changed = !persister.getVersionType().isSame(
				persister.getVersion(target),
				persister.getVersion(entity)
		);

		// TODO : perhaps we should additionally require that the incoming entity
		// version be equivalent to the defined unsaved-value?
		return changed && existsInDatabase(target, source, persister);
	}

	private static boolean existsInDatabase(
			final Object entity,
			final SessionImplementor source,
			final EntityPersister persister) {
		final EntityEntry entry = source.getPersistenceContext().getEntry(entity);
		if (entry != null) {
			return entry.isExistsInDatabase();
		}

		final Serializable id = persister.getIdentifier(entity, source);
		if (id == null) {
			return false;
		}

		final EntityKey key = source.generateEntityKey(id, persister);
		final Object managedEntity = source.getPersistenceContext().getEntity(key);
		final EntityEntry entry1 = source.getPersistenceContext().getEntry(managedEntity);
		return entry1 != null && entry1.isExistsInDatabase();
	}

	private static void markInterceptorDirty(final Object target, final EntityPersister persister) {
		if (persister.getInstrumentationMetadata().isInstrumented()) {
			final FieldInterceptor interceptor = persister.getInstrumentationMetadata().extractInterceptor(target);
			if (interceptor != null) {
				interceptor.dirty();
			}
		}
	}

	private static <A, B> Optional<B> bind(final Optional<A> maybe, final Function<A, Optional<B>> fun) {
		if (maybe.isPresent()) {
			return fun.apply(maybe.get());
		}
		return Optional.absent();
	}
}
