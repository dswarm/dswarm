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

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ExecutionScope implements Scope, AutoCloseable {

	private static final Logger LOG = LoggerFactory.getLogger(ExecutionScope.class);

	private final ThreadLocal<ExecutionScopeStore> scopes = new ThreadLocal<>();
	private final ThreadLocal<Integer> nestingLevel = new ThreadLocal<>();

	@Override
	public <T> Provider<T> scope(final Key<T> key, final Provider<T> unscoped) {
		return () -> {
			final ExecutionScopeStore scope = scopes.get();
			if (scope == null) {
				LOG.warn("[{}] was requested out of scope, falling back to unscoped provision", key);
				return unscoped.get();
			}

			return scope.getOrElseUpdate(key, unscoped::get);
		};
	}

	public ExecutionScope enter() {
		if (scopes.get() != null) {
			nestingLevel.set(nestingLevel.get() + 1);
			LOG.warn("An execution scope is already opened, appending to the existing one.");
			if (LOG.isDebugEnabled()) {
				final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
				if (stackTrace.length >= 3) {
					// 0 is Thread::getStackTrace
					// 1 is ExecutionScope::enter
					final StackTraceElement caller = stackTrace[2];
					LOG.debug("You may omit the call to 'enter' from {}", caller);
				}
			}
		} else {
			scopes.set(new ExecutionScopeStore());
			nestingLevel.set(0);
		}
		return this;
	}

	public void leave() {
		if (scopes.get() == null) {
			LOG.warn("No execution scope was opened, will not close anything.");
		} else {
			final Integer level = nestingLevel.get();
			if (level == 0) {
				scopes.remove();
				nestingLevel.remove();
			} else {
				nestingLevel.set(level - 1);
				LOG.warn("Nested execution scope was closed, but parent is still open.");
			}
		}
	}

	@Override
	public void close() {
		leave();
	}
}
