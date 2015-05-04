/**
 * Copyright (C) 2013 – 2015 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
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
package org.dswarm.persistence;

import rx.functions.Action0;

/**
 * An error for persistence work.
 * This is an unchecked Exception.
 * For a checked Exception, see {@link DMPPersistenceException}.
 */
public final class DMPPersistenceError extends RuntimeException {

	private final DMPPersistenceException exception;

	public DMPPersistenceError(final String message) {
		super(message);
		exception = null;
	}

	public DMPPersistenceError(final Throwable cause) {
		super(cause);
		if (cause instanceof DMPPersistenceException) {
			exception = (DMPPersistenceException) cause;
		} else {
			exception = null;
		}
	}

	public DMPPersistenceError(final String message, final Throwable cause) {
		super(message, cause);
		if (cause instanceof DMPPersistenceException) {
			exception = (DMPPersistenceException) cause;
		} else {
			exception = null;
		}
	}

	public void unwrap() throws DMPPersistenceException {
		if (exception != null) {
			throw exception;
		}
	}

	public static DMPPersistenceError wrap(final DMPPersistenceException exception) {
		return new DMPPersistenceError(exception);
	}

	public static Throwable unwrap(final Throwable throwable) throws DMPPersistenceException {
		if (throwable instanceof DMPPersistenceError) {
			((DMPPersistenceError) throwable).unwrap();
		}
		return throwable;
	}

	public static Action0 wrapped(final PersistenceAction action) {
		return () -> {
			try {
				action.run();
			} catch (DMPPersistenceException e) {
				throw wrap(e);
			}
		};
	}

	@FunctionalInterface
	public interface PersistenceAction {

		void run() throws DMPPersistenceException;
	}
}
