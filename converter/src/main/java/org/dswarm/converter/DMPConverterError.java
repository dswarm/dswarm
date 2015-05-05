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
package org.dswarm.converter;

import rx.functions.Action0;
import rx.functions.Func1;

/**
 * An error for converter work.
 * This is an unchecked Exception.
 * For a checked Exception, see {@link DMPConverterException}.
 */
public final class DMPConverterError extends RuntimeException {

	private final DMPConverterException exception;

	public DMPConverterError(final String message) {
		super(message);
		exception = null;
	}

	public DMPConverterError(final Throwable cause) {
		super(cause);
		if (cause instanceof DMPConverterException) {
			exception = (DMPConverterException) cause;
		} else {
			exception = null;
		}
	}

	public DMPConverterError(final String message, final Throwable cause) {
		super(message, cause);
		if (cause instanceof DMPConverterException) {
			exception = (DMPConverterException) cause;
		} else {
			exception = null;
		}
	}

	public void unwrap() throws DMPConverterException {
		if (exception != null) {
			throw exception;
		}
	}

	public static DMPConverterError wrap(final DMPConverterException exception) {
		return new DMPConverterError(exception);
	}

	public static Throwable unwrap(final Throwable throwable) throws DMPConverterException {
		if (throwable instanceof DMPConverterError) {
			((DMPConverterError) throwable).unwrap();
		}
		return throwable;
	}

	public static Action0 wrapped(final ConverterAction action) {
		return () -> {
			try {
				action.run();
			} catch (DMPConverterException e) {
				throw wrap(e);
			}
		};
	}

	public static <T, R> Func1<T, R> wrapped(final ConverterFunction1<T, R> func) {
		return t -> {
			try {
				return func.apply(t);
			} catch (DMPConverterException e) {
				throw wrap(e);
			}
		};
	}

	@FunctionalInterface
	public interface ConverterAction {

		void run() throws DMPConverterException;
	}

	@FunctionalInterface
	public interface ConverterFunction1<T, R> {

		R apply(final T t) throws DMPConverterException;
	}
}
