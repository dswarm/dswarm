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
package org.dswarm.converter;

import rx.functions.Func1;

/**
 * An error for converter work.
 * This is an unchecked Exception.
 * For a checked Exception, see {@link DMPConverterException}.
 */
public final class DMPConverterError extends RuntimeException {

	public DMPConverterError(final String message) {
		super(message);
	}

	public DMPConverterError(final Throwable cause) {
		super(cause);
	}

	public static DMPConverterError wrap(final DMPConverterException exception) {
		return new DMPConverterError(exception);
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
	public interface ConverterFunction1<T, R> {

		R apply(final T t) throws DMPConverterException;
	}
}
