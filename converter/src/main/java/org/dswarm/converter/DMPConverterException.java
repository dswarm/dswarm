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

import org.dswarm.init.DMPException;

/**
 * The exception class for DMP converter exceptions.<br>
 */

public class DMPConverterException extends DMPException {

	/**
	 *
	 */
	private static final long	serialVersionUID	= 1L;

	/**
	 * Creates a new DMP converter exception with the given exception message.
	 * 
	 * @param exception the exception message
	 */
	public DMPConverterException(final String exception) {

		super(exception);
	}

	public DMPConverterException(final String message, final Throwable cause) {

		super(message, cause);
	}

}
